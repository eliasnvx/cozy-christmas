package dev.eliasnvx.cozychristmas.client.snowman;

import dev.architectury.registry.client.level.entity.EntityModelLayerRegistry;
import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.snowman.EvilSnowman;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

/** The snowman, and its icy eyes glowing in the dark like a spider's ({@link EyesLayer}). */
public final class EvilSnowmanRenderer extends MobRenderer<EvilSnowman, EvilSnowmanModel> {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(CozyChristmas.id("evil_snowman"), "main");
    private static final ResourceLocation TEXTURE = CozyChristmas.id("textures/entity/evil_snowman.png");
    private static final RenderType EYES = RenderType.eyes(CozyChristmas.id("textures/entity/evil_snowman_eyes.png"));
    private static final float SHADOW = 0.5F;

    public EvilSnowmanRenderer(EntityRendererProvider.Context context) {
        super(context, new EvilSnowmanModel(context.bakeLayer(LAYER)), SHADOW);
        addLayer(new EyesLayer<>(this) {
            @Override
            public RenderType renderType() {
                return EYES;
            }
        });
    }

    public static void registerLayers() {
        EntityModelLayerRegistry.register(LAYER, EvilSnowmanModel::createLayer);
    }

    @Override
    public ResourceLocation getTextureLocation(EvilSnowman snowman) {
        return TEXTURE;
    }
}
