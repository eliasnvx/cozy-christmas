package dev.eliasnvx.cozychristmas.food;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * The bell striking twelve after a tangerine at midnight ({@link Tangerine}). Given hidden (no particles, no icon):
 * the only sign is the sound, low and slow like a big tower bell.
 */
public class ChimesEffect extends MobEffect {

    public static final int COLOR = 0xF28C28;
    private static final float VOLUME = 2.0F;
    private static final float PITCH = 0.5F;

    public ChimesEffect() {
        super(MobEffectCategory.NEUTRAL, COLOR);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return Tangerine.strikesAt(duration);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (!(entity.level() instanceof ServerLevel level)) {
            return true;
        }
        level.playSound(null, entity.getX(), entity.getEyeY(), entity.getZ(), SoundEvents.BELL_BLOCK, SoundSource.AMBIENT,
                VOLUME, PITCH);
        return true;
    }
}
