package dev.eliasnvx.cozychristmas.client.sleigh;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.architectury.registry.client.level.entity.EntityModelLayerRegistry;
import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.client.reindeer.ReindeerModel;
import dev.eliasnvx.cozychristmas.client.reindeer.ReindeerRenderState;
import dev.eliasnvx.cozychristmas.client.reindeer.ReindeerRenderer;
import dev.eliasnvx.cozychristmas.config.CozyConfig;
import dev.eliasnvx.cozychristmas.sleigh.SantasSleigh;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

/**
 * Santa's sleigh and, while harnessed, its team: four reindeer in two pairs ahead of it, drawn with the reindeer's own
 * model and texture (they're part of the sleigh, not entities). In flight they run in the air the way a ridden
 * reindeer flies; parked, they stand.
 */
public final class SleighRenderer extends EntityRenderer<SantasSleigh, SleighRenderState> {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(CozyChristmas.id("santas_sleigh"), "main");
    private static final Identifier TEXTURE = CozyChristmas.id("textures/entity/santas_sleigh.png");
    private static final Identifier REINDEER_TEXTURE = CozyChristmas.id("textures/entity/reindeer.png");
    /** The team's places in the sleigh's model space, in blocks: two pairs ahead of it (forward is -z). */
    private static final float[][] TEAM = {{-0.6F, -2.2F}, {0.6F, -2.2F}, {-0.6F, -3.85F}, {0.6F, -3.85F}};
    /** The team runs this far ahead: keep drawing the sleigh while only they are in view. */
    private static final double CULL_MARGIN = 4.5;

    private final SleighModel model;
    private final ReindeerModel reindeer;

    public SleighRenderer(EntityRendererProvider.Context context) {
        super(context);
        model = new SleighModel(context.bakeLayer(LAYER));
        reindeer = new ReindeerModel(context.bakeLayer(ReindeerRenderer.LAYER));
        shadowRadius = 1.3F;
    }

    public static void registerLayers() {
        EntityModelLayerRegistry.register(LAYER, SleighModel::createLayer);
    }

    @Override
    public SleighRenderState createRenderState() {
        return new SleighRenderState();
    }

    @Override
    public void extractRenderState(SantasSleigh sleigh, SleighRenderState state, float partialTicks) {
        super.extractRenderState(sleigh, state, partialTicks);
        state.yRot = sleigh.getYRot(partialTicks);
        state.load = Mth.clamp(sleigh.load() / (float) CozyConfig.common().workshop().sleigh().capacity(), 0.0F, 1.0F);
        state.harnessed = sleigh.isHarnessed();
        state.flying = sleigh.phase().isFlying();
        for (int i = 0; i < SleighRenderState.TEAM; i++) {
            ReindeerRenderState deer = state.team[i];
            deer.ageInTicks = state.ageInTicks + i * 7.0F; // not in step with each other
            deer.flying = state.flying;
            deer.walkAnimationSpeed = 0.0F; // parked they stand; flying, the model tucks their legs
            deer.lightCoords = state.lightCoords;
        }
    }

    @Override
    protected AABB getBoundingBoxForCulling(SantasSleigh sleigh, float partialTicks) {
        return super.getBoundingBoxForCulling(sleigh, partialTicks).inflate(CULL_MARGIN);
    }

    @Override
    public void submit(SleighRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.isInvisible) {
            return; // away delivering: the base renderer only hides the shadow of a non-living entity
        }
        poseStack.pushPose();
        poseStack.rotateDegrees(Axis.YP, 180.0F - state.yRot);
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0F, -1.501F, 0.0F);
        collector.submitModel(model, state, poseStack, TEXTURE, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        if (state.harnessed) {
            for (int i = 0; i < SleighRenderState.TEAM; i++) {
                poseStack.pushPose();
                poseStack.translate(TEAM[i][0], 0.0F, TEAM[i][1]);
                collector.submitModel(reindeer, state.team[i], poseStack, REINDEER_TEXTURE, state.lightCoords,
                        OverlayTexture.NO_OVERLAY, state.outlineColor);
                poseStack.popPose();
            }
        }
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }
}
