package dev.eliasnvx.cozychristmas.client.goblin;

import dev.architectury.registry.client.level.entity.EntityModelLayerRegistry;
import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.goblin.GingerbreadMan;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class GingerbreadManRenderer extends HumanoidMobRenderer<GingerbreadMan, GingerbreadManModel> {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(CozyChristmas.id("gingerbread_man"), "main");
    private static final ResourceLocation TEXTURE = CozyChristmas.id("textures/entity/gingerbread_man.png");
    private static final float SHADOW = 0.25F;

    public GingerbreadManRenderer(EntityRendererProvider.Context context) {
        super(context, new GingerbreadManModel(context.bakeLayer(LAYER)), SHADOW);
    }

    public static void registerLayers() {
        EntityModelLayerRegistry.register(LAYER, GingerbreadManModel::createLayer);
    }

    @Override
    public ResourceLocation getTextureLocation(GingerbreadMan man) {
        return TEXTURE;
    }
}
