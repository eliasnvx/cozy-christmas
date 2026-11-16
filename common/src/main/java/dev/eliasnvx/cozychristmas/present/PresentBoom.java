package dev.eliasnvx.cozychristmas.present;

import dev.eliasnvx.cozychristmas.config.CozyConfig;
import dev.eliasnvx.cozychristmas.registry.CozyDamageTypes;
import dev.eliasnvx.cozychristmas.registry.CozyParticles;
import dev.eliasnvx.cozychristmas.registry.CozySounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * The prank: a vanilla explosion as players know it (knockback, blast protection, cover, creative mode) with our
 * damage curve ({@link BoomSettings}), no block damage unless the config says so, and confetti and snow instead of
 * smoke. No flash.
 *
 * <p>1.20.1's explosion has no hooks for entity damage, knockback or its particles and sound, so this does vanilla's
 * steps itself: the entity pass of {@code Explosion#explode} with our damage, and, only if blocks break, vanilla's
 * block rays handed to {@link Explosion#finalizeExplosion} (drops, decay, chain reactions as usual).
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
    /** Vanilla's explosion sound: heard far, a little lower and varied. */
    private static final float BOOM_VOLUME = 4.0F;
    private static final float BOOM_PITCH = 0.7F;
    private static final float BOOM_PITCH_SPREAD = 0.2F;
    /** Vanilla's block rays: a 16x16x16 grid of directions, each with a power of 0.7-1.3 times the radius. */
    private static final int RAY_GRID = 16;
    private static final float RAY_POWER_MIN = 0.7F;
    private static final float RAY_POWER_SPREAD = 0.6F;
    private static final float RAY_STEP = 0.3F;
    private static final float RAY_DECAY = 0.22500001F;
    private static final ExplosionDamageCalculator VANILLA_RESISTANCE = new ExplosionDamageCalculator();

    private PresentBoom() {
    }

    /** @param prankster who wrapped it (named in death messages), null for a present found in the world */
    public static void explode(ServerLevel level, Vec3 center, @Nullable Entity prankster) {
        BoomSettings boom = CozyConfig.common().presents().boom();
        DamageSource source = new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(prankster == null ? CozyDamageTypes.PRESENT_BOOM : CozyDamageTypes.PRESENT_PRANK), null, prankster);
        level.gameEvent(null, GameEvent.EXPLODE, center);
        hitEntities(level, center, source, boom);
        if (boom.breaksBlocks()) {
            // vanilla reaches entities within twice the explosion radius: the blocks get half of ours
            breakBlocks(level, center, (float) (boom.radius() / 2.0));
        }

        RandomSource random = level.getRandom();
        level.playSound(null, center.x, center.y, center.z, CozySounds.PRESENT_BOOM.get(), SoundSource.BLOCKS, BOOM_VOLUME,
                (1.0F + (random.nextFloat() - random.nextFloat()) * BOOM_PITCH_SPREAD) * BOOM_PITCH);
        level.sendParticles(CozyParticles.CONFETTI.get(), center.x, center.y, center.z, CONFETTI,
                CONFETTI_SPREAD, CONFETTI_SPREAD, CONFETTI_SPREAD, CONFETTI_SPEED);
        level.sendParticles(ParticleTypes.SNOWFLAKE, center.x, center.y, center.z, SNOWFLAKES,
                SNOW_SPREAD, SNOW_SPREAD, SNOW_SPREAD, SNOW_SPEED);
        level.sendParticles(ParticleTypes.POOF, center.x, center.y, center.z, SNOW_PUFFS,
                PUFF_SPREAD, PUFF_SPREAD, PUFF_SPREAD, SNOW_SPEED);
        level.playSound(null, center.x, center.y, center.z, CozySounds.PRESENT_CONFETTI.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    /** Vanilla's entity pass: damage by our curve and cover, a push away from the present scaled by the config. */
    private static void hitEntities(ServerLevel level, Vec3 center, DamageSource source, BoomSettings boom) {
        double reach = boom.radius();
        AABB area = new AABB(center.x - reach - 1.0, center.y - reach - 1.0, center.z - reach - 1.0,
                center.x + reach + 1.0, center.y + reach + 1.0, center.z + reach + 1.0);
        List<Entity> entities = level.getEntities((Entity) null, area);
        for (Entity entity : entities) {
            if (entity.ignoreExplosion()) {
                continue;
            }
            double distance = Math.sqrt(entity.distanceToSqr(center));
            double scaled = distance / reach;
            if (scaled > 1.0) {
                continue;
            }
            double dx = entity.getX() - center.x;
            double dy = (entity instanceof PrimedTnt ? entity.getY() : entity.getEyeY()) - center.y;
            double dz = entity.getZ() - center.z;
            double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (length == 0.0) {
                continue;
            }
            float seen = Explosion.getSeenPercent(center, entity);
            if (!isSpared(entity)) {
                entity.hurt(source, boom.damageAt(distance) * seen);
            }
            double push = (1.0 - scaled) * seen * boom.knockback();
            if (entity instanceof LivingEntity living) {
                push = ProtectionEnchantment.getExplosionKnockbackAfterDampener(living, push);
            }
            Vec3 knockback = new Vec3(dx / length * push, dy / length * push, dz / length * push);
            if (entity instanceof Player player) {
                // players move on their client: vanilla sends them the push, unless they fly in creative or spectate
                if (player.isSpectator() || player.isCreative() && player.getAbilities().flying) {
                    continue;
                }
                player.hurtMarked = true;
            }
            entity.setDeltaMovement(entity.getDeltaMovement().add(knockback));
        }
    }

    /** A prank goes off in faces: it spares items on the ground, armor stands, frames and paintings. */
    private static boolean isSpared(Entity entity) {
        return entity instanceof ItemEntity || entity instanceof ArmorStand || entity instanceof HangingEntity;
    }

    /** Only with {@code presents.boom.breaks_blocks}: vanilla's block rays, then vanilla breaks and drops them. */
    private static void breakBlocks(ServerLevel level, Vec3 center, float power) {
        Explosion.BlockInteraction interaction = level.getGameRules().getBoolean(GameRules.RULE_BLOCK_EXPLOSION_DROP_DECAY)
                ? Explosion.BlockInteraction.DESTROY_WITH_DECAY
                : Explosion.BlockInteraction.DESTROY;
        Explosion explosion = new Explosion(level, null, center.x, center.y, center.z, power, false, interaction, List.of());
        RandomSource random = level.getRandom();
        Set<BlockPos> blown = new HashSet<>();
        int last = RAY_GRID - 1;
        for (int i = 0; i < RAY_GRID; i++) {
            for (int j = 0; j < RAY_GRID; j++) {
                for (int k = 0; k < RAY_GRID; k++) {
                    if (i != 0 && i != last && j != 0 && j != last && k != 0 && k != last) {
                        continue; // only the grid's surface: one ray per direction
                    }
                    double rx = i / (float) last * 2.0F - 1.0F;
                    double ry = j / (float) last * 2.0F - 1.0F;
                    double rz = k / (float) last * 2.0F - 1.0F;
                    double length = Math.sqrt(rx * rx + ry * ry + rz * rz);
                    rx /= length;
                    ry /= length;
                    rz /= length;
                    float strength = power * (RAY_POWER_MIN + random.nextFloat() * RAY_POWER_SPREAD);
                    double x = center.x;
                    double y = center.y;
                    double z = center.z;
                    while (strength > 0.0F) {
                        BlockPos pos = BlockPos.containing(x, y, z);
                        if (!level.isInWorldBounds(pos)) {
                            break;
                        }
                        BlockState state = level.getBlockState(pos);
                        FluidState fluid = level.getFluidState(pos);
                        Optional<Float> resistance = VANILLA_RESISTANCE.getBlockExplosionResistance(explosion, level, pos, state, fluid);
                        if (resistance.isPresent()) {
                            strength -= (resistance.get() + RAY_STEP) * RAY_STEP;
                        }
                        if (strength > 0.0F && VANILLA_RESISTANCE.shouldBlockExplode(explosion, level, pos, state, strength)) {
                            blown.add(pos);
                        }
                        x += rx * RAY_STEP;
                        y += ry * RAY_STEP;
                        z += rz * RAY_STEP;
                        strength -= RAY_DECAY;
                    }
                }
            }
        }
        explosion.getToBlow().addAll(blown);
        explosion.finalizeExplosion(false); // false: no vanilla smoke; the sound is client-only there
    }
}
