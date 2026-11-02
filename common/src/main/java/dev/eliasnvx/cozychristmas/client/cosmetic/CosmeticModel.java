package dev.eliasnvx.cozychristmas.client.cosmetic;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

/**
 * A baked {@link CosmeticShape}: poses like the wearer, and swings the scarf's back end. Posed when the frame is drawn
 * (submissions are deferred), from the state it was submitted with, so one instance serves every player.
 */
public final class CosmeticModel extends PlayerModel {

    /** The back end hangs a little away from the back even when standing still. */
    private static final float TAIL_REST = 0.12F;
    /** How far it lifts behind a running player. */
    private static final float TAIL_LIFT = 1.0F;
    private static final float FLUTTER = 0.08F;
    private static final float FLUTTER_SPEED = 0.6F;

    private final @Nullable ModelPart scarfTail;

    public CosmeticModel(ModelPart root, boolean slim) {
        super(root, slim);
        ModelPart body = root.getChild("body");
        this.scarfTail = body.hasChild(CosmeticShape.SCARF_TAIL) ? body.getChild(CosmeticShape.SCARF_TAIL) : null;
    }

    /**
     * Also hides the skin's outer layer parts: cosmetics never draw there, and mods that put the skin's 3D layers into
     * every player model (3D Skin Layers) would otherwise draw the wearer's outer skin with the knit texture.
     */
    @Override
    public void setupAnim(AvatarRenderState state) {
        super.setupAnim(state);
        hat.visible = false;
        jacket.visible = false;
        leftSleeve.visible = false;
        rightSleeve.visible = false;
        leftPants.visible = false;
        rightPants.visible = false;
        if (scarfTail != null) {
            CosmeticRenderData data = CosmeticRenderData.of(state);
            float walk = data.walkAmount();
            float flutter = Mth.sin(state.ageInTicks * FLUTTER_SPEED + data.phase()) * FLUTTER * walk;
            scarfTail.xRot = TAIL_REST + walk * TAIL_LIFT + flutter;
        }
    }
}
