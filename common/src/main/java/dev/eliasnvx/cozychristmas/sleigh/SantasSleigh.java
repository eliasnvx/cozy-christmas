package dev.eliasnvx.cozychristmas.sleigh;

import dev.eliasnvx.cozychristmas.config.CozyConfig;
import dev.eliasnvx.cozychristmas.karma.KarmaManager;
import dev.eliasnvx.cozychristmas.present.PresentContents;
import dev.eliasnvx.cozychristmas.registry.CozyBlocks;
import dev.eliasnvx.cozychristmas.registry.CozyComponents;
import dev.eliasnvx.cozychristmas.registry.CozySounds;
import dev.eliasnvx.cozychristmas.season.ChristmasSeason;
import dev.eliasnvx.cozychristmas.stocking.StockingBlockEntity;
import dev.eliasnvx.cozychristmas.workshop.Santa;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Santa's sleigh, parked in the workshop's plaza (SPEC: on 24-25 December the elves load presents into the sleigh,
 * Santa standing by). On delivery days ({@link ChristmasSeason#isWorkshopDelivery}) the reindeer are harnessed, elves
 * carry their presents to it and players may load one too ({@code gift_given} karma once a day, and Santa remembers
 * them); the sack on the back grows. Each night of those days, with presents aboard and Santa beside it, Santa gets in
 * and it flies off ({@link SleighFlight}), sleigh bells and a trail of snow; it's back at dawn, the sack empty, and
 * Santa has a present for every helper. Not hurt by anything; a creative player can remove it.
 */
public class SantasSleigh extends Entity {

    private static final EntityDataAccessor<Integer> DATA_LOAD = SynchedEntityData.defineId(SantasSleigh.class,
            EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_PHASE = SynchedEntityData.defineId(SantasSleigh.class,
            EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_HARNESSED = SynchedEntityData.defineId(SantasSleigh.class,
            EntityDataSerializers.BOOLEAN);

    private static final String ORIGIN = "origin";
    private static final String ORIGIN_YAW = "origin_yaw";
    private static final String PHASE = "phase";
    private static final String PHASE_TICK = "phase_tick";
    private static final String LOAD = "load";
    private static final String DEPARTED = "departed_morning";
    private static final String HELPERS = "helpers";

    /** Where Santa sits, in the sleigh's frame: 26.3's passenger attachment. */
    private static final Vec3 SEAT = new Vec3(0.0, 0.55, -0.55);
    private static final int CHECK_INTERVAL = 20;
    private static final int BELL_INTERVAL = 30;
    private static final int TRAIL_INTERVAL = 2;
    /** Santa gets in from this close; {@link Santa} keeps near the sleigh on delivery days. */
    public static final double BOARD_RANGE = 8.0;
    private static final long TICKS_PER_DAY = 24000L;
    private static final long DUSK = 13000L;
    private static final long DAWN = 23000L;
    private static final long NEVER = Long.MIN_VALUE;

    private @Nullable Vec3 origin;
    private float originYaw;
    private int phaseTick;
    private long departedMorning = NEVER;
    private final Set<UUID> helpers = new HashSet<>();

    public SantasSleigh(EntityType<? extends SantasSleigh> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(DATA_LOAD, 0);
        entityData.define(DATA_PHASE, SleighFlight.Phase.PARKED.ordinal());
        entityData.define(DATA_HARNESSED, false);
    }

    /**
     * Santa sits at the back, 26.3's passenger attachment (0.55 up, 0.55 behind the middle), turned with the sleigh.
     * 1.20.1's vehicles place their riders here instead.
     */
    @Override
    protected void positionRider(Entity passenger, MoveFunction moveFunction) {
        if (hasPassenger(passenger)) {
            Vec3 seat = SEAT.yRot(-getYRot() * Mth.DEG_TO_RAD);
            moveFunction.accept(passenger, getX() + seat.x, getY() + seat.y + passenger.getMyRidingOffset(), getZ() + seat.z);
        }
    }

    public int load() {
        return entityData.get(DATA_LOAD);
    }

    public SleighFlight.Phase phase() {
        return SleighFlight.Phase.byOrdinal(entityData.get(DATA_PHASE));
    }

    /** The reindeer are in front: on delivery days and in flight. */
    public boolean isHarnessed() {
        return entityData.get(DATA_HARNESSED);
    }

    public Set<UUID> helpers() {
        return Set.copyOf(helpers);
    }

    /** Between dusk and the last of the night by the day-night clock: Santa flies then. */
    public static boolean isNight(Level level) {
        long time = Math.floorMod(level.getDayTime(), TICKS_PER_DAY);
        return time >= DUSK && time < DAWN;
    }

    private static SleighSettings settings() {
        return CozyConfig.common().workshop().sleigh();
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel level)) {
            return;
        }
        if (origin == null) {
            origin = position();
            originYaw = getYRot();
        }
        boolean flying = phase().isFlying();
        if (tickCount % CHECK_INTERVAL == 0) {
            entityData.set(DATA_HARNESSED, flying || ChristmasSeason.isWorkshopDelivery());
        }
        if (flying || tickCount % CHECK_INTERVAL == 0) {
            advance(level, isNight(level));
        }
    }

    /**
     * One step of the sleigh's day: parked, it takes off when it may; flying, it moves along its path; away, it comes
     * back once it's no longer night. GameTests call this directly with the night they want.
     */
    public void advance(ServerLevel level, boolean night) {
        if (origin == null) {
            origin = position();
            originYaw = getYRot();
        }
        SleighFlight.Phase phase = phase();
        switch (phase) {
            case PARKED -> {
                // where it stands is where it comes back to, even if something moved it
                origin = position();
                originYaw = getYRot();
                tryDepart(level, night);
            }
            case AWAY -> {
                setHidden(true); // invisibility isn't saved: hide again after a reload
                if (!night) {
                    startPhase(SleighFlight.Phase.DESCENT);
                    setHidden(false);
                    fly(level);
                }
            }
            default -> {
                phaseTick++;
                fly(level);
                if (phaseTick >= phase.ticks()) {
                    next(level, phase);
                }
            }
        }
    }

    private void next(ServerLevel level, SleighFlight.Phase finished) {
        switch (finished) {
            case TAKEOFF -> startPhase(SleighFlight.Phase.CLIMB);
            case CLIMB -> {
                startPhase(SleighFlight.Phase.AWAY);
                setHidden(true);
            }
            case DESCENT -> startPhase(SleighFlight.Phase.LANDING);
            case LANDING -> land(level);
            default -> {
            }
        }
    }

    /** Takes off if it's a delivery night, there are presents aboard, it hasn't flown tonight and Santa is near. */
    public boolean tryDepart(ServerLevel level, boolean night) {
        long morning = StockingBlockEntity.morning(level);
        if (!night || !settings().flies() || load() == 0 || departedMorning == morning || !ChristmasSeason.isWorkshopDelivery()) {
            return false;
        }
        Santa santa = nearbySanta(level);
        if (santa == null || !santa.startRiding(this, true)) {
            return false;
        }
        departedMorning = morning;
        entityData.set(DATA_HARNESSED, true);
        startPhase(SleighFlight.Phase.TAKEOFF);
        noPhysics = true;
        level.playSound(null, getX(), getY(), getZ(), CozySounds.SANTA_YES.get(), SoundSource.NEUTRAL, 2.0F, 1.0F);
        level.playSound(null, getX(), getY(), getZ(), CozySounds.REINDEER_BELLS.get(), SoundSource.NEUTRAL, 2.0F, 1.0F);
        return true;
    }

    private @Nullable Santa nearbySanta(ServerLevel level) {
        List<Santa> santas = level.getEntitiesOfClass(Santa.class, getBoundingBox().inflate(BOARD_RANGE),
                santa -> santa.isAlive() && !santa.isPassenger() && santa.getTradingPlayer() == null && santa.getTarget() == null);
        return santas.stream().min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
    }

    private void startPhase(SleighFlight.Phase phase) {
        phaseTick = 0;
        entityData.set(DATA_PHASE, phase.ordinal());
    }

    /** Moves to the path's pose for now, with bells and a trail of snow. */
    private void fly(ServerLevel level) {
        Vec3 base = origin == null ? position() : origin;
        SleighFlight.Pose pose = SleighFlight.pose(phase(), phaseTick, originYaw);
        setPos(base.x + pose.dx(), base.y + pose.dy(), base.z + pose.dz());
        setYRot(pose.yaw());
        if (isInvisible()) {
            return;
        }
        if (phaseTick % TRAIL_INTERVAL == 0) {
            Vec3 back = Vec3.directionFromRotation(0.0F, getYRot()).scale(-1.5);
            level.sendParticles(ParticleTypes.SNOWFLAKE, getX() + back.x, getY() + 0.4, getZ() + back.z, 3, 0.25, 0.15, 0.25, 0.01);
        }
        if (phaseTick % BELL_INTERVAL == 0) {
            level.playSound(null, getX(), getY(), getZ(), CozySounds.REINDEER_BELLS.get(), SoundSource.NEUTRAL, 3.0F, 1.0F);
        }
    }

    /** Back where it was parked: Santa gets out and remembers who helped, the sack is empty. */
    private void land(ServerLevel level) {
        Vec3 base = origin == null ? position() : origin;
        setPos(base);
        setYRot(originYaw);
        noPhysics = false;
        startPhase(SleighFlight.Phase.PARKED);
        for (Entity passenger : List.copyOf(getPassengers())) {
            passenger.stopRiding();
            Vec3 side = Vec3.directionFromRotation(0.0F, originYaw + 90.0F).scale(1.8);
            passenger.teleportTo(base.x + side.x, base.y, base.z + side.z);
            if (passenger instanceof Santa santa) {
                santa.thank(helpers);
            }
        }
        helpers.clear();
        entityData.set(DATA_LOAD, 0);
        level.playSound(null, getX(), getY(), getZ(), CozySounds.SANTA_GIFT.get(), SoundSource.NEUTRAL, 1.5F, 1.0F);
    }

    /**
     * Removed in flight (a creative player's hit, /kill): Santa is set down where it was parked, seen again, and
     * remembers who helped.
     */
    @Override
    public void remove(RemovalReason reason) {
        if (level() instanceof ServerLevel && phase().isFlying() && origin != null) {
            Vec3 base = origin;
            Vec3 side = Vec3.directionFromRotation(0.0F, originYaw + 90.0F).scale(1.8);
            for (Entity passenger : List.copyOf(getPassengers())) {
                passenger.stopRiding();
                passenger.setInvisible(false);
                passenger.teleportTo(base.x + side.x, base.y, base.z + side.z);
                passenger.resetFallDistance();
                if (passenger instanceof Santa santa) {
                    santa.thank(helpers);
                }
            }
        }
        super.remove(reason);
    }

    private void setHidden(boolean hidden) {
        setInvisible(hidden);
        for (Entity passenger : getPassengers()) {
            passenger.setInvisible(hidden);
        }
    }

    /** Whether it takes presents now: parked, on a delivery day, with room left. */
    public boolean isLoading() {
        return phase() == SleighFlight.Phase.PARKED && ChristmasSeason.isWorkshopDelivery() && load() < settings().capacity();
    }

    /** One more present aboard ({@code helper}: the player who loaded it, null for an elf); false if it takes none now. */
    public boolean loadPresent(@Nullable ServerPlayer helper) {
        if (!isLoading()) {
            return false;
        }
        entityData.set(DATA_LOAD, load() + 1);
        if (helper != null) {
            helpers.add(helper.getUUID());
        }
        playSound(SoundEvents.WOOL_PLACE, 0.8F, 1.1F);
        return true;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (phase().isFlying()) {
            return InteractionResult.PASS;
        }
        ItemStack stack = player.getItemInHand(hand);
        boolean present = stack.is(CozyBlocks.PRESENT_ITEM.get());
        if (!(player instanceof ServerPlayer server)) {
            return present ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
        }
        int capacity = settings().capacity();
        if (!ChristmasSeason.isWorkshopDelivery()) {
            server.displayClientMessage(Component.translatable("message.cozychristmas.sleigh.not_yet"), true);
        } else if (!present) {
            server.displayClientMessage(Component.translatable("message.cozychristmas.sleigh.status", load(), capacity), true);
        } else if (isPrank(stack)) {
            server.displayClientMessage(Component.translatable("message.cozychristmas.sleigh.prank"), true);
        } else if (!loadPresent(server)) {
            server.displayClientMessage(Component.translatable("message.cozychristmas.sleigh.full"), true);
        } else {
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            KarmaManager.rewardGift(server);
            server.displayClientMessage(Component.translatable("message.cozychristmas.sleigh.loaded", load(), capacity), true);
        }
        return InteractionResult.CONSUME;
    }

    private static boolean isPrank(ItemStack stack) {
        PresentContents contents = CozyComponents.PRESENT_CONTENTS.get(stack);
        return contents != null && contents.isBoom();
    }

    @Override
    public boolean isPickable() {
        return !isInvisible() && isAlive();
    }

    /** Nothing hurts it; a creative player's hit removes it (and lets Santa out). */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!level().isClientSide() && source.getEntity() instanceof Player player && player.isCreative()) {
            ejectPassengers();
            discard();
            return true;
        }
        return false;
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return getPassengers().isEmpty() && passenger instanceof Santa;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        origin = tag.contains(ORIGIN) ? Vec3.CODEC.parse(NbtOps.INSTANCE, tag.get(ORIGIN)).result().orElse(null) : null;
        originYaw = tag.contains(ORIGIN_YAW, Tag.TAG_FLOAT) ? tag.getFloat(ORIGIN_YAW) : getYRot();
        entityData.set(DATA_PHASE, tag.getInt(PHASE)); // missing: 0, parked
        phaseTick = tag.getInt(PHASE_TICK);
        entityData.set(DATA_LOAD, tag.getInt(LOAD));
        departedMorning = tag.contains(DEPARTED, Tag.TAG_LONG) ? tag.getLong(DEPARTED) : NEVER;
        helpers.clear();
        if (tag.contains(HELPERS)) {
            UUIDUtil.CODEC.listOf().parse(NbtOps.INSTANCE, tag.get(HELPERS)).result().ifPresent(helpers::addAll);
        }
        noPhysics = phase().isFlying();
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (phase().isFlying()) { // parked, where it stands is the origin (and a saved template mustn't carry it)
            if (origin != null) {
                Vec3.CODEC.encodeStart(NbtOps.INSTANCE, origin).result().ifPresent(encoded -> tag.put(ORIGIN, encoded));
            }
            tag.putFloat(ORIGIN_YAW, originYaw);
        }
        tag.putInt(PHASE, phase().ordinal());
        tag.putInt(PHASE_TICK, phaseTick);
        tag.putInt(LOAD, load());
        tag.putLong(DEPARTED, departedMorning);
        UUIDUtil.CODEC.listOf().encodeStart(NbtOps.INSTANCE, List.copyOf(helpers)).result()
                .ifPresent(encoded -> tag.put(HELPERS, encoded));
    }
}
