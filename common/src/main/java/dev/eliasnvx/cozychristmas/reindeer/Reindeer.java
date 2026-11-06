package dev.eliasnvx.cozychristmas.reindeer;

import dev.eliasnvx.cozychristmas.config.CozyConfig;
import dev.eliasnvx.cozychristmas.karma.KarmaManager;
import dev.eliasnvx.cozychristmas.registry.CozyEntities;
import dev.eliasnvx.cozychristmas.registry.CozySounds;
import dev.eliasnvx.cozychristmas.registry.CozyTags;
import dev.eliasnvx.cozychristmas.season.ChristmasSeason;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * A reindeer: a mount like a horse, tamed with carrots ({@code #cozychristmas:reindeer_taming_items}, each one a
 * {@code reindeer.tame_chance}) and bred with golden carrots. Feeding one is a good deed ({@code karma.feed_reindeer},
 * once per reindeer per game day). Saddled and ridden, it flies whenever {@code reindeer.flight} allows (the Christmas
 * season by default): jump to take off, then it goes where the rider looks, holding jump climbs, and it hovers slowly down
 * when let be. A flying-capable reindeer takes no fall damage.
 *
 * <p>Ridden movement runs on the rider's client, so whether flight is allowed now is synced as entity data
 * ({@link #DATA_CAN_FLY}), refreshed by the server every second from the season and the config.
 */
public class Reindeer extends AbstractHorse {

    private static final EntityDataAccessor<Boolean> DATA_CAN_FLY = SynchedEntityData.defineId(Reindeer.class,
            EntityDataSerializers.BOOLEAN);
    private static final int FLIGHT_CHECK_INTERVAL = 20;
    private static final int BELLS_INTERVAL = 16;
    /** Reindeer placed by the workshop stay around the stable until tamed. */
    private static final int HOME_RADIUS = 12;
    /** Horizontal drag in flight, like vanilla's air drag. */
    private static final double FLIGHT_DRAG = 0.91;
    /** How fast the vertical speed follows the rider's steering (0..1 per tick). */
    private static final double CLIMB_RESPONSE = 0.25;
    /** Let be, it sinks this fast, so a rider who lets go comes down gently. */
    private static final double HOVER_SINK = -0.03;
    /** Top flying speed is about ten times the per-tick push (drag 0.91). */
    private static final double TOP_SPEED_FACTOR = 10.0;
    private static final String LAST_FED_DAY = "last_fed_day";

    private long lastFedDay = Long.MIN_VALUE;

    public Reindeer(EntityType<? extends Reindeer> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes(ReindeerSettings settings) {
        return AbstractHorse.createBaseHorseAttributes()
                .add(Attributes.MAX_HEALTH, settings.health())
                .add(Attributes.MOVEMENT_SPEED, settings.speed())
                .add(Attributes.JUMP_STRENGTH, settings.jumpStrength());
    }

    /** Natural spawns: on {@code #cozychristmas:reindeer_spawnable_on} in daylight, like other animals. */
    public static boolean checkReindeerSpawnRules(EntityType<Reindeer> type, LevelAccessor level, EntitySpawnReason reason,
                                                  BlockPos pos, RandomSource random) {
        return level.getBlockState(pos.below()).is(CozyTags.REINDEER_SPAWNABLE_ON) && Animal.isBrightEnoughToSpawn(level, pos);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(DATA_CAN_FLY, false);
    }

    @Override
    protected void addBehaviourGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new PanicGoal(this, 1.2));
        goalSelector.addGoal(3, new TemptGoal(this, 1.25, stack -> stack.is(CozyTags.REINDEER_FOOD), false));
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                                  EntitySpawnReason reason, @Nullable SpawnGroupData groupData) {
        if (reason == EntitySpawnReason.STRUCTURE) {
            setHomeTo(blockPosition(), HOME_RADIUS);
        }
        return super.finalizeSpawn(level, difficulty, reason, groupData);
    }

    // --- flight --------------------------------------------------------------------------------------------------

    /** Saddled, and flight is allowed right now. */
    public boolean canFly() {
        return isSaddled() && entityData.get(DATA_CAN_FLY);
    }

    /** In the air under a rider who can make it fly. */
    public boolean isFlying() {
        return canFly() && !onGround() && !isInLiquid() && getControllingPassenger() instanceof Player;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            return;
        }
        if (tickCount % FLIGHT_CHECK_INTERVAL == 0) {
            refreshFlight(ChristmasSeason.isActive());
        }
        if (tickCount % BELLS_INTERVAL == 0 && isFlying()) {
            playSound(CozySounds.REINDEER_BELLS.get(), 0.6F, 0.95F + getRandom().nextFloat() * 0.1F);
        }
    }

    /** Server: whether flight is allowed now, from {@code reindeer.flight} and the season; synced to clients. */
    public void refreshFlight(boolean seasonActive) {
        entityData.set(DATA_CAN_FLY, CozyConfig.common().reindeer().flight().allows(seasonActive));
    }

    @Override
    public void travel(Vec3 input) {
        if (getControllingPassenger() instanceof Player rider && canFly() && !onGround() && !isInLiquid()) {
            travelInFlight(rider, input);
        } else {
            super.travel(input);
        }
    }

    /**
     * No gravity: the rider's forward and sideways input push along the facing, the rider's pitch tilts that up or
     * down, holding jump climbs. The vertical speed eases toward its target so turns don't jolt.
     */
    private void travelInFlight(Player rider, Vec3 input) {
        ReindeerSettings settings = CozyConfig.common().reindeer();
        moveRelative((float) settings.flySpeed(), new Vec3(input.x, 0.0, input.z));
        Vec3 motion = getDeltaMovement();
        double targetClimb;
        if (rider.isJumping()) {
            targetClimb = settings.climbSpeed();
        } else if (input.z > 0.0) {
            double topSpeed = settings.flySpeed() * TOP_SPEED_FACTOR;
            targetClimb = -Mth.sin(rider.getXRot() * Mth.DEG_TO_RAD) * topSpeed * input.z;
        } else {
            targetClimb = HOVER_SINK;
        }
        double climb = motion.y + (targetClimb - motion.y) * CLIMB_RESPONSE;
        move(MoverType.SELF, new Vec3(motion.x, climb, motion.z));
        setDeltaMovement(motion.x * FLIGHT_DRAG, climb, motion.z * FLIGHT_DRAG);
        resetFallDistance();
    }

    /** No jump bar in the air: there the jump key climbs instead. */
    @Override
    public boolean canJump() {
        return super.canJump() && (onGround() || !canFly());
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float damageModifier, DamageSource damageSource) {
        return !canFly() && super.causeFallDamage(fallDistance, damageModifier, damageSource);
    }

    // --- feeding and taming --------------------------------------------------------------------------------------

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(CozyTags.REINDEER_FOOD);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        boolean openInventory = !isBaby() && isTamed() && player.isSecondaryUseActive();
        if (isVehicle() || openInventory || held.isEmpty()) {
            return super.mobInteract(player, hand);
        }
        if (!isTamed() && !isBaby() && held.is(CozyTags.REINDEER_TAMING_ITEMS)) {
            return feedToTame(player, held);
        }
        if (isFood(held)) {
            InteractionResult result = fedFood(player, held);
            if (result.consumesAction()) {
                rewardFeeding(player);
            }
            return result;
        }
        if (!isTamed()) {
            makeMad();
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    /** One carrot, one chance: hearts and a new owner, or a puff of smoke. */
    private InteractionResult feedToTame(Player player, ItemStack carrot) {
        if (level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        carrot.consume(1, player);
        playEatingSound();
        rewardFeeding(player);
        if (getRandom().nextFloat() < CozyConfig.common().reindeer().tameChance()) {
            tameWithName(player);
        } else {
            level().broadcastEntityEvent(this, (byte) 6); // smoke, like a failed horse taming
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    /** Vanilla's is empty for animals; horses play theirs inside their eating handler. */
    @Override
    protected void playEatingSound() {
        SoundEvent sound = getEatingSound();
        if (sound != null) {
            makeSound(sound);
        }
    }

    @Override
    public boolean tameWithName(Player player) {
        clearHome(); // a tamed reindeer goes with its owner
        return super.tameWithName(player);
    }

    private void rewardFeeding(Player player) {
        if (player instanceof ServerPlayer feeder && feeder.level() instanceof ServerLevel level) {
            long today = KarmaManager.today(level);
            if (lastFedDay != today) {
                lastFedDay = today;
                KarmaManager.add(feeder, CozyConfig.common().karma().feedReindeer());
            }
        }
    }

    // --- breeding ------------------------------------------------------------------------------------------------

    @Override
    public boolean canMate(Animal partner) {
        return partner != this && partner instanceof Reindeer other && canParent() && other.canParent();
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        Reindeer baby = CozyEntities.REINDEER.get().create(level, EntitySpawnReason.BREEDING);
        if (baby != null && partner instanceof AbstractHorse parent) {
            setOffspringAttributes(parent, baby);
        }
        return baby;
    }

    // --- saving and sounds ---------------------------------------------------------------------------------------

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putLong(LAST_FED_DAY, lastFedDay);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        lastFedDay = input.getLongOr(LAST_FED_DAY, Long.MIN_VALUE);
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return CozySounds.REINDEER_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return CozySounds.REINDEER_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return CozySounds.REINDEER_DEATH.get();
    }

    @Override
    protected @Nullable SoundEvent getEatingSound() {
        return CozySounds.REINDEER_EAT.get();
    }

    @Override
    protected @Nullable SoundEvent getAngrySound() {
        return CozySounds.REINDEER_ANGRY.get();
    }
}
