package dev.eliasnvx.cozychristmas.client.sleigh;

import dev.eliasnvx.cozychristmas.client.reindeer.ReindeerRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/** Santa's sleigh as drawn, with the four reindeer of its team (made once, reused every frame). */
public final class SleighRenderState extends EntityRenderState {

    public static final int TEAM = 4;

    public float yRot;
    /** How full the sack is, 0 (no sack) to 1. */
    public float load;
    public boolean harnessed;
    public boolean flying;
    public final ReindeerRenderState[] team = new ReindeerRenderState[TEAM];

    public SleighRenderState() {
        for (int i = 0; i < TEAM; i++) {
            team[i] = new ReindeerRenderState();
            team[i].saddled = true; // the harness: saddle and bell collar
        }
    }
}
