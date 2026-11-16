package dev.eliasnvx.cozychristmas.krampus;

import dev.eliasnvx.cozychristmas.config.CozyConfig;
import dev.eliasnvx.cozychristmas.goblin.PresentGoblin;
import dev.eliasnvx.cozychristmas.karma.KarmaManager;
import dev.eliasnvx.cozychristmas.registry.CozyEffects;
import dev.eliasnvx.cozychristmas.registry.CozyEntities;
import dev.eliasnvx.cozychristmas.registry.CozySounds;
import dev.eliasnvx.cozychristmas.util.Players;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Krampus (SPEC boss): a shaggy, horned, goat-legged demon with a birch switch in one hand and a chain in the other,
 * a basket on his back. He lives in his lair (structure) and comes himself to very naughty players
 * ({@link KrampusVisits}). He hits with the switch up close, lashes with the chain from further off (it pulls the
 * target in), puts players in his sack for a moment ({@link SackedEffect}) and calls present goblins below two thirds
 * and one third of his health. Beating him clears the bad karma of everyone who fought him. Settings:
 * {@link KrampusSettings}.
 */
public class Krampus extends Monster {

    /** Eye height (blocks): 1.20.1 has none in the entity type. */
    private static final float EYE_HEIGHT = 2.45F;

    /** Client: how long each move's animation runs. */
    public static final int LASH_TICKS = 12;
    public static final int SACK_TICKS = 14;
    public static final int ROAR_TICKS = 30;
    private static final byte LASH_EVENT = EntityEvent.START_ATTACKING;
    private static final byte SACK_EVENT = EntityEvent.STOP_ATTACKING;
    private static final byte ROAR_EVENT = EntityEvent.EAT_GRASS;

    private static final int HOME_RADIUS = 10;
    private static final double FOLLOW_RANGE = 40.0;
    private static final double KNOCKBACK_RESISTANCE = 0.8;
    private static final float STEP_HEIGHT = 1.0F;
    private static final double CHASE = 1.1;
    private static final float LOOK_DISTANCE = 12.0F;
    private static final double SACK_REACH = 3.5;
    private static final double PULL = 1.1;
    private static final double PULL_UP = 0.35;
    private static final int SUMMON_RADIUS = 3;
    private static final int SUMMON_TRIES = 10;
    /** Fighters this far away when he falls don't count (they left). */
    private static final double FIGHTER_RANGE = 64.0;
    /** A visitor whose player is gone or this far away leaves. */
    private static final double VISIT_RANGE = 96.0;
    private static final int VANISH_PARTICLES = 40;
    private static final int XP = 50;

    /** Blocks from him within which a player sees his boss bar (fighters always do). */
    private static final double BOSS_BAR_RANGE = 40.0;
    private static final int BOSS_BAR_CHECK = 20;
    private final ServerBossEvent bossEvent = new ServerBossEvent(getDisplayName(),
            BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.NOTCHED_10);
    private final Set<UUID> fighters = new HashSet<>();
    private int summons;
    private @Nullable UUID visiting;
    /** Players whose clients track him; the boss bar goes to those of them near or fighting ({@link #updateBossBar}). */
    private final Set<ServerPlayer> seenBy = new HashSet<>();
    private int lashCooldown;
    private int sackCooldown;
    // client animation counters
    private int lashTicks;
    private int sackTicks;
    private int roarTicks;

    public Krampus(EntityType<? extends Krampus> type, Level level) {
        super(type, level);
        xpReward = XP;
        setMaxUpStep(STEP_HEIGHT); // 1.20.1 has no step height attribute
    }

    /** 1.20.1 has no eye height in the entity type: 26.3's. */
    @Override
    protected float getStandingEyeHeight(Pose pose, EntityDimensions dimensions) {
        return EYE_HEIGHT;
    }

    private static KrampusSettings settings() {
        return CozyConfig.common().krampus();
    }

    public static AttributeSupplier.Builder createAttributes(KrampusSettings settings) {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, settings.health())
                .add(Attributes.ARMOR, settings.armor())
                .add(Attributes.ATTACK_DAMAGE, settings.attackDamage())
                .add(Attributes.MOVEMENT_SPEED, settings.speed())
                .add(Attributes.FOLLOW_RANGE, FOLLOW_RANGE)
                .add(Attributes.KNOCKBACK_RESISTANCE, KNOCKBACK_RESISTANCE);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, CHASE, true));
        goalSelector.addGoal(4, new MoveTowardsRestrictionGoal(this, 1.0));
        goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.7));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, LOOK_DISTANCE));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this, PresentGoblin.class));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    /** In his lair he keeps to the arena and stays for good. */
    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                                  MobSpawnType reason, @Nullable SpawnGroupData groupData,
                                                  @Nullable CompoundTag spawnTag) {
        if (reason == MobSpawnType.STRUCTURE) {
            restrictTo(blockPosition(), HOME_RADIUS);
        }
        setPersistenceRequired();
        return super.finalizeSpawn(level, difficulty, reason, groupData, spawnTag);
    }

    // --- the fight ------------------------------------------------------------------------------------------------

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (!(level() instanceof ServerLevel level)) {
            return;
        }
        bossEvent.setProgress(getHealth() / getMaxHealth());
        if (tickCount % BOSS_BAR_CHECK == 0) {
            seenBy.forEach(this::updateBossBar);
        }
        if (lashCooldown > 0) {
            lashCooldown--;
        }
        if (sackCooldown > 0) {
            sackCooldown--;
        }
        if (visiting != null && shouldLeave(level)) {
            vanish(level);
            return;
        }
        LivingEntity target = getTarget();
        if (target != null && target.isAlive() && hasLineOfSight(target)) {
            double distance = distanceTo(target);
            KrampusSettings settings = settings();
            if (sackCooldown == 0 && distance <= SACK_REACH && canSack(target)) {
                sack(level, target);
            } else if (lashCooldown == 0 && distance >= settings.lashMinRange() && distance <= settings.lashRange()) {
                lash(level, target);
            }
        }
        int due = KrampusSettings.summonsDue(getHealth() / getMaxHealth());
        while (summons < due) {
            summons++;
            summonGoblins(level);
        }
    }

    /** The chain: hurts and pulls the target toward him. */
    public void lash(ServerLevel level, LivingEntity target) {
        lashCooldown = settings().lashCooldown();
        if (target.hurt(damageSources().mobAttack(this), (float) settings().lashDamage())) {
            Vec3 toward = new Vec3(getX() - target.getX(), 0.0, getZ() - target.getZ()).normalize();
            double resist = 1.0 - target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
            target.setDeltaMovement(target.getDeltaMovement().add(toward.x * PULL * resist, PULL_UP * resist, toward.z * PULL * resist));
            target.hurtMarked = true;
        }
        playSound(CozySounds.KRAMPUS_LASH.get(), 1.0F, 0.9F + getRandom().nextFloat() * 0.2F);
        level.broadcastEntityEvent(this, LASH_EVENT);
    }

    /** Players in survival or adventure, and any mob; never someone already in the sack. */
    public boolean canSack(LivingEntity target) {
        if (settings().sackTicks() == 0 || target.hasEffect(sacked()) || target.isPassenger()) {
            return false;
        }
        return !(target instanceof Player player) || Players.inPlay(player);
    }

    /** Into the sack: the target can't move for {@code sack_ticks}. */
    public void sack(ServerLevel level, LivingEntity target) {
        sackCooldown = settings().sackCooldown();
        int ticks = settings().sackTicks();
        if (target.addEffect(new MobEffectInstance(sacked(), ticks, 0, false, false, true), this)) {
            target.setDeltaMovement(Vec3.ZERO);
            target.hurtMarked = true;
            level.addFreshEntity(KrampusSack.over(target, ticks));
        }
        playSound(CozySounds.KRAMPUS_SACK.get(), 1.0F, 1.0F);
        level.broadcastEntityEvent(this, SACK_EVENT);
    }

    private static MobEffect sacked() {
        return CozyEffects.SACKED.get();
    }

    /** A roar, smoke, and {@code summon_goblins} present goblins at his side, after his target. */
    public void summonGoblins(ServerLevel level) {
        playSound(CozySounds.KRAMPUS_ROAR.get(), 2.0F, 1.0F);
        level.broadcastEntityEvent(this, ROAR_EVENT);
        for (int i = 0; i < settings().summonGoblins(); i++) {
            PresentGoblin goblin = CozyEntities.PRESENT_GOBLIN.get().create(level);
            BlockPos spot = summonSpot(level, goblin);
            if (goblin == null || spot == null) {
                continue;
            }
            goblin.moveTo(spot.getX() + 0.5, spot.getY(), spot.getZ() + 0.5, getRandom().nextFloat() * 360.0F, 0.0F);
            goblin.finalizeSpawn(level, level.getCurrentDifficultyAt(spot), MobSpawnType.MOB_SUMMONED, null, null);
            goblin.setTarget(getTarget());
            level.addFreshEntity(goblin);
            level.sendParticles(ParticleTypes.LARGE_SMOKE, goblin.getX(), goblin.getY() + 0.5, goblin.getZ(), 12, 0.3, 0.4, 0.3, 0.02);
        }
    }

    private @Nullable BlockPos summonSpot(ServerLevel level, @Nullable PresentGoblin goblin) {
        if (goblin == null) {
            return null;
        }
        for (int i = 0; i < SUMMON_TRIES; i++) {
            BlockPos spot = blockPosition().offset(getRandom().nextIntBetweenInclusive(-SUMMON_RADIUS, SUMMON_RADIUS), 0,
                    getRandom().nextIntBetweenInclusive(-SUMMON_RADIUS, SUMMON_RADIUS));
            goblin.setPos(spot.getX() + 0.5, spot.getY(), spot.getZ() + 0.5);
            if (level.noCollision(goblin) && !level.getBlockState(spot.below()).isAir()) {
                return spot;
            }
        }
        return blockPosition();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getEntity() instanceof ServerPlayer player) {
            fighters.add(player.getUUID());
        }
        return super.hurt(source, amount);
    }

    /** Everyone who fought him (and is still around) loses their bad karma; good karma stays. */
    @Override
    public void die(DamageSource source) {
        if (level() instanceof ServerLevel level && !isRemoved() && !dead) {
            if (source.getEntity() instanceof ServerPlayer killer) {
                fighters.add(killer.getUUID());
            }
            if (settings().clearsKarma()) {
                for (ServerPlayer player : fightersAround(level)) {
                    if (KarmaManager.clearBadKarma(player) < 0) {
                        player.sendSystemMessage(Component.translatable("message.cozychristmas.krampus.karma_cleared"));
                    }
                }
            }
        }
        super.die(source);
    }

    /** Fighters still online and near, as players. */
    public List<ServerPlayer> fightersAround(ServerLevel level) {
        List<ServerPlayer> players = new ArrayList<>();
        for (UUID id : fighters) {
            if (level.getPlayerByUUID(id) instanceof ServerPlayer player && player.distanceTo(this) <= FIGHTER_RANGE) {
                players.add(player);
            }
        }
        return players;
    }

    // --- visits ---------------------------------------------------------------------------------------------------

    /** He came for {@code player}: goes straight for them and leaves at dawn. */
    public void visit(ServerPlayer player) {
        visiting = player.getUUID();
        setTarget(player);
    }

    public boolean isVisiting() {
        return visiting != null;
    }

    private boolean shouldLeave(ServerLevel level) {
        if (level.isDay()) {
            return true;
        }
        Player player = level.getPlayerByUUID(visiting);
        if (player == null || !player.isAlive() || player.distanceTo(this) > VISIT_RANGE) {
            return true;
        }
        if (getTarget() == null && Players.inPlay(player)) {
            setTarget(player);
        }
        return false;
    }

    /** Gone in a puff of smoke, chains rattling. */
    public void vanish(ServerLevel level) {
        level.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + getBbHeight() / 2, getZ(), VANISH_PARTICLES,
                getBbWidth() / 2, getBbHeight() / 3, getBbWidth() / 2, 0.02);
        level.playSound(null, getX(), getY(), getZ(), CozySounds.KRAMPUS_CHAINS.get(), SoundSource.HOSTILE, 1.5F, 0.8F);
        discard();
    }

    /** A spot on the ground {@code min}..{@code max} blocks from {@code center}, where he fits, or null. */
    public static @Nullable Vec3 visitSpot(ServerLevel level, Krampus krampus, BlockPos center, int min, int max) {
        for (int i = 0; i < SUMMON_TRIES * 2; i++) {
            float angle = level.getRandom().nextFloat() * Mth.TWO_PI;
            double distance = Mth.lerp(level.getRandom().nextDouble(), min, max);
            BlockPos column = center.offset(Mth.floor(Mth.cos(angle) * distance), 0, Mth.floor(Mth.sin(angle) * distance));
            if (!level.isLoaded(column)) {
                continue;
            }
            BlockPos ground = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, column);
            Vec3 spot = Vec3.atBottomCenterOf(ground);
            krampus.setPos(spot);
            if (level.noCollision(krampus) && level.getFluidState(ground).isEmpty()
                    && !level.getBlockState(ground.below()).isAir()) {
                return spot;
            }
        }
        return null;
    }

    // --- boss bar, saving, client -------------------------------------------------------------------------------

    /**
     * The bar is for those near him or fighting him, not for everyone whose client tracks him: the lair is underground,
     * and a bar showing up over a mountain would give it away.
     */
    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        seenBy.add(player);
        updateBossBar(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        seenBy.remove(player);
        bossEvent.removePlayer(player);
    }

    private void updateBossBar(ServerPlayer player) {
        if (player.distanceTo(this) <= BOSS_BAR_RANGE || fighters.contains(player.getUUID())) {
            bossEvent.addPlayer(player);
        } else {
            bossEvent.removePlayer(player);
        }
    }

    /** Peaceful removes monsters; Krampus in his lair stays (he can't hurt anyone there), a visiting one goes. */
    @Override
    public void checkDespawn() {
        if (visiting == null && level().getDifficulty() == Difficulty.PEACEFUL) {
            return;
        }
        super.checkDespawn();
    }

    @Override
    public void setCustomName(@Nullable Component name) {
        super.setCustomName(name);
        bossEvent.setName(getDisplayName());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("summons", summons);
        UUIDUtil.CODEC.listOf().encodeStart(NbtOps.INSTANCE, List.copyOf(fighters)).result().ifPresent(list -> tag.put("fighters", list));
        if (visiting != null) {
            tag.putUUID("visiting", visiting);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        summons = tag.getInt("summons");
        fighters.clear();
        if (tag.contains("fighters")) {
            UUIDUtil.CODEC.listOf().parse(NbtOps.INSTANCE, tag.get("fighters")).resultOrPartial(error -> { }).ifPresent(fighters::addAll);
        }
        visiting = tag.hasUUID("visiting") ? tag.getUUID("visiting") : null;
        bossEvent.setName(getDisplayName());
    }

    @Override
    public void handleEntityEvent(byte id) {
        switch (id) {
            case LASH_EVENT -> lashTicks = LASH_TICKS;
            case SACK_EVENT -> sackTicks = SACK_TICKS;
            case ROAR_EVENT -> roarTicks = ROAR_TICKS;
            default -> super.handleEntityEvent(id);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (lashTicks > 0) {
            lashTicks--;
        }
        if (sackTicks > 0) {
            sackTicks--;
        }
        if (roarTicks > 0) {
            roarTicks--;
        }
    }

    /** Client: 1 as a move starts, down to 0 as it ends. */
    public float lashProgress(float partialTick) {
        return progress(lashTicks, LASH_TICKS, partialTick);
    }

    public float sackProgress(float partialTick) {
        return progress(sackTicks, SACK_TICKS, partialTick);
    }

    public float roarProgress(float partialTick) {
        return progress(roarTicks, ROAR_TICKS, partialTick);
    }

    private static float progress(int ticks, int length, float partialTick) {
        return ticks <= 0 ? 0.0F : Math.max(0.0F, (ticks - partialTick) / length);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return CozySounds.KRAMPUS_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return CozySounds.KRAMPUS_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return CozySounds.KRAMPUS_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
        playSound(CozySounds.KRAMPUS_STEP.get(), 0.5F, 1.0F);
    }

    @Override
    public int getAmbientSoundInterval() {
        return 160;
    }
}
