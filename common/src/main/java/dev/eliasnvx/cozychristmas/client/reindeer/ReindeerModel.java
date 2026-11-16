package dev.eliasnvx.cozychristmas.client.reindeer;

import dev.eliasnvx.cozychristmas.reindeer.Reindeer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * A reindeer: a deer-sized quadruped with a raised neck, branching antlers and a stub tail; the saddle and a collar
 * with a bell show when saddled. Texture 128x64 ({@code tools/gen_workshop_mobs.py} paints it on the same box layout).
 * Flying, the legs fold like in a leap and paddle slowly, and the neck stretches forward. Santa's sleigh draws its team
 * with this model too, through {@link #pose} (they're part of the sleigh, not entities).
 */
public final class ReindeerModel extends HierarchicalModel<Reindeer> {

    private static final int TEXTURE_WIDTH = 128;
    private static final int TEXTURE_HEIGHT = 64;
    private static final float LEG_TOP = 14.0F;
    private static final float NECK_TILT = 0.5F;
    private static final float NECK_TILT_FLYING = 0.95F;
    private static final float ANTLER_OUT = 0.35F;
    private static final float ANTLER_BACK = -0.25F;
    private static final float TAIL_TILT = 0.35F;
    private static final float WALK_SPEED = 0.6662F;
    private static final float WALK_SWING = 1.2F;
    private static final float FLY_FOLD = 0.9F;
    private static final float FLY_PADDLE = 0.15F;
    private static final float FLY_PADDLE_SPEED = 0.2F;

    private final ModelPart root;
    private final ModelPart neck;
    private final ModelPart head;
    private final ModelPart rightAntler;
    private final ModelPart leftAntler;
    private final ModelPart saddle;
    private final ModelPart collar;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;

    public ReindeerModel(ModelPart root) {
        this.root = root;
        this.neck = root.getChild("neck");
        this.head = neck.getChild("head");
        this.rightAntler = head.getChild("right_antler");
        this.leftAntler = head.getChild("left_antler");
        this.saddle = root.getChild("saddle");
        this.collar = neck.getChild("collar");
        this.rightFrontLeg = root.getChild("right_front_leg");
        this.leftFrontLeg = root.getChild("left_front_leg");
        this.rightHindLeg = root.getChild("right_hind_leg");
        this.leftHindLeg = root.getChild("left_hind_leg");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -9.0F, 8, 8, 18),
                PartPose.offset(0.0F, 10.0F, 0.0F));
        root.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(12, 26).addBox(-1.0F, 0.0F, 0.0F, 2, 3, 1),
                PartPose.offsetAndRotation(0.0F, 7.0F, 9.0F, TAIL_TILT, 0.0F, 0.0F));
        root.addOrReplaceChild("saddle", CubeListBuilder.create().texOffs(0, 39).addBox(-4.5F, -5.0F, -4.0F, 9, 2, 8),
                PartPose.offset(0.0F, 10.0F, 0.0F));

        PartDefinition neck = root.addOrReplaceChild("neck", CubeListBuilder.create()
                        .texOffs(52, 0).addBox(-2.0F, -8.0F, -2.0F, 4, 9, 4),
                PartPose.offsetAndRotation(0.0F, 8.0F, -7.0F, NECK_TILT, 0.0F, 0.0F));
        neck.addOrReplaceChild("collar", CubeListBuilder.create()
                        .texOffs(34, 39).addBox(-2.5F, -1.0F, -2.5F, 5, 2, 5)
                        .texOffs(54, 39).addBox(-1.0F, 1.0F, -3.5F, 2, 2, 2),
                PartPose.ZERO);
        PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(68, 0).addBox(-2.5F, -3.0F, -4.0F, 5, 5, 6)
                        .texOffs(90, 0).addBox(-1.5F, -1.0F, -7.0F, 3, 3, 3)
                        .texOffs(102, 0).addBox(-4.5F, -2.5F, -0.5F, 2, 1, 1)
                        .texOffs(102, 2).addBox(2.5F, -2.5F, -0.5F, 2, 1, 1),
                PartPose.offsetAndRotation(0.0F, -8.0F, 0.0F, -NECK_TILT, 0.0F, 0.0F));
        CubeListBuilder antler = CubeListBuilder.create()
                .texOffs(108, 0).addBox(-0.5F, -7.0F, -0.5F, 1, 7, 1)
                .texOffs(112, 0).addBox(-0.5F, -4.0F, -3.0F, 1, 1, 3)
                .texOffs(112, 4).addBox(-0.5F, -7.0F, -2.0F, 1, 1, 2)
                .texOffs(108, 8).addBox(-0.5F, -9.0F, 0.5F, 1, 3, 1);
        head.addOrReplaceChild("right_antler", antler,
                PartPose.offsetAndRotation(-1.5F, -3.0F, -1.0F, ANTLER_BACK, 0.0F, -ANTLER_OUT));
        head.addOrReplaceChild("left_antler", antler,
                PartPose.offsetAndRotation(1.5F, -3.0F, -1.0F, ANTLER_BACK, 0.0F, ANTLER_OUT));

        CubeListBuilder leg = CubeListBuilder.create().texOffs(0, 26).addBox(-1.5F, 0.0F, -1.5F, 3, 10, 3);
        CubeListBuilder mirroredLeg = CubeListBuilder.create().mirror().texOffs(0, 26).addBox(-1.5F, 0.0F, -1.5F, 3, 10, 3);
        root.addOrReplaceChild("right_front_leg", leg, PartPose.offset(-2.5F, LEG_TOP, -6.5F));
        root.addOrReplaceChild("left_front_leg", mirroredLeg, PartPose.offset(2.5F, LEG_TOP, -6.5F));
        root.addOrReplaceChild("right_hind_leg", leg, PartPose.offset(-2.5F, LEG_TOP, 6.5F));
        root.addOrReplaceChild("left_hind_leg", mirroredLeg, PartPose.offset(2.5F, LEG_TOP, 6.5F));
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(Reindeer reindeer, float walkPos, float walkSpeed, float ageInTicks, float headYaw, float headPitch) {
        pose(reindeer.isSaddled(), reindeer.isBaby(), reindeer.isFlying(), walkPos, walkSpeed, ageInTicks, headYaw, headPitch);
    }

    /**
     * Poses the model: saddled shows the saddle and bell collar, fawns have no antlers yet, flying tucks the legs. Every
     * animated part is set outright (1.20.1 models keep last frame's pose), so a team of four can share one model.
     */
    public void pose(boolean saddled, boolean baby, boolean flying, float walkPos, float walkSpeed, float ageInTicks,
                     float headYaw, float headPitch) {
        saddle.visible = saddled;
        collar.visible = saddled;
        rightAntler.visible = !baby;
        leftAntler.visible = !baby;
        neck.yRot = headYaw * Mth.DEG_TO_RAD;
        if (flying) {
            neck.xRot = NECK_TILT_FLYING;
            head.xRot = -NECK_TILT_FLYING;
            float paddle = Mth.sin(ageInTicks * FLY_PADDLE_SPEED) * FLY_PADDLE;
            rightFrontLeg.xRot = -FLY_FOLD + paddle;
            leftFrontLeg.xRot = -FLY_FOLD - paddle;
            rightHindLeg.xRot = FLY_FOLD - paddle;
            leftHindLeg.xRot = FLY_FOLD + paddle;
            return;
        }
        neck.xRot = NECK_TILT;
        head.xRot = -NECK_TILT + headPitch * Mth.DEG_TO_RAD;
        float walk = walkPos * WALK_SPEED;
        float swing = WALK_SWING * walkSpeed;
        rightHindLeg.xRot = Mth.cos(walk) * swing;
        leftHindLeg.xRot = Mth.cos(walk + Mth.PI) * swing;
        rightFrontLeg.xRot = Mth.cos(walk + Mth.PI) * swing;
        leftFrontLeg.xRot = Mth.cos(walk) * swing;
    }
}
