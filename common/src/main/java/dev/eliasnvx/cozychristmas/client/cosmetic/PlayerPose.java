package dev.eliasnvx.cozychristmas.client.cosmetic;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.PlayerSkin;

/**
 * What the cosmetic models read about the wearer this frame. 1.21.1 has no render states, so the cosmetic layer fills
 * one reusable instance per draw (same field names as the 26.x render state, so the models stay alike across versions).
 * Ported from Catgirl &amp; Catboy Mod.
 */
public final class PlayerPose {

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
        this.slim = player.getSkin().model() == PlayerSkin.Model.SLIM;
        this.cosmetics = CosmeticRenderData.STILL;
    }
}
