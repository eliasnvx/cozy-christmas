package dev.eliasnvx.cozychristmas.snowman;

import dev.eliasnvx.cozychristmas.cold.Cold;
import dev.eliasnvx.cozychristmas.config.CozyConfig;
import dev.eliasnvx.cozychristmas.registry.CozyEntities;
import dev.eliasnvx.cozychristmas.registry.CozyItems;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import dev.eliasnvx.cozychristmas.util.Players;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * A snowball with ice packed in: it hurts, slows and chills ({@link IceSnowballSettings}). Evil snowmen throw them,
 * players and dispensers can too. Snowmen don't hurt each other with them.
 */
public class IceSnowball extends ThrowableItemProjectile {

    private static final byte HIT_EVENT = EntityEvent.DEATH;
    private static final int HIT_PARTICLES = 8;

    public IceSnowball(EntityType<? extends IceSnowball> type, Level level) {
        super(type, level);
    }

    public IceSnowball(Level level, LivingEntity thrower, ItemStack stack) {
        super(CozyEntities.ICE_SNOWBALL.get(), thrower, level, stack);
    }

    public IceSnowball(Level level, double x, double y, double z, ItemStack stack) {
        super(CozyEntities.ICE_SNOWBALL.get(), x, y, z, level, stack);
    }

    @Override
    protected Item getDefaultItem() {
        return CozyItems.ICE_SNOWBALL.get();
    }

    /** Evil snowmen's ice flies through other evil snowmen. */
    @Override
    protected boolean canHitEntity(Entity target) {
        return super.canHitEntity(target) && !(getOwner() instanceof EvilSnowman && target instanceof EvilSnowman);
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        super.onHitEntity(hit);
        if (!(level() instanceof ServerLevel server)) {
            return;
        }
        IceSnowballSettings settings = CozyConfig.common().evilSnowman().iceSnowball();
        Entity target = hit.getEntity();
        boolean hurt = target.hurtServer(server, damageSources().thrown(this, getOwner()), (float) settings.damage());
        // with no damage set, "hurt" is always false: then hit whoever could have been hurt (not creative players)
        boolean struck = hurt || settings.damage() == 0.0 && !(target instanceof Player player && !Players.inPlay(player));
        if (struck && target instanceof LivingEntity living) {
            if (settings.slownessTicks() > 0) {
                living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, settings.slownessTicks(), settings.slownessAmplifier()),
                        getEffectSource());
            }
            Cold.chill(living, settings.chillTicks());
        }
    }

    @Override
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (!level().isClientSide()) {
            level().playSound(null, getX(), getY(), getZ(), SoundEvents.GLASS_BREAK, SoundSource.NEUTRAL, 0.3F,
                    1.6F + random.nextFloat() * 0.3F);
            level().broadcastEntityEvent(this, HIT_EVENT);
            discard();
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == HIT_EVENT) {
            ItemStack item = getItem();
            ParticleOptions particle = item.isEmpty() ? ParticleTypes.ITEM_SNOWBALL
                    : new ItemParticleOption(ParticleTypes.ITEM, ItemStackTemplate.fromNonEmptyStack(item));
            for (int i = 0; i < HIT_PARTICLES; i++) {
                level().addParticle(particle, getX(), getY(), getZ(), 0.0, 0.0, 0.0);
            }
        } else {
            super.handleEntityEvent(id);
        }
    }
}
