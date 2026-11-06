package dev.eliasnvx.cozychristmas.client.workshop;

import dev.architectury.registry.client.level.entity.EntityModelLayerRegistry;
import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.workshop.Elf;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.Identifier;

public final class ElfRenderer extends HumanoidMobRenderer<Elf, ElfRenderState, ElfModel> {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(CozyChristmas.id("elf"), "main");
    private static final Identifier TEXTURE = CozyChristmas.id("textures/entity/elf.png");
    private static final float SHADOW = 0.3F;

    public ElfRenderer(EntityRendererProvider.Context context) {
        super(context, new ElfModel(context.bakeLayer(LAYER)), SHADOW);
    }

    public static void registerLayers() {
        EntityModelLayerRegistry.register(LAYER, ElfModel::createLayer);
    }

    @Override
    public ElfRenderState createRenderState() {
        return new ElfRenderState();
    }

    @Override
    public void extractRenderState(Elf elf, ElfRenderState state, float partialTicks) {
        super.extractRenderState(elf, state, partialTicks);
        state.carrying = elf.isCarryingPresent();
    }

    @Override
    public Identifier getTextureLocation(ElfRenderState state) {
        return TEXTURE;
    }
}
