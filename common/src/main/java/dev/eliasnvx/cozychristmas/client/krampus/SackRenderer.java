package dev.eliasnvx.cozychristmas.client.krampus;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.registry.client.level.entity.EntityModelLayerRegistry;
import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.krampus.KrampusSack;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;

/** The sack over Krampus's victim, facing the way they face. */
public final class SackRenderer extends EntityRenderer<KrampusSack> {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(CozyChristmas.id("krampus_sack"), "main");
    private static final ResourceLocation TEXTURE = CozyChristmas.id("textures/entity/krampus_sack.png");

    private final SackModel model;
    private final RenderType renderType;
    /** Reused every frame: the pose stack copies it into its matrix. */
    private final Quaternionf rotation = new Quaternionf();

    public SackRenderer(EntityRendererProvider.Context context) {
        super(context);
        model = new SackModel(context.bakeLayer(LAYER));
        renderType = model.renderType(TEXTURE);
        shadowRadius = 0.5F;
    }

    public static void registerLayers() {
        EntityModelLayerRegistry.register(LAYER, SackModel::createLayer);
    }

    @Override
    public void render(KrampusSack sack, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
        LivingEntity victim = sack.victim();
        float yRot = victim == null ? 0.0F : victim.getVisualRotationYInDegrees();
        poseStack.pushPose();
        poseStack.mulPose(rotation.rotationY((180.0F - yRot) * Mth.DEG_TO_RAD));
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0F, -1.501F, 0.0F);
        model.setupAnim(sack, 0.0F, 0.0F, sack.tickCount + partialTick, 0.0F, 0.0F);
        model.renderToBuffer(poseStack, buffers.getBuffer(renderType), light, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        super.render(sack, yaw, partialTick, poseStack, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(KrampusSack sack) {
        return TEXTURE;
    }
}
