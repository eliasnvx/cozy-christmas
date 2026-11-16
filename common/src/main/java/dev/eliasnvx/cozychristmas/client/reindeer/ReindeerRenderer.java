package dev.eliasnvx.cozychristmas.client.reindeer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.registry.client.level.entity.EntityModelLayerRegistry;
import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.reindeer.Reindeer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/** Draws reindeer; fawns are the same model at half size, without antlers. */
public final class ReindeerRenderer extends MobRenderer<Reindeer, ReindeerModel> {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(CozyChristmas.id("reindeer"), "main");
    /** Also the texture of the sleigh's team. */
    public static final ResourceLocation TEXTURE = CozyChristmas.id("textures/entity/reindeer.png");
    private static final float SHADOW = 0.7F;
    private static final float FAWN_SCALE = 0.55F;

    public ReindeerRenderer(EntityRendererProvider.Context context) {
        super(context, new ReindeerModel(context.bakeLayer(LAYER)), SHADOW);
    }

    public static void registerLayers() {
        EntityModelLayerRegistry.register(LAYER, ReindeerModel::createLayer);
    }

    @Override
    protected void scale(Reindeer reindeer, PoseStack poseStack, float partialTick) {
        if (reindeer.isBaby()) {
            poseStack.scale(FAWN_SCALE, FAWN_SCALE, FAWN_SCALE);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(Reindeer reindeer) {
        return TEXTURE;
    }
}
