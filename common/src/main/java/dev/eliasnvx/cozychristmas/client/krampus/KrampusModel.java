package dev.eliasnvx.cozychristmas.client.krampus;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * Krampus: shaggy black fur, a goat's muzzle and beard, a long red tongue, ram-curled horns, goat legs bent back at
 * the hock and ending in hooves, a tail, a wicker basket on his back, a birch switch in his right hand and a chain in
 * his left. The renderer draws him {@link KrampusRenderer#SCALE} times a player's size. Moves: the chain whips out
 * (the arm swings over and the chain stretches toward the target), both hands grab forward for the sack, both arms go
 * up for the roar that calls goblins. Texture 128x64, boxes in {@code tools/gen_krampus.py}.
 */
public final class KrampusModel extends HumanoidModel<KrampusRenderState> {

    private static final int TEXTURE_WIDTH = 128;
    private static final int TEXTURE_HEIGHT = 64;
    /** Goat legs: thigh forward, shin back, hoof flat; the walk swing adds to the thigh. */
    private static final float THIGH = -0.4F;
    private static final float SHIN = 0.9F;
    private static final float HOOF = -0.5F;
    private static final float[] HORN_CURL = {-0.35F, -0.5F, -0.6F, -0.7F};
    private static final float HORN_OUT = 0.4F;
    private static final float SWITCH_FORWARD = -0.8F;
    private static final float CHAIN_STRETCH = 3.2F;
    private static final float LASH_WIND_UP = 0.3F;
    private static final float LASH_WHIP = 0.55F;
    private static final float LASH_OVERHEAD = -2.9F;
    private static final float LASH_FORWARD = -1.45F;
    private static final float TONGUE_SWAY = 0.15F;
    private static final float TAIL_SWAY = 0.2F;

    private final ModelPart tongue;
    private final ModelPart tail;
    private final ModelPart chain;

    public KrampusModel(ModelPart root) {
        super(root);
        tongue = head.getChild("tongue");
        tail = body.getChild("tail");
        chain = leftArm.getChild("chain");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8)
                        .texOffs(32, 0).addBox(-2.0F, -4.0F, -7.0F, 4, 3, 3)      // muzzle
                        .texOffs(46, 0).addBox(-1.0F, -1.0F, -6.0F, 2, 4, 1),     // goatee
                PartPose.ZERO);
        head.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        head.addOrReplaceChild("tongue", CubeListBuilder.create().texOffs(52, 0).addBox(-1.0F, 0.0F, -0.5F, 2, 7, 1),
                PartPose.offsetAndRotation(0.0F, -1.5F, -6.5F, 0.15F, 0.0F, 0.0F));
        for (int side = -1; side <= 1; side += 2) {
            String name = side < 0 ? "right" : "left";
            head.addOrReplaceChild(name + "_ear", CubeListBuilder.create().texOffs(58, 0)
                            .addBox(side < 0 ? -3.0F : 0.0F, -0.5F, -1.0F, 3, 1, 2),
                    PartPose.offsetAndRotation(side * 4.0F, -6.0F, -1.0F, 0.0F, 0.0F, side * 0.45F));
            horn(head, name, side);
        }

        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(0, 16).addBox(-5.0F, 0.0F, -3.0F, 10, 12, 6)
                        .texOffs(32, 16).addBox(-6.0F, -1.0F, -4.0F, 12, 4, 8)      // fur ruff
                        .texOffs(72, 16).addBox(-5.5F, 9.0F, -3.5F, 11, 4, 7),      // shaggy hips
                PartPose.ZERO);
        body.addOrReplaceChild("basket", CubeListBuilder.create().texOffs(0, 34).addBox(-4.0F, 0.0F, 0.0F, 8, 9, 4),
                PartPose.offsetAndRotation(0.0F, 1.5F, 3.0F, 0.12F, 0.0F, 0.0F));
        PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create()
                        .texOffs(108, 16).addBox(-0.5F, 0.0F, -0.5F, 1, 8, 1),
                PartPose.offsetAndRotation(0.0F, 11.0F, 3.0F, 0.5F, 0.0F, 0.0F));
        tail.addOrReplaceChild("tuft", CubeListBuilder.create().texOffs(112, 16).addBox(-1.0F, 0.0F, -1.0F, 2, 3, 2),
                PartPose.offset(0.0F, 7.5F, 0.0F));

        PartDefinition rightArm = root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(24, 34).addBox(-3.0F, -2.0F, -2.0F, 4, 13, 4), PartPose.offset(-5.0F, 2.0F, 0.0F));
        rightArm.addOrReplaceChild("switch", CubeListBuilder.create().texOffs(56, 34).addBox(-1.0F, -1.0F, -1.0F, 2, 11, 2),
                PartPose.offsetAndRotation(-1.0F, 10.0F, 0.0F, SWITCH_FORWARD, 0.0F, 0.0F));
        PartDefinition leftArm = root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(40, 34).addBox(-1.0F, -2.0F, -2.0F, 4, 13, 4), PartPose.offset(5.0F, 2.0F, 0.0F));
        leftArm.addOrReplaceChild("chain", CubeListBuilder.create().texOffs(68, 34).addBox(-0.5F, 0.0F, -0.5F, 1, 10, 1),
                PartPose.offset(1.0F, 10.5F, 0.0F));

        leg(root, "right_leg", -2.5F);
        leg(root, "left_leg", 2.5F);
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    /** Four segments from the crown, each thinner and curling further back, the whole horn leaning out. */
    private static void horn(PartDefinition head, String name, int side) {
        int[][] boxes = {{68, 0, 3, 4}, {80, 0, 2, 4}, {88, 0, 1, 4}, {92, 0, 1, 3}};
        PartDefinition parent = head;
        for (int i = 0; i < boxes.length; i++) {
            int[] b = boxes[i];
            float half = b[2] / 2.0F;
            PartPose pose = i == 0
                    ? PartPose.offsetAndRotation(side * 2.5F, -7.5F, -1.5F, HORN_CURL[0], 0.0F, side * HORN_OUT)
                    : PartPose.offsetAndRotation(0.0F, -boxes[i - 1][3] + 0.5F, 0.0F, HORN_CURL[i], 0.0F, side * 0.12F);
            parent = parent.addOrReplaceChild(name + "_horn_" + i, CubeListBuilder.create().texOffs(b[0], b[1])
                    .addBox(-half, -b[3], -half, b[2], b[3], b[2]), pose);
        }
    }

    private static void leg(PartDefinition root, String name, float x) {
        PartDefinition thigh = root.addOrReplaceChild(name, CubeListBuilder.create()
                .texOffs(72, 34).addBox(-2.5F, -0.5F, -2.5F, 5, 7, 5), PartPose.offset(x, 12.0F, 0.0F));
        PartDefinition shin = thigh.addOrReplaceChild("shin", CubeListBuilder.create()
                .texOffs(92, 34).addBox(-1.5F, 0.0F, -1.5F, 3, 5, 3), PartPose.offsetAndRotation(0.0F, 6.5F, 0.0F, SHIN, 0.0F, 0.0F));
        shin.addOrReplaceChild("hoof", CubeListBuilder.create().texOffs(104, 34).addBox(-2.0F, -0.5F, -3.0F, 4, 2, 5),
                PartPose.offsetAndRotation(0.0F, 5.0F, 0.0F, HOOF, 0.0F, 0.0F));
    }

    @Override
    public void setupAnim(KrampusRenderState state) {
        super.setupAnim(state);
        rightLeg.xRot += THIGH;
        leftLeg.xRot += THIGH;
        tongue.zRot = Mth.sin(state.ageInTicks * 0.1F) * TONGUE_SWAY;
        tail.zRot = Mth.sin(state.ageInTicks * 0.07F + 1.0F) * TAIL_SWAY;
        if (state.roarProgress > 0.0F) {
            float raise = Mth.sin((1.0F - state.roarProgress) * Mth.PI);
            rightArm.xRot = -2.7F * raise;
            leftArm.xRot = -2.7F * raise;
            rightArm.zRot = 0.4F * raise;
            leftArm.zRot = -0.4F * raise;
            head.xRot = -0.6F * raise;
        }
        if (state.sackProgress > 0.0F) {
            float grab = Mth.sin((1.0F - state.sackProgress) * Mth.PI);
            rightArm.xRot = -1.5F * grab;
            leftArm.xRot = -1.5F * grab;
            rightArm.yRot = 0.3F * grab;
            leftArm.yRot = -0.3F * grab;
        }
        if (state.lashProgress > 0.0F) {
            float t = 1.0F - state.lashProgress;
            // up overhead, a whip forward, and the chain stretches out toward the target as the arm comes down
            if (t < LASH_WIND_UP) {
                leftArm.xRot = Mth.lerp(t / LASH_WIND_UP, leftArm.xRot, LASH_OVERHEAD);
            } else if (t < LASH_WHIP) {
                leftArm.xRot = Mth.lerp((t - LASH_WIND_UP) / (LASH_WHIP - LASH_WIND_UP), LASH_OVERHEAD, LASH_FORWARD);
            } else {
                leftArm.xRot = LASH_FORWARD;
            }
            float reach = t < LASH_WIND_UP ? 0.0F : (t - LASH_WIND_UP) / (1.0F - LASH_WIND_UP);
            chain.yScale = 1.0F + CHAIN_STRETCH * Mth.sin(reach * Mth.PI);
        }
    }
}
