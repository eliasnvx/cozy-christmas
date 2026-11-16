package dev.eliasnvx.cozychristmas.food;

import dev.eliasnvx.cozychristmas.registry.CozyEffects;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** A tangerine: plain food, except around midnight ({@link Tangerine}). The tooltip says nothing about it. */
public class TangerineItem extends Item {

    public TangerineItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (level instanceof ServerLevel && Tangerine.isMidnight(level.getOverworldClockTime())) {
            ringIn(entity);
        }
        return super.finishUsingItem(stack, level, entity);
    }

    /** The bell strikes twelve for {@code entity}, unless it is already striking. */
    public static void ringIn(LivingEntity entity) {
        if (!entity.hasEffect(CozyEffects.CHIMES.asHolder())) {
            entity.addEffect(new MobEffectInstance(CozyEffects.CHIMES.asHolder(), Tangerine.CHIMES_DURATION, 0,
                    false, false, false));
        }
    }
}
