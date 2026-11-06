package dev.eliasnvx.cozychristmas.client.workshop;

import dev.architectury.registry.client.level.entity.EntityModelLayerRegistry;
import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.workshop.Santa;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.Identifier;

public final class SantaRenderer extends HumanoidMobRenderer<Santa, SantaRenderState, SantaModel> {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(CozyChristmas.id("santa"), "main");
    private static final Identifier TEXTURE = CozyChristmas.id("textures/entity/santa.png");
    private static final float SHADOW = 0.6F;

    public SantaRenderer(EntityRendererProvider.Context context) {
        super(context, new SantaModel(context.bakeLayer(LAYER)), SHADOW);
    }

    public static void registerLayers() {
        EntityModelLayerRegistry.register(LAYER, SantaModel::createLayer);
    }

    @Override
    public SantaRenderState createRenderState() {
        return new SantaRenderState();
    }

    @Override
    public void extractRenderState(Santa santa, SantaRenderState state, float partialTicks) {
        super.extractRenderState(santa, state, partialTicks);
        state.unhappy = santa.getUnhappyCounter() > 0;
    }

    @Override
    public Identifier getTextureLocation(SantaRenderState state) {
        return TEXTURE;
    }
}
