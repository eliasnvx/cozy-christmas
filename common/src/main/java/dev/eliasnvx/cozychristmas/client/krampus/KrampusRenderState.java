package dev.eliasnvx.cozychristmas.client.krampus;

import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

public final class KrampusRenderState extends HumanoidRenderState {

    /** Each 1 as the move starts, down to 0 as it ends. */
    public float lashProgress;
    public float sackProgress;
    public float roarProgress;
}
