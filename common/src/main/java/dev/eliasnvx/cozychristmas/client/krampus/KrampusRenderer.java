package dev.eliasnvx.cozychristmas.client.krampus;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.registry.client.level.entity.EntityModelLayerRegistry;
import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.krampus.Krampus;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

/** Krampus, bigger than life, with ember eyes that glow in the dark. */
public final class KrampusRenderer extends HumanoidMobRenderer<Krampus, KrampusModel> {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(CozyChristmas.id("krampus"), "main");
    /** A player-sized model drawn this much bigger: with his horns he's taller than the doorway. */
    public static final float SCALE = 1.35F;
    private static final ResourceLocation TEXTURE = CozyChristmas.id("textures/entity/krampus.png");
    private static final RenderType EYES = RenderType.eyes(CozyChristmas.id("textures/entity/krampus_eyes.png"));
    private static final float SHADOW = 0.8F;

    public KrampusRenderer(EntityRendererProvider.Context context) {
        super(context, new KrampusModel(context.bakeLayer(LAYER)), SHADOW);
        addLayer(new EyesLayer<>(this) {
            @Override
            public RenderType renderType() {
                return EYES;
            }
        });
    }

    public static void registerLayers() {
        EntityModelLayerRegistry.register(LAYER, KrampusModel::createLayer);
    }

    @Override
    protected void scale(Krampus krampus, PoseStack poseStack, float partialTick) {
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    @Override
    public ResourceLocation getTextureLocation(Krampus krampus) {
        return TEXTURE;
    }
}
