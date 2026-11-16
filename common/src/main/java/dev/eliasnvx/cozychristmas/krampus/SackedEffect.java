package dev.eliasnvx.cozychristmas.krampus;

import dev.eliasnvx.cozychristmas.CozyChristmas;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

/**
 * In Krampus's sack (SPEC: "puts you in his sack, 2 seconds unable to move"): no walking, no jumping, no getting on
 * a mount or a boat, no gliding off. The sack itself is a {@link KrampusSack} entity over the victim, so everyone sees
 * it; the victim sees burlap.
 */
public class SackedEffect extends MobEffect {

    public static final int COLOR = 0x8A6A3C;

    public SackedEffect() {
        super(MobEffectCategory.HARMFUL, COLOR);
        addAttributeModifier(Attributes.MOVEMENT_SPEED, CozyChristmas.id("sacked_speed"), -1.0,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        addAttributeModifier(Attributes.JUMP_STRENGTH, CozyChristmas.id("sacked_jump"), -1.0,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (!(entity.level() instanceof ServerLevel)) {
            return true; // 1.21.1 ticks effects on the client too: the server decides, riding and gliding are synced
        }
        if (entity.isPassenger()) {
            entity.stopRiding();
        }
        // on 1.21.1 only players glide (the elytra flag is set by the player's packet), and only they can stop it
        if (entity instanceof Player player && player.isFallFlying()) {
            player.stopFallFlying();
        }
        return true;
    }
}
