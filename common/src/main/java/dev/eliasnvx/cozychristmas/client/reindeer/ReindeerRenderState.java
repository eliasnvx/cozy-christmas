package dev.eliasnvx.cozychristmas.client.reindeer;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/** A reindeer as drawn: saddled (saddle and bell collar show), flying (legs tucked like a leap). */
public class ReindeerRenderState extends LivingEntityRenderState {
    public boolean saddled;
    public boolean flying;
}
