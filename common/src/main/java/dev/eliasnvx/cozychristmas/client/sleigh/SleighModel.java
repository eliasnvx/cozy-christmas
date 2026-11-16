package dev.eliasnvx.cozychristmas.client.sleigh;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.rendertype.RenderTypes;

/**
 * Santa's sleigh: a red body with gold trim on gold runners curling up at the front, a green seat at the back, the
 * sack of presents in the middle (growing as it's loaded) and, while the team is harnessed, the shaft running forward
 * between the reindeer. Forward is -z. Texture 256x128: one patch per material, shared by every box of it; the patches
 * are painted by {@code tools/gen_sleigh.py}.
 */
public final class SleighModel extends EntityModel<SleighRenderState> {

    private static final int RED_U = 0;
    private static final int RED_V = 0;
    private static final int GOLD_U = 104;
    private static final int GOLD_V = 0;
    private static final int GREEN_U = 188;
    private static final int GREEN_V = 0;
    private static final int SACK_U = 188;
    private static final int SACK_V = 14;
    private static final int RIBBON_U = 188;
    private static final int RIBBON_V = 40;
    private static final int WOOD_U = 0;
    private static final int WOOD_V = 44;
    /** The sack's size empty-ish and full. */
    private static final float SACK_MIN = 0.45F;

    private final ModelPart sack;
    private final ModelPart shaft;

    public SleighModel(ModelPart root) {
        super(root, RenderTypes::entityCutout);
        sack = root.getChild("sack");
        shaft = root.getChild("shaft");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        CubeListBuilder body = CubeListBuilder.create()
                .texOffs(RED_U, RED_V).addBox(-10.0F, -8.0F, -14.0F, 20, 2, 32)      // floor
                .texOffs(RED_U, RED_V).addBox(9.0F, -14.0F, -14.0F, 2, 8, 32)        // left side
                .texOffs(RED_U, RED_V).addBox(-11.0F, -14.0F, -14.0F, 2, 8, 32)      // right side
                .texOffs(RED_U, RED_V).addBox(-10.0F, -12.0F, -16.0F, 20, 6, 2)      // front
                .texOffs(RED_U, RED_V).addBox(-10.0F, -18.0F, 16.0F, 20, 12, 2)      // backrest
                .texOffs(GREEN_U, GREEN_V).addBox(-8.0F, -10.0F, 6.0F, 16, 2, 9)     // seat
                .texOffs(GOLD_U, GOLD_V).addBox(9.0F, -15.0F, -14.0F, 2, 1, 32)      // trim
                .texOffs(GOLD_U, GOLD_V).addBox(-11.0F, -15.0F, -14.0F, 2, 1, 32)
                .texOffs(GOLD_U, GOLD_V).addBox(-10.0F, -19.0F, 16.0F, 20, 1, 2)
                .texOffs(GOLD_U, GOLD_V).addBox(-10.0F, -13.0F, -16.0F, 20, 1, 2);
        for (int side = -1; side <= 1; side += 2) {
            float x = side < 0 ? -11.0F : 9.0F;
            body.texOffs(GOLD_U, GOLD_V).addBox(x, -2.0F, -20.0F, 2, 2, 38)          // runner
                    .texOffs(GOLD_U, GOLD_V).addBox(x, -10.0F, -22.0F, 2, 10, 2)      // the curl up at the front
                    .texOffs(GOLD_U, GOLD_V).addBox(x, -12.0F, -22.0F, 2, 2, 6)
                    .texOffs(WOOD_U, WOOD_V).addBox(x, -6.0F, -10.0F, 2, 4, 2)        // struts
                    .texOffs(WOOD_U, WOOD_V).addBox(x, -6.0F, 10.0F, 2, 4, 2);
        }
        root.addOrReplaceChild("body", body, PartPose.offset(0.0F, 24.0F, 0.0F));
        root.addOrReplaceChild("sack", CubeListBuilder.create()
                        .texOffs(SACK_U, SACK_V).addBox(-6.0F, -12.0F, -6.0F, 12, 12, 12)
                        .texOffs(RIBBON_U, RIBBON_V).addBox(-2.0F, -15.0F, -2.0F, 4, 3, 4),
                PartPose.offset(0.0F, 16.0F, -2.0F));
        root.addOrReplaceChild("shaft", CubeListBuilder.create()
                        .texOffs(WOOD_U, WOOD_V).addBox(-1.0F, -7.0F, -66.0F, 2, 2, 50)
                        .texOffs(WOOD_U, WOOD_V).addBox(-12.0F, -7.5F, -40.0F, 24, 1, 1)
                        .texOffs(WOOD_U, WOOD_V).addBox(-12.0F, -7.5F, -64.0F, 24, 1, 1),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        return LayerDefinition.create(mesh, 256, 128);
    }

    @Override
    public void setupAnim(SleighRenderState state) {
        super.setupAnim(state);
        sack.visible = state.load > 0.0F;
        float size = SACK_MIN + (1.0F - SACK_MIN) * state.load;
        sack.xScale = size;
        sack.yScale = size;
        sack.zScale = size;
        shaft.visible = state.harnessed;
    }
}
