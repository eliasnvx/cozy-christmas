package dev.eliasnvx.cozychristmas.food;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * Warmed through by hot cocoa (SPEC: "warms: removes freezing"). Thaws whatever freezing there is at once, and while
 * it lasts {@link dev.eliasnvx.cozychristmas.cold.Cold#isKeptWarm} is true: powder snow and ice snowballs can't
 * freeze the drinker, like the Toasty set.
 */
public class WarmEffect extends MobEffect {

    public static final int COLOR = 0xE08A3C;

    public WarmEffect() {
        super(MobEffectCategory.BENEFICIAL, COLOR);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
        if (entity.getTicksFrozen() > 0) {
            entity.setTicksFrozen(0);
        }
        return true;
    }
}
