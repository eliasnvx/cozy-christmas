package dev.eliasnvx.cozychristmas.client.workshop;

import dev.architectury.registry.client.level.entity.EntityModelLayerRegistry;
import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.workshop.Santa;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class SantaRenderer extends HumanoidMobRenderer<Santa, SantaModel> {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(CozyChristmas.id("santa"), "main");
    private static final ResourceLocation TEXTURE = CozyChristmas.id("textures/entity/santa.png");
    private static final float SHADOW = 0.6F;

    public SantaRenderer(EntityRendererProvider.Context context) {
        super(context, new SantaModel(context.bakeLayer(LAYER)), SHADOW);
    }

    public static void registerLayers() {
        EntityModelLayerRegistry.register(LAYER, SantaModel::createLayer);
    }

    @Override
    public ResourceLocation getTextureLocation(Santa santa) {
        return TEXTURE;
    }
}
