package dev.eliasnvx.cozychristmas.snowman;

import dev.eliasnvx.cozychristmas.config.CozyConfig;
import dev.eliasnvx.cozychristmas.registry.CozyItems;
import dev.eliasnvx.cozychristmas.registry.CozySounds;
import dev.eliasnvx.cozychristmas.registry.CozyTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

/**
 * An evil snowman: comes out at night in snowy biomes and pelts players (and the good snow golems) with ice snowballs
 * that slow and chill ({@link IceSnowball}). It's snow: it melts. Fire hurts it several times over (burning, lava, warm
 * biomes like a snow golem), and so does standing next to a lit fire, campfire or fireplace
 * ({@code #cozychristmas:melts_snowmen}), so a fire at home keeps them off the doorstep. Water hurts it like it hurts snow
 * golems. Settings: {@link EvilSnowmanSettings}.
 */
public class EvilSnowman extends Monster implements RangedAttackMob {

    /** Client: the throwing arm swings for this many ticks after a throw. */
    public static final int THROW_TICKS = 8;
    private static final byte THROW_EVENT = EntityEvent.START_ATTACKING;
    private static final int HEAT_CHECK_INTERVAL = 20;
    private static final float HEAT_DAMAGE = 1.0F;
    private static final float BIOME_MELT_DAMAGE = 1.0F;
    private static final double FOLLOW_MARGIN = 4.0;
    private static final float THROW_POWER = 1.5F;
    private static final float THROW_INACCURACY = 8.0F;
    private static final double ARC = 0.2;
    private static final float LOOK_DISTANCE = 8.0F;
    private static final int MELT_PARTICLES = 6;
    private static final double TRAIL_OFFSET = 0.25;

    private int throwTicks;

    public EvilSnowman(EntityType<? extends EvilSnowman> type, Level level) {
        super(type, level);
    }

    private static EvilSnowmanSettings settings() {
        return CozyConfig.common().evilSnowman();
    }

    public static AttributeSupplier.Builder createAttributes(EvilSnowmanSettings settings) {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, settings.health())
                .add(Attributes.MOVEMENT_SPEED, settings.speed())
                .add(Attributes.FOLLOW_RANGE, settings.throwRange() + FOLLOW_MARGIN);
    }

    /** Monster rules (dark enough, not peaceful) under the open sky: no snowmen in caves. */
    public static boolean checkEvilSnowmanSpawnRules(EntityType<? extends Monster> type, ServerLevelAccessor level,
                                                     MobSpawnType reason, BlockPos pos, RandomSource random) {
        // 26.3's Monster#checkSurfaceMonstersSpawnRules, which 1.21.1 doesn't have
        return Monster.checkMonsterSpawnRules(type, level, reason, pos, random)
                && (MobSpawnType.isSpawner(reason) || level.canSeeSky(pos));
    }

    @Override
    protected void registerGoals() {
        EvilSnowmanSettings settings = settings();
        goalSelector.addGoal(1, new RangedAttackGoal(this, 1.0, settings.throwInterval(), settings.maxThrowInterval(),
                (float) settings.throwRange()));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, LOOK_DISTANCE));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, SnowGolem.class, true));
    }

    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        if (!(level() instanceof ServerLevel server)) {
            return;
        }
        double dx = target.getX() - getX();
        double dz = target.getZ() - getZ();
        double arc = Math.sqrt(dx * dx + dz * dz) * ARC;
        IceSnowball projectile = new IceSnowball(server, this, new ItemStack(CozyItems.ICE_SNOWBALL.get()));
        projectile.shoot(dx, target.getEyeY() - 1.1 + arc - projectile.getY(), dz, THROW_POWER, THROW_INACCURACY);
        server.addFreshEntity(projectile);
        playSound(CozySounds.EVIL_SNOWMAN_THROW.get(), 1.0F, 0.4F / (getRandom().nextFloat() * 0.4F + 0.8F));
        server.broadcastEntityEvent(this, THROW_EVENT);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == THROW_EVENT) {
            throwTicks = THROW_TICKS;
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (throwTicks > 0) {
            throwTicks--;
        }
    }

    /** Client: 1 right after a throw, down to 0 when the arm is back. */
    public float throwProgress(float partialTick) {
        return throwTicks <= 0 ? 0.0F : Math.max(0.0F, (throwTicks - partialTick) / THROW_TICKS);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level() instanceof ServerLevel server && isAlive()) {
            if (server.getBiome(blockPosition()).is(BiomeTags.SNOW_GOLEM_MELTS)) {
                hurt(damageSources().onFire(), BIOME_MELT_DAMAGE);
            } else if ((tickCount + getId()) % HEAT_CHECK_INTERVAL == 0 && nearHeat(server)) {
                hurt(damageSources().inFire(), HEAT_DAMAGE);
            }
            if (settings().leavesSnow() && server.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                leaveSnow(server);
            }
        }
    }

    /** Like the snow golem: a snow layer on every free block under its four corners that can hold one. */
    private void leaveSnow(ServerLevel level) {
        BlockState snow = Blocks.SNOW.defaultBlockState();
        for (int i = 0; i < 4; i++) {
            BlockPos pos = new BlockPos(Mth.floor(getX() + (i % 2 * 2 - 1) * TRAIL_OFFSET), Mth.floor(getY()),
                    Mth.floor(getZ() + (i / 2 % 2 * 2 - 1) * TRAIL_OFFSET));
            if (level.getBlockState(pos).isAir() && snow.canSurvive(level, pos)) {
                level.setBlockAndUpdate(pos, snow);
                level.gameEvent(GameEvent.BLOCK_PLACE, pos, GameEvent.Context.of(this, snow));
            }
        }
    }

    /** A lit {@code #cozychristmas:melts_snowmen} block within {@code heat_radius} (blocks without {@code lit} always count). */
    public boolean nearHeat(Level level) {
        int radius = settings().heatRadius();
        if (radius <= 0) {
            return false;
        }
        BlockPos center = blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -1, -radius), center.offset(radius, 2, radius))) {
            BlockState state = level.getBlockState(pos);
            if (state.is(CozyTags.MELTS_SNOWMEN) && state.getOptionalValue(BlockStateProperties.LIT).orElse(true)) {
                return true;
            }
        }
        return false;
    }

    /** Fire, lava and heat hurt {@code fire_multiplier} times as much, and the snowman drips. */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean fire = source.is(DamageTypeTags.IS_FIRE);
        boolean hurt = super.hurt(source, fire ? settings().fireDamage(amount) : amount);
        if (hurt && fire && level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.FALLING_WATER, getX(), getY() + getBbHeight() * 0.6, getZ(), MELT_PARTICLES,
                    getBbWidth() * 0.4, getBbHeight() * 0.25, getBbWidth() * 0.4, 0.0);
            playSound(CozySounds.EVIL_SNOWMAN_MELT.get(), 0.6F, 1.0F + (getRandom().nextFloat() - 0.5F) * 0.4F);
        }
        return hurt;
    }

    @Override
    public boolean isSensitiveToWater() {
        return true;
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return CozySounds.EVIL_SNOWMAN_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return CozySounds.EVIL_SNOWMAN_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return CozySounds.EVIL_SNOWMAN_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        playSound(CozySounds.EVIL_SNOWMAN_STEP.get(), 0.4F, 0.8F);
    }
}
