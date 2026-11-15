package dev.eliasnvx.cozychristmas.client.snowman;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * An evil snowman: three snowballs like the snow golem's, a carrot nose, a crooked top hat and twig arms raised up and
 * out. It waddles as it walks and swings its right arm over to throw. Texture 128x64, boxes in
 * {@code tools/gen_snowman.py}.
 */
public final class EvilSnowmanModel extends EntityModel<EvilSnowmanRenderState> {

    private static final int TEXTURE_WIDTH = 128;
    private static final int TEXTURE_HEIGHT = 64;
    private static final CubeDeformation SNOW = new CubeDeformation(-0.5F);
    /** Twig arms: out from the shoulders and a little up. */
    private static final float ARM_RAISE = 1.9F;
    /** The throw lifts the arm overhead (z) while it winds back and whips forward (x). */
    private static final float THROW_LIFT = 0.9F;
    private static final float THROW_WHIP = 0.9F;
    private static final float HAT_TILT = 0.14F;
    private static final float WADDLE = 0.1F;
    private static final float WADDLE_RATE = 0.6F;
    private static final float BODY_TURN = 0.25F;

    private final ModelPart head;
    private final ModelPart upperBody;
    private final ModelPart lowerBody;
    private final ModelPart rightArm;
    private final ModelPart leftArm;

    public EvilSnowmanModel(ModelPart root) {
        super(root);
        head = root.getChild("head");
        upperBody = root.getChild("upper_body");
        lowerBody = root.getChild("lower_body");
        rightArm = upperBody.getChild("right_arm");
        leftArm = upperBody.getChild("left_arm");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, SNOW), PartPose.offset(0.0F, 4.0F, 0.0F));
        head.addOrReplaceChild("nose", CubeListBuilder.create()
                .texOffs(48, 0).addBox(-0.5F, -4.5F, -6.5F, 1, 1, 3), PartPose.ZERO);
        PartDefinition hat = head.addOrReplaceChild("hat", CubeListBuilder.create()
                        .texOffs(64, 0).addBox(-5.0F, -1.0F, -5.0F, 10, 1, 10)
                        .texOffs(64, 12).addBox(-3.0F, -6.0F, -3.0F, 6, 5, 6),
                PartPose.offsetAndRotation(0.0F, -7.0F, 0.0F, 0.0F, 0.0F, HAT_TILT));
        hat.addOrReplaceChild("band", CubeListBuilder.create()
                .texOffs(88, 12).addBox(-3.0F, -2.0F, -3.0F, 6, 1, 6, new CubeDeformation(0.1F)), PartPose.ZERO);
        PartDefinition upper = root.addOrReplaceChild("upper_body", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-5.0F, -10.0F, -5.0F, 10, 10, 10, SNOW), PartPose.offset(0.0F, 13.0F, 0.0F));
        CubeListBuilder arm = CubeListBuilder.create().texOffs(32, 0).addBox(-1.0F, 0.0F, -1.0F, 2, 11, 2, SNOW);
        upper.addOrReplaceChild("right_arm", arm, PartPose.offsetAndRotation(-4.0F, -7.0F, 0.0F, 0.0F, 0.0F, ARM_RAISE));
        upper.addOrReplaceChild("left_arm", arm, PartPose.offsetAndRotation(4.0F, -7.0F, 0.0F, 0.0F, 0.0F, -ARM_RAISE));
        root.addOrReplaceChild("lower_body", CubeListBuilder.create()
                .texOffs(0, 36).addBox(-6.0F, -12.0F, -6.0F, 12, 12, 12, SNOW), PartPose.offset(0.0F, 24.0F, 0.0F));
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    @Override
    public void setupAnim(EvilSnowmanRenderState state) {
        super.setupAnim(state);
        head.yRot = state.yRot * Mth.DEG_TO_RAD;
        head.xRot = state.xRot * Mth.DEG_TO_RAD;
        float waddle = Mth.sin(state.walkAnimationPos * WADDLE_RATE) * WADDLE * Math.min(1.0F, state.walkAnimationSpeed);
        lowerBody.zRot = waddle;
        upperBody.zRot = -waddle;
        upperBody.yRot = state.yRot * Mth.DEG_TO_RAD * BODY_TURN;
        head.zRot = waddle * 0.5F;
        if (state.throwProgress > 0.0F) {
            float swing = 1.0F - state.throwProgress;
            rightArm.zRot += Mth.sin(swing * Mth.PI) * THROW_LIFT;
            rightArm.xRot = Mth.sin(swing * Mth.TWO_PI) * THROW_WHIP;
        }
    }
}
