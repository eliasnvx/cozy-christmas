package dev.eliasnvx.cozychristmas.client.workshop;

import dev.architectury.registry.client.level.entity.EntityModelLayerRegistry;
import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.workshop.Elf;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class ElfRenderer extends HumanoidMobRenderer<Elf, ElfModel> {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(CozyChristmas.id("elf"), "main");
    private static final ResourceLocation TEXTURE = CozyChristmas.id("textures/entity/elf.png");
    private static final float SHADOW = 0.3F;

    public ElfRenderer(EntityRendererProvider.Context context) {
        super(context, new ElfModel(context.bakeLayer(LAYER)), SHADOW);
    }

    public static void registerLayers() {
        EntityModelLayerRegistry.register(LAYER, ElfModel::createLayer);
    }

    @Override
    public ResourceLocation getTextureLocation(Elf elf) {
        return TEXTURE;
    }
}
