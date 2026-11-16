package dev.eliasnvx.cozychristmas.client.krampus;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.architectury.registry.client.level.entity.EntityModelLayerRegistry;
import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.krampus.KrampusSack;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;

/** The sack over Krampus's victim, facing the way they face. */
public final class SackRenderer extends EntityRenderer<KrampusSack, SackRenderState> {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(CozyChristmas.id("krampus_sack"), "main");
    private static final Identifier TEXTURE = CozyChristmas.id("textures/entity/krampus_sack.png");

    private final SackModel model;

    public SackRenderer(EntityRendererProvider.Context context) {
        super(context);
        model = new SackModel(context.bakeLayer(LAYER));
        shadowRadius = 0.5F;
    }

    public static void registerLayers() {
        EntityModelLayerRegistry.register(LAYER, SackModel::createLayer);
    }

    @Override
    public SackRenderState createRenderState() {
        return new SackRenderState();
    }

    @Override
    public void extractRenderState(KrampusSack sack, SackRenderState state, float partialTicks) {
        super.extractRenderState(sack, state, partialTicks);
        LivingEntity victim = sack.victim();
        state.yRot = victim == null ? 0.0F : victim.getVisualRotationYInDegrees();
    }

    @Override
    public void submit(SackRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.rotateDegrees(Axis.YP, 180.0F - state.yRot);
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0F, -1.501F, 0.0F);
        collector.submitModel(model, state, poseStack, TEXTURE, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }
}
