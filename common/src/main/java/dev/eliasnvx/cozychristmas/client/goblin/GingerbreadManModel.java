package dev.eliasnvx.cozychristmas.client.goblin;

import dev.eliasnvx.cozychristmas.goblin.GingerbreadMan;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * A gingerbread man: a flat cookie (2 px thick), 13 px tall, arms held out from the body like a cutter's shape.
 * Texture 32x32, boxes in {@code tools/gen_goblins.py}.
 */
public final class GingerbreadManModel extends HumanoidModel<GingerbreadMan> {

    private static final int TEXTURE_SIZE = 32;
    /** Arms stick out sideways at rest, like the cookie cutter. */
    private static final float ARMS_OUT = 0.7F;

    public GingerbreadManModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-2.5F, -5.0F, -1.0F, 5, 5, 2),
                PartPose.offset(0.0F, 15.0F, 0.0F));
        // 1.21.1 HumanoidModel takes the (empty) hat from the root and copies the head's pose onto it
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0.0F, 15.0F, 0.0F));
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 7).addBox(-2.5F, 0.0F, -1.0F, 5, 5, 2),
                PartPose.offset(0.0F, 15.0F, 0.0F));
        root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(14, 0).addBox(-1.0F, -0.5F, -1.0F, 2, 5, 2),
                PartPose.offset(-3.0F, 15.5F, 0.0F));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(22, 0).addBox(-1.0F, -0.5F, -1.0F, 2, 5, 2),
                PartPose.offset(3.0F, 15.5F, 0.0F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(14, 7).addBox(-1.0F, 0.0F, -1.0F, 2, 4, 2),
                PartPose.offset(-1.3F, 20.0F, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(22, 7).addBox(-1.0F, 0.0F, -1.0F, 2, 4, 2),
                PartPose.offset(1.3F, 20.0F, 0.0F));
        return LayerDefinition.create(mesh, TEXTURE_SIZE, TEXTURE_SIZE);
    }

    @Override
    public void setupAnim(GingerbreadMan man, float walkPos, float walkSpeed, float ageInTicks, float headYaw, float headPitch) {
        super.setupAnim(man, walkPos, walkSpeed, ageInTicks, headYaw, headPitch);
        // 1.21.1 HumanoidModel moves the parts to a player's positions: back to ours
        restorePosition(head);
        restorePosition(body);
        restorePosition(rightArm);
        restorePosition(leftArm);
        restorePosition(rightLeg);
        restorePosition(leftLeg);
        hat.copyFrom(head);
        rightArm.zRot += ARMS_OUT;
        leftArm.zRot -= ARMS_OUT;
    }

    private static void restorePosition(ModelPart part) {
        PartPose pose = part.getInitialPose();
        part.x = pose.x;
        part.y = pose.y;
        part.z = pose.z;
    }
}
