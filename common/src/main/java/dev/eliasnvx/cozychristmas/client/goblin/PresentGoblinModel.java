package dev.eliasnvx.cozychristmas.client.goblin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.eliasnvx.cozychristmas.goblin.PresentGoblin;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;

/**
 * Present goblin: the Sock Goblin's rig from Femboy Mod (a 14 px humanoid with a big head, long pointy ears and a little
 * nose) under a floppy red cap with a pompom. Humanoid, so the vanilla held-item layer shows the stolen stack; while it
 * has loot the right arm holds it up over its head. Texture 64x32, boxes in {@code tools/gen_goblins.py}.
 */
public final class PresentGoblinModel extends HumanoidModel<PresentGoblin> {

    private static final int TEXTURE_WIDTH = 64;
    private static final int TEXTURE_HEIGHT = 32;
    private static final float NECK = 15.0F;
    /** Items sit 10 px down a 12 px arm; these arms are 5 px: up 3 px and scaled to 0.7 puts them at the hand. */
    private static final float HAND_SHIFT = -3.0F / 16.0F;
    private static final float HELD_SCALE = 0.7F;
    private static final float LOOT_ARM = -2.9F;
    private static final float LOOT_WAVE = 0.15F;

    public PresentGoblinModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-3.0F, -5.0F, -2.5F, 6, 5, 5)
                        .texOffs(22, 0).addBox(-0.5F, -2.5F, -3.5F, 1, 2, 1)
                        .texOffs(0, 10).addBox(-7.0F, -4.5F, -0.5F, 4, 2, 1)
                        .texOffs(0, 13).mirror().addBox(3.0F, -4.5F, -0.5F, 4, 2, 1),
                PartPose.offset(0.0F, NECK, 0.0F));
        // the cap: a band on the head, a crown leaning back and a pompom hanging off its tip. 1.21.1 HumanoidModel takes
        // the hat from the root (26.3: a head child) and copies the head's pose onto it, so it sits at the head's pivot
        PartDefinition cap = root.addOrReplaceChild("hat", CubeListBuilder.create()
                        .texOffs(32, 0).addBox(-3.5F, -6.0F, -3.0F, 7, 2, 6),
                PartPose.offset(0.0F, NECK, 0.0F));
        PartDefinition crown = cap.addOrReplaceChild("cap_crown", CubeListBuilder.create()
                        .texOffs(32, 8).addBox(-2.0F, -3.0F, -2.0F, 4, 3, 4),
                PartPose.offsetAndRotation(0.0F, -6.0F, 0.5F, -0.5F, 0.0F, 0.2F));
        crown.addOrReplaceChild("cap_pompom", CubeListBuilder.create()
                        .texOffs(48, 8).addBox(-1.0F, -1.0F, -1.0F, 2, 2, 2),
                PartPose.offset(0.5F, -3.0F, 0.0F));
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 16).addBox(-2.5F, 0.0F, -1.5F, 5, 5, 3),
                PartPose.offset(0.0F, NECK, 0.0F));
        root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(16, 16).addBox(-1.5F, -0.5F, -1.0F, 2, 5, 2),
                PartPose.offset(-3.0F, 15.5F, 0.0F));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(24, 16).addBox(-0.5F, -0.5F, -1.0F, 2, 5, 2),
                PartPose.offset(3.0F, 15.5F, 0.0F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 24).addBox(-1.0F, 0.0F, -1.0F, 2, 4, 2),
                PartPose.offset(-1.2F, 20.0F, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(8, 24).addBox(-1.0F, 0.0F, -1.0F, 2, 4, 2),
                PartPose.offset(1.2F, 20.0F, 0.0F));
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    @Override
    public void setupAnim(PresentGoblin goblin, float walkPos, float walkSpeed, float ageInTicks, float headYaw, float headPitch) {
        super.setupAnim(goblin, walkPos, walkSpeed, ageInTicks, headYaw, headPitch);
        // 1.21.1 HumanoidModel moves the parts to a player's positions: back to ours
        restorePosition(head);
        restorePosition(body);
        restorePosition(rightArm);
        restorePosition(leftArm);
        restorePosition(rightLeg);
        restorePosition(leftLeg);
        hat.copyFrom(head);
        if (goblin.hasLoot()) {
            rightArm.xRot = LOOT_ARM + Mth.sin(ageInTicks * 0.5F) * LOOT_WAVE;
            rightArm.yRot = 0.0F;
            rightArm.zRot = 0.1F;
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
        pose.translate(0.0F, HAND_SHIFT, 0.0F);
        pose.scale(HELD_SCALE, HELD_SCALE, HELD_SCALE);
    }
}
