package dev.eliasnvx.cozychristmas.client.workshop;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * Santa: a 32 px humanoid, broad, with a round belly, a full beard and a floppy hat with a pompom; texture 128x64
 * ({@code tools/gen_workshop_mobs.py} paints it on the same box layout). Turning someone away he shakes his head
 * like a villager.
 */
public final class SantaModel extends HumanoidModel<SantaRenderState> {

    private static final int TEXTURE_WIDTH = 128;
    private static final int TEXTURE_HEIGHT = 64;
    private static final float SHOULDER = 1.0F;
    private static final float HIP = 12.0F;
    private static final float HAT_FLOP_BACK = -0.45F;
    private static final float HAT_FLOP_SIDE = -0.35F;
    private static final float SHAKE = 0.3F;
    private static final float SHAKE_SPEED = 0.45F;
    private static final float SHAKE_NOD = 0.4F;

    public SantaModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8)
                        .texOffs(32, 0).addBox(-4.0F, -3.0F, -5.0F, 8, 7, 2)
                        .texOffs(52, 0).addBox(-1.0F, -5.0F, -5.0F, 2, 2, 1),
                PartPose.ZERO);
        PartDefinition hat = head.addOrReplaceChild("hat", CubeListBuilder.create()
                        .texOffs(0, 16).addBox(-4.5F, -9.0F, -4.5F, 9, 2, 9)
                        .texOffs(36, 16).addBox(-3.5F, -11.0F, -3.5F, 7, 2, 7),
                PartPose.ZERO);
        hat.addOrReplaceChild("hat_tip", CubeListBuilder.create()
                        .texOffs(0, 27).addBox(-2.0F, -4.0F, -2.0F, 4, 4, 4)
                        .texOffs(16, 27).addBox(-1.5F, -7.0F, -1.5F, 3, 3, 3),
                PartPose.offsetAndRotation(0.0F, -11.0F, 0.5F, HAT_FLOP_BACK, 0.0F, HAT_FLOP_SIDE));
        root.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(0, 35).addBox(-5.0F, 0.0F, -3.5F, 10, 12, 7)
                        .texOffs(34, 35).addBox(-4.0F, 3.0F, -4.5F, 8, 7, 1),
                PartPose.ZERO);
        root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(34, 44).addBox(-3.0F, -1.0F, -2.0F, 4, 11, 4),
                PartPose.offset(-6.0F, SHOULDER, 0.0F));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(64, 0).addBox(-1.0F, -1.0F, -2.0F, 4, 11, 4),
                PartPose.offset(6.0F, SHOULDER, 0.0F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(80, 0).addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4),
                PartPose.offset(-2.5F, HIP, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(96, 0).addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4),
                PartPose.offset(2.5F, HIP, 0.0F));
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    @Override
    public void setupAnim(SantaRenderState state) {
        super.setupAnim(state);
        if (state.unhappy) {
            head.zRot = SHAKE * Mth.sin(SHAKE_SPEED * state.ageInTicks);
            head.xRot = SHAKE_NOD;
        }
    }
}
