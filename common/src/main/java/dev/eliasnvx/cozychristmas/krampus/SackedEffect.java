package dev.eliasnvx.cozychristmas.krampus;

import dev.eliasnvx.cozychristmas.CozyChristmas;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * In Krampus's sack (SPEC: "puts you in his sack, 2 seconds unable to move"): no walking, no jumping. The sack
 * itself is a {@link KrampusSack} entity over the victim, so everyone sees it; the victim sees burlap.
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
}
