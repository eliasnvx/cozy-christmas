package dev.eliasnvx.cozychristmas.client.goblin;

import dev.architectury.registry.client.level.entity.EntityModelLayerRegistry;
import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.goblin.PresentGoblin;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class PresentGoblinRenderer extends HumanoidMobRenderer<PresentGoblin, PresentGoblinModel> {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(CozyChristmas.id("present_goblin"), "main");
    private static final ResourceLocation TEXTURE = CozyChristmas.id("textures/entity/present_goblin.png");
    private static final float SHADOW = 0.3F;

    public PresentGoblinRenderer(EntityRendererProvider.Context context) {
        super(context, new PresentGoblinModel(context.bakeLayer(LAYER)), SHADOW);
    }

    public static void registerLayers() {
        EntityModelLayerRegistry.register(LAYER, PresentGoblinModel::createLayer);
    }

    @Override
    public ResourceLocation getTextureLocation(PresentGoblin goblin) {
        return TEXTURE;
    }
}
