package dev.eliasnvx.cozychristmas.client.workshop;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.eliasnvx.cozychristmas.workshop.Elf;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.HumanoidArm;

/**
 * An elf: an 18 px humanoid with a big head, pointy ears, a pointy shoe on each foot and a tall hat that bends back;
 * texture 64x64 ({@code tools/gen_workshop_mobs.py} paints it on the same box layout). Humanoid so the vanilla
 * held-item layer shows the present it carries; carrying, both arms reach forward under it.
 */
public final class ElfModel extends HumanoidModel<Elf> {

    private static final int TEXTURE_WIDTH = 64;
    private static final int TEXTURE_HEIGHT = 64;
    private static final float NECK = 12.0F;
    private static final float HIP = 18.0F;
    /**
     * The held-item layer puts items 10 px down a 12 px arm; these arms are 6 px. Shifting up 2 px and scaling to 0.75
     * puts the item at the hand (-2 + 0.75 * 10 = 5.5 px).
     */
    private static final float HAND_SHIFT = -2.0F / 16.0F;
    private static final float HELD_SCALE = 0.75F;
    /** Carrying, the present is held between both hands: in front of the chest, not off to the right. */
    private static final float CARRY_SHIFT = 2.5F / 16.0F;
    private static final float CARRY_ARMS = -1.05F;
    private static final float CARRY_INWARD = 0.25F;
    private static final float HAT_BEND = -0.55F;

    /** Set in {@link #setupAnim} for the held-item layer that runs right after it in the same frame. */
    private boolean carrying;

    public ElfModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-3.0F, -6.0F, -3.0F, 6, 6, 6)
                        .texOffs(24, 0).addBox(-5.0F, -4.0F, -0.5F, 2, 3, 1)
                        .texOffs(24, 4).addBox(3.0F, -4.0F, -0.5F, 2, 3, 1)
                        .texOffs(30, 0).addBox(-6.0F, -5.0F, -0.5F, 1, 2, 1)
                        .texOffs(30, 3).addBox(5.0F, -5.0F, -0.5F, 1, 2, 1)
                        .texOffs(34, 0).addBox(-0.5F, -3.0F, -4.0F, 1, 1, 1),
                PartPose.offset(0.0F, NECK, 0.0F));
        // 1.20.1 HumanoidModel takes the hat from the root (26.3: a head child) and copies the head's pose onto it
        PartDefinition hat = root.addOrReplaceChild("hat", CubeListBuilder.create()
                        .texOffs(0, 12).addBox(-3.5F, -7.0F, -3.5F, 7, 2, 7)
                        .texOffs(0, 21).addBox(-2.5F, -9.0F, -2.5F, 5, 2, 5),
                PartPose.offset(0.0F, NECK, 0.0F));
        hat.addOrReplaceChild("hat_tip", CubeListBuilder.create()
                        .texOffs(20, 21).addBox(-1.5F, -3.0F, -1.5F, 3, 3, 3)
                        .texOffs(32, 21).addBox(-1.0F, -5.0F, -1.0F, 2, 2, 2),
                PartPose.offsetAndRotation(0.0F, -9.0F, 0.5F, HAT_BEND, 0.0F, 0.0F));
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 28).addBox(-2.5F, 0.0F, -1.5F, 5, 6, 3),
                PartPose.offset(0.0F, NECK, 0.0F));
        root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(16, 28).addBox(-1.5F, -0.5F, -1.0F, 2, 6, 2),
                PartPose.offset(-3.0F, NECK + 0.5F, 0.0F));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(24, 28).addBox(-0.5F, -0.5F, -1.0F, 2, 6, 2),
                PartPose.offset(3.0F, NECK + 0.5F, 0.0F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                        .texOffs(0, 37).addBox(-1.0F, 0.0F, -1.0F, 2, 6, 2)
                        .texOffs(8, 37).addBox(-1.0F, 5.0F, -2.0F, 2, 1, 1),
                PartPose.offset(-1.2F, HIP, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                        .texOffs(16, 37).addBox(-1.0F, 0.0F, -1.0F, 2, 6, 2)
                        .texOffs(24, 37).addBox(-1.0F, 5.0F, -2.0F, 2, 1, 1),
                PartPose.offset(1.2F, HIP, 0.0F));
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    @Override
    public void setupAnim(Elf elf, float walkPos, float walkSpeed, float ageInTicks, float headYaw, float headPitch) {
        super.setupAnim(elf, walkPos, walkSpeed, ageInTicks, headYaw, headPitch);
        // 1.20.1 HumanoidModel moves the parts to a player's positions: back to ours
        restorePosition(head);
        restorePosition(body);
        restorePosition(rightArm);
        restorePosition(leftArm);
        restorePosition(rightLeg);
        restorePosition(leftLeg);
        hat.copyFrom(head);
        carrying = elf.isCarryingPresent();
        if (carrying) {
            rightArm.xRot = CARRY_ARMS;
            leftArm.xRot = CARRY_ARMS;
            rightArm.yRot = -CARRY_INWARD;
            leftArm.yRot = CARRY_INWARD;
        }
    }

    private static void restorePosition(ModelPart part) {
        PartPose pose = part.getInitialPose();
        part.x = pose.x;
        part.y = pose.y;
        part.z = pose.z;
    }

    @Override
    public void translateToHand(HumanoidArm arm, PoseStack pose) {
        super.translateToHand(arm, pose);
        pose.translate(carrying ? (arm == HumanoidArm.RIGHT ? CARRY_SHIFT : -CARRY_SHIFT) : 0.0F, HAND_SHIFT, 0.0F);
        pose.scale(HELD_SCALE, HELD_SCALE, HELD_SCALE);
    }
}
