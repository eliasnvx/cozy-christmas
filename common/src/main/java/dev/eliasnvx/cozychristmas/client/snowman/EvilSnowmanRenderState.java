package dev.eliasnvx.cozychristmas.client.snowman;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public final class EvilSnowmanRenderState extends LivingEntityRenderState {

    /** 1 right after a throw, down to 0 when the arm is back. */
    public float throwProgress;
}
