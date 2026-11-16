package dev.eliasnvx.cozychristmas.client.cosmetic;

import net.minecraft.client.player.AbstractClientPlayer;

/**
 * What the cosmetic models read about the wearer this frame. 1.20.1 has no render states, so the cosmetic layer fills
 * one reusable instance per draw (same field names as the 26.x render state, so the models stay alike across versions).
 * Ported from Catgirl &amp; Catboy Mod.
 */
public final class PlayerPose {

    /** {@code AbstractClientPlayer#getModelName} of slim-armed skins. */
    public static final String SLIM = "slim";

    public float ageInTicks;
    public float walkAnimationPos;
    public float walkAnimationSpeed;
    public boolean hasRedOverlay;
    public boolean slim;
    public CosmeticRenderData cosmetics = CosmeticRenderData.STILL;

    public void capture(AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float ageInTicks) {
        this.ageInTicks = ageInTicks;
        this.walkAnimationPos = limbSwing;
        this.walkAnimationSpeed = limbSwingAmount;
        // the same check as LivingEntityRenderer's red hurt tint
        this.hasRedOverlay = player.hurtTime > 0 || player.deathTime > 0;
        this.slim = SLIM.equals(player.getModelName());
        this.cosmetics = CosmeticRenderData.STILL;
    }
}
