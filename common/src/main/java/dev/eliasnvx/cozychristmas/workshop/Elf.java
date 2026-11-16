package dev.eliasnvx.cozychristmas.workshop;

import dev.eliasnvx.cozychristmas.config.CozyConfig;
import dev.eliasnvx.cozychristmas.present.PresentDesign;
import dev.eliasnvx.cozychristmas.registry.CozyBlocks;
import dev.eliasnvx.cozychristmas.registry.CozyItems;
import dev.eliasnvx.cozychristmas.registry.CozySounds;
import dev.eliasnvx.cozychristmas.registry.CozyTags;
import dev.eliasnvx.cozychristmas.season.ChristmasSeason;
import dev.eliasnvx.cozychristmas.sleigh.SantasSleigh;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.EnumSet;

/**
 * An elf of Santa's workshop. Elves potter between the workbenches ({@code #cozychristmas:elf_workstations}),
 * hammering away, and now and then carry a finished present to the pile. They're peaceful: they run from monsters, from
 * players with a weapon drawn ({@code #cozychristmas:scares_elves}) and from whoever hurt one of them, and every hurt
 * raises the {@link WorkshopAlarm}, so Santa comes. Elves follow a player holding {@code #cozychristmas:elf_treats}.
 * Hurting or killing one is naughty ({@code #cozychristmas:karma_protected}).
 */
public class Elf extends PathfinderMob {

    /** Eye height (blocks): 1.20.1 has none in the entity type. */
    private static final float EYE_HEIGHT = 0.9F;

    /** Elves placed by the workshop stay around it. */
    private static final int HOME_RADIUS = 24;
    private static final float THREAT_DISTANCE = 6.0F;
    private static final float CULPRIT_DISTANCE = 16.0F;
    private static final float MONSTER_DISTANCE = 8.0F;
    private static final double WALK = 1.0;
    private static final double RUN = 1.4;
    private static final int FLEE_TICKS = 300;
    /** How far an elf will carry a present to Santa's sleigh. */
    private static final double SLEIGH_SEARCH = 40.0;

    private @Nullable LivingEntity culprit;
    private long fleeUntil;

    public Elf(EntityType<? extends Elf> type, Level level) {
        super(type, level);
        setDropChance(EquipmentSlot.MAINHAND, 0.0F); // the present it carries belongs to the workshop
    }

    /** 1.20.1 has no eye height in the entity type: 26.3's. */
    @Override
    protected float getStandingEyeHeight(Pose pose, EntityDimensions dimensions) {
        return EYE_HEIGHT;
    }

    public static AttributeSupplier.Builder createAttributes(ElfSettings settings) {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, settings.health())
                .add(Attributes.MOVEMENT_SPEED, settings.speed()); // 1.20.1's TemptGoal reaches 10 blocks, as 26.3's tempt_range
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new PanicGoal(this, RUN));
        goalSelector.addGoal(1, new AvoidEntityGoal<>(this, LivingEntity.class, CULPRIT_DISTANCE, WALK, RUN, this::isCulprit) {
            @Override
            public boolean canUse() {
                return culprit != null && super.canUse(); // skips the entity scan while nobody is wanted
            }
        });
        goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Player.class, THREAT_DISTANCE, WALK, RUN,
                entity -> entity instanceof Player player && isThreat(player)));
        goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Monster.class, MONSTER_DISTANCE, WALK, RUN));
        goalSelector.addGoal(3, new TemptGoal(this, WALK, Ingredient.of(CozyTags.ELF_TREATS), false));
        goalSelector.addGoal(4, new CarryPresentGoal());
        goalSelector.addGoal(5, new WorkAtStationGoal());
        goalSelector.addGoal(6, new MoveTowardsRestrictionGoal(this, WALK));
        goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, WALK * 0.8));
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 6.0F));
        goalSelector.addGoal(9, new RandomLookAroundGoal(this));
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                                  MobSpawnType reason, @Nullable SpawnGroupData groupData,
                                                  @Nullable CompoundTag spawnTag) {
        if (reason == MobSpawnType.STRUCTURE) {
            restrictTo(blockPosition(), HOME_RADIUS);
        }
        return super.finalizeSpawn(level, difficulty, reason, groupData, spawnTag);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        MobHome.save(this, tag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        MobHome.load(this, tag);
    }

    /** Elves belong to the workshop; they don't wander off and vanish. */
    @Override
    public boolean removeWhenFarAway(double distanceSquared) {
        return false;
    }

    /** Runs from {@code culprit} for a while (alarm). */
    public void fleeFrom(LivingEntity culprit) {
        this.culprit = culprit;
        this.fleeUntil = level().getGameTime() + FLEE_TICKS;
    }

    public boolean isFleeingFrom(LivingEntity entity) {
        return culprit == entity && level().getGameTime() < fleeUntil;
    }

    private boolean isCulprit(LivingEntity entity) {
        return isFleeingFrom(entity) && WorkshopAlarm.canBeBlamed(entity);
    }

    private static boolean isThreat(Player player) {
        return !player.isCreative() && !player.isSpectator() && player.getMainHandItem().is(CozyTags.SCARES_ELVES);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (culprit != null && (level().getGameTime() >= fleeUntil || !culprit.isAlive())) {
            culprit = null;
        }
    }

    @Override
    public boolean hurt(DamageSource source, float damage) {
        boolean hurt = super.hurt(source, damage);
        if (hurt && level() instanceof ServerLevel level && source.getEntity() instanceof LivingEntity attacker) {
            WorkshopAlarm.raise(level, position(), attacker);
        }
        return hurt;
    }

    /**
     * Puts the present in hand away: into Santa's sleigh if one taking presents is within reach (it counts as
     * loaded), otherwise just onto the pile. True if it went into the sleigh.
     */
    public boolean putPresentAway() {
        if (!isCarryingPresent()) {
            return false;
        }
        SantasSleigh sleigh = loadingSleigh(CarryPresentGoal.SLEIGH_REACH + 1.0);
        boolean loaded = sleigh != null && sleigh.loadPresent(null);
        setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        swing(InteractionHand.MAIN_HAND);
        playSound(SoundEvents.WOOL_PLACE, 0.8F, 1.2F);
        return loaded;
    }

    /** The nearest parked sleigh within {@code range} that takes presents now, if any. */
    private @Nullable SantasSleigh loadingSleigh(double range) {
        return level().getEntitiesOfClass(SantasSleigh.class, getBoundingBox().inflate(range), SantasSleigh::isLoading).stream()
                .min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
    }

    public boolean isCarryingPresent() {
        return getMainHandItem().is(CozyBlocks.PRESENT_ITEM.get());
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return CozySounds.ELF_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return CozySounds.ELF_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return CozySounds.ELF_DEATH.get();
    }

    /**
     * Walks to a free spot next to the nearest workstation within {@code workshop.elf.work_range} and works there for a
     * while: swings its hammer arm with a tap now and then. Afterwards it may pick up a finished present to carry.
     */
    final class WorkAtStationGoal extends Goal {

        private static final int SEARCH_HEIGHT = 2;
        private static final int MIN_WORK = 160;
        private static final int MAX_WORK = 360;
        private static final int GIVE_UP_TICKS = 300;
        private static final double REACH = 1.3;
        private static final float CARRY_CHANCE = 0.5F;

        private @Nullable BlockPos station;
        private @Nullable BlockPos stand;
        private int cooldown;
        private int tries;
        private int worked;
        private int workFor;
        private int nextSwing;

        WorkAtStationGoal() {
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (cooldown > 0) {
                cooldown--;
                return false;
            }
            cooldown = reducedTickDelay(60 + getRandom().nextInt(80));
            if (isCarryingPresent()) {
                return false;
            }
            return findStation();
        }

        private boolean findStation() {
            int range = CozyConfig.common().workshop().elf().workRange();
            BlockPos origin = blockPosition();
            double best = Double.MAX_VALUE;
            station = null;
            stand = null;
            for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-range, -SEARCH_HEIGHT, -range),
                    origin.offset(range, SEARCH_HEIGHT, range))) {
                if (!isWithinRestriction(pos) || !level().getBlockState(pos).is(CozyTags.ELF_WORKSTATIONS)) {
                    continue;
                }
                BlockPos spot = standingSpot(pos);
                double distance = spot == null ? Double.MAX_VALUE : spot.distSqr(origin);
                if (distance < best) {
                    best = distance;
                    station = pos.immutable();
                    stand = spot;
                }
            }
            return station != null;
        }

        /** The side of the station an elf can stand on (floor below, room for its height), nearest to it. */
        private @Nullable BlockPos standingSpot(BlockPos station) {
            BlockPos best = null;
            double bestDistance = Double.MAX_VALUE;
            for (Direction side : Direction.Plane.HORIZONTAL) {
                BlockPos spot = station.relative(side);
                BlockState feet = level().getBlockState(spot);
                if (!feet.getCollisionShape(level(), spot).isEmpty()
                        || !level().getBlockState(spot.above()).getCollisionShape(level(), spot.above()).isEmpty()
                        || !level().getBlockState(spot.below()).isFaceSturdy(level(), spot.below(), Direction.UP)) {
                    continue;
                }
                double distance = spot.distSqr(blockPosition());
                if (distance < bestDistance) {
                    bestDistance = distance;
                    best = spot.immutable();
                }
            }
            return best;
        }

        @Override
        public boolean canContinueToUse() {
            return station != null && stand != null && culprit == null && tries < GIVE_UP_TICKS && worked < workFor
                    && level().getBlockState(station).is(CozyTags.ELF_WORKSTATIONS);
        }

        @Override
        public void start() {
            tries = 0;
            worked = 0;
            workFor = MIN_WORK + getRandom().nextInt(MAX_WORK - MIN_WORK);
            nextSwing = 0;
            moveToStand();
        }

        private void moveToStand() {
            if (stand != null) {
                getNavigation().moveTo(stand.getX() + 0.5, stand.getY(), stand.getZ() + 0.5, WALK);
            }
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            if (station == null || stand == null) {
                return;
            }
            Vec3 bench = Vec3.atCenterOf(station);
            if (!stand.closerToCenterThan(position(), REACH)) {
                tries++;
                if (tries % 40 == 0) {
                    moveToStand();
                }
                return;
            }
            getNavigation().stop();
            getLookControl().setLookAt(bench.x, bench.y + 0.5, bench.z);
            worked++;
            if (--nextSwing <= 0) {
                nextSwing = 8 + getRandom().nextInt(8);
                swing(InteractionHand.MAIN_HAND);
                if (getRandom().nextInt(3) == 0) {
                    playSound(CozySounds.ELF_WORK.get(), 0.6F, 0.9F + getRandom().nextFloat() * 0.3F);
                }
            }
        }

        @Override
        public void stop() {
            // on delivery days every finished present goes to the sleigh
            if (worked >= workFor && (ChristmasSeason.isWorkshopDelivery() || getRandom().nextFloat() < CARRY_CHANCE)) {
                PresentDesign[] designs = PresentDesign.values();
                setItemSlot(EquipmentSlot.MAINHAND, CozyItems.present(designs[getRandom().nextInt(designs.length)]));
            }
            station = null;
            stand = null;
            getNavigation().stop();
        }
    }

    /**
     * With a finished present in hand: carries it to Santa's sleigh on delivery days (while it takes presents),
     * otherwise to the nearest present nearby (the pile) or, if there's none, a little way off, and puts it away there.
     * The present in hand is only for show; it never drops.
     */
    final class CarryPresentGoal extends Goal {

        private static final int PILE_RANGE = 12;
        private static final int PILE_HEIGHT = 3;
        private static final int GIVE_UP_TICKS = 400;
        private static final double REACH = 1.8;
        private static final double SLEIGH_REACH = 2.4;

        private @Nullable Vec3 target;
        private double reach = REACH;
        private int tries;

        CarryPresentGoal() {
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return isCarryingPresent() && culprit == null;
        }

        @Override
        public boolean canContinueToUse() {
            return target != null && isCarryingPresent() && culprit == null && tries < GIVE_UP_TICKS;
        }

        @Override
        public void start() {
            tries = 0;
            SantasSleigh sleigh = loadingSleigh(SLEIGH_SEARCH);
            BlockPos pile = sleigh == null ? nearestPresent() : null;
            if (sleigh != null) {
                target = sleigh.position();
            } else {
                target = pile != null ? Vec3.atBottomCenterOf(pile) : DefaultRandomPos.getPos(Elf.this, 8, 3);
            }
            reach = sleigh != null ? SLEIGH_REACH : REACH;
            if (target == null) {
                putAway();
                return;
            }
            getNavigation().moveTo(target.x, target.y, target.z, WALK);
        }

        private @Nullable BlockPos nearestPresent() {
            BlockPos origin = blockPosition();
            BlockPos best = null;
            double bestDistance = Double.MAX_VALUE;
            for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-PILE_RANGE, -PILE_HEIGHT, -PILE_RANGE),
                    origin.offset(PILE_RANGE, PILE_HEIGHT, PILE_RANGE))) {
                if (isWithinRestriction(pos) && level().getBlockState(pos).is(CozyBlocks.PRESENT.get())) {
                    double distance = pos.distSqr(origin);
                    if (distance < bestDistance) {
                        bestDistance = distance;
                        best = pos.immutable();
                    }
                }
            }
            return best;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            tries++;
            if (target == null) {
                return;
            }
            if (position().closerThan(target, reach)) {
                putAway();
            } else if (tries % 40 == 0) {
                getNavigation().moveTo(target.x, target.y, target.z, WALK);
            }
        }

        @Override
        public void stop() {
            if (tries >= GIVE_UP_TICKS) {
                putAway();
            }
            target = null;
        }

        private void putAway() {
            putPresentAway();
            target = null;
        }
    }
}
