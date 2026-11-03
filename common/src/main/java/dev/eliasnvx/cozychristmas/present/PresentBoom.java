package dev.eliasnvx.cozychristmas.present;

import dev.eliasnvx.cozychristmas.config.CozyConfig;
import dev.eliasnvx.cozychristmas.registry.CozyDamageTypes;
import dev.eliasnvx.cozychristmas.registry.CozyParticles;
import dev.eliasnvx.cozychristmas.registry.CozySounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * The prank: a vanilla explosion (so knockback, blast protection, cover and creative mode work as players know them)
 * with our damage curve ({@link BoomSettings}), no block damage unless the config says so, and confetti and snow
 * instead of smoke. No flash.
 */
public final class PresentBoom {

    private static final int CONFETTI = 90;
    private static final double CONFETTI_SPREAD = 0.2;
    private static final double CONFETTI_SPEED = 0.45;
    private static final int SNOWFLAKES = 40;
    private static final double SNOW_SPREAD = 0.7;
    private static final double SNOW_SPEED = 0.08;
    private static final int SNOW_PUFFS = 8;
    private static final double PUFF_SPREAD = 0.4;

    private PresentBoom() {
    }

    /** @param prankster who wrapped it (named in death messages), null for a present found in the world */
    public static void explode(ServerLevel level, Vec3 center, @Nullable Entity prankster) {
        BoomSettings boom = CozyConfig.common().presents().boom();
        DamageSource source = new DamageSource(level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE)
                .getOrThrow(prankster == null ? CozyDamageTypes.PRESENT_BOOM : CozyDamageTypes.PRESENT_PRANK), null, prankster);
        // vanilla reaches entities within twice the explosion radius
        float radius = (float) (boom.radius() / 2.0);
        level.explode(null, source, new Calculator(boom), center.x, center.y, center.z, radius, false,
                boom.breaksBlocks() ? Level.ExplosionInteraction.BLOCK : Level.ExplosionInteraction.NONE,
                CozyParticles.CONFETTI.get(), CozyParticles.CONFETTI.get(), WeightedList.of(),
                BuiltInRegistries.SOUND_EVENT.wrapAsHolder(CozySounds.PRESENT_BOOM.get()));

        level.sendParticles(CozyParticles.CONFETTI.get(), center.x, center.y, center.z, CONFETTI,
                CONFETTI_SPREAD, CONFETTI_SPREAD, CONFETTI_SPREAD, CONFETTI_SPEED);
        level.sendParticles(ParticleTypes.SNOWFLAKE, center.x, center.y, center.z, SNOWFLAKES,
                SNOW_SPREAD, SNOW_SPREAD, SNOW_SPREAD, SNOW_SPEED);
        level.sendParticles(ParticleTypes.POOF, center.x, center.y, center.z, SNOW_PUFFS,
                PUFF_SPREAD, PUFF_SPREAD, PUFF_SPREAD, SNOW_SPEED);
        level.playSound(null, center.x, center.y, center.z, CozySounds.PRESENT_CONFETTI.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    private static final class Calculator extends ExplosionDamageCalculator {

        private final BoomSettings boom;

        Calculator(BoomSettings boom) {
            this.boom = boom;
        }

        @Override
        public float getEntityDamageAmount(Explosion explosion, Entity entity, float seenPercent) {
            return boom.damageAt(entity.position().distanceTo(explosion.center())) * seenPercent;
        }

        @Override
        public float getKnockbackMultiplier(Entity entity) {
            return boom.knockback();
        }

        @Override
        public boolean shouldBlockExplode(Explosion explosion, BlockGetter level, BlockPos pos, BlockState state, float power) {
            return boom.breaksBlocks() && super.shouldBlockExplode(explosion, level, pos, state, power);
        }
    }
}
