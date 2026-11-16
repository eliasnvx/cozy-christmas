package dev.eliasnvx.cozychristmas.client.sleigh;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.architectury.registry.client.level.entity.EntityModelLayerRegistry;
import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.client.reindeer.ReindeerModel;
import dev.eliasnvx.cozychristmas.client.reindeer.ReindeerRenderer;
import dev.eliasnvx.cozychristmas.sleigh.SantasSleigh;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;

/**
 * Santa's sleigh and, while harnessed, its team: four reindeer in two pairs ahead of it, drawn with the reindeer's own
 * model and texture (they're part of the sleigh, not entities). In flight they run in the air the way a ridden
 * reindeer flies; parked, they stand.
 */
public final class SleighRenderer extends EntityRenderer<SantasSleigh> {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(CozyChristmas.id("santas_sleigh"), "main");
    private static final ResourceLocation TEXTURE = CozyChristmas.id("textures/entity/santas_sleigh.png");
    /** The team's places in the sleigh's model space, in blocks: two pairs ahead of it (forward is -z). */
    private static final float[][] TEAM = {{-0.6F, -2.2F}, {0.6F, -2.2F}, {-0.6F, -3.85F}, {0.6F, -3.85F}};
    /** Each deer's paddle runs this many ticks behind the one before, so they're not in step with each other. */
    private static final float TEAM_PHASE = 7.0F;
    /** The team runs this far ahead: keep drawing the sleigh while only they are in view. */
    private static final double CULL_MARGIN = 4.5;

    private final SleighModel model;
    private final ReindeerModel reindeer;
    private final RenderType sleighType;
    private final RenderType reindeerType;
    /** Reused every frame: the pose stack copies it into its matrix. */
    private final Quaternionf rotation = new Quaternionf();

    public SleighRenderer(EntityRendererProvider.Context context) {
        super(context);
        model = new SleighModel(context.bakeLayer(LAYER));
        reindeer = new ReindeerModel(context.bakeLayer(ReindeerRenderer.LAYER));
        sleighType = model.renderType(TEXTURE);
        reindeerType = reindeer.renderType(ReindeerRenderer.TEXTURE);
        shadowRadius = 1.3F;
    }

    public static void registerLayers() {
        EntityModelLayerRegistry.register(LAYER, SleighModel::createLayer);
    }

    @Override
    public boolean shouldRender(SantasSleigh sleigh, Frustum frustum, double camX, double camY, double camZ) {
        if (super.shouldRender(sleigh, frustum, camX, camY, camZ)) {
            return true;
        }
        return sleigh.shouldRender(camX, camY, camZ) && frustum.isVisible(sleigh.getBoundingBoxForCulling().inflate(CULL_MARGIN));
    }

    @Override
    public void render(SantasSleigh sleigh, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
        if (sleigh.isInvisible()) {
            return; // away delivering: the dispatcher only hides the shadow of an invisible entity
        }
        float ageInTicks = sleigh.tickCount + partialTick;
        poseStack.pushPose();
        poseStack.mulPose(rotation.rotationY((180.0F - yaw) * Mth.DEG_TO_RAD));
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0F, -1.501F, 0.0F);
        model.setupAnim(sleigh, 0.0F, 0.0F, ageInTicks, 0.0F, 0.0F);
        model.renderToBuffer(poseStack, buffers.getBuffer(sleighType), light, OverlayTexture.NO_OVERLAY);
        if (sleigh.isHarnessed()) {
            boolean flying = sleigh.phase().isFlying();
            VertexConsumer team = buffers.getBuffer(reindeerType);
            for (int i = 0; i < TEAM.length; i++) {
                poseStack.pushPose();
                poseStack.translate(TEAM[i][0], 0.0F, TEAM[i][1]);
                // the harness: saddle and bell collar; parked they stand, flying the model tucks their legs
                reindeer.pose(true, false, flying, 0.0F, 0.0F, ageInTicks + i * TEAM_PHASE, 0.0F, 0.0F);
                reindeer.renderToBuffer(poseStack, team, light, OverlayTexture.NO_OVERLAY);
                poseStack.popPose();
            }
        }
        poseStack.popPose();
        super.render(sleigh, yaw, partialTick, poseStack, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(SantasSleigh sleigh) {
        return TEXTURE;
    }
}
