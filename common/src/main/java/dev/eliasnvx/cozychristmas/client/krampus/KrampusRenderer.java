package dev.eliasnvx.cozychristmas.client.krampus;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.registry.client.level.entity.EntityModelLayerRegistry;
import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.krampus.Krampus;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/** Krampus, bigger than life, with ember eyes that glow in the dark. */
public final class KrampusRenderer extends HumanoidMobRenderer<Krampus, KrampusRenderState, KrampusModel> {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(CozyChristmas.id("krampus"), "main");
    /** A player-sized model drawn this much bigger: with his horns he's taller than the doorway. */
    public static final float SCALE = 1.35F;
    private static final Identifier TEXTURE = CozyChristmas.id("textures/entity/krampus.png");
    private static final RenderType EYES = RenderTypes.eyes(CozyChristmas.id("textures/entity/krampus_eyes.png"));
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
    public KrampusRenderState createRenderState() {
        return new KrampusRenderState();
    }

    @Override
    public void extractRenderState(Krampus krampus, KrampusRenderState state, float partialTicks) {
        super.extractRenderState(krampus, state, partialTicks);
        state.lashProgress = krampus.lashProgress(partialTicks);
        state.sackProgress = krampus.sackProgress(partialTicks);
        state.roarProgress = krampus.roarProgress(partialTicks);
    }

    @Override
    protected void scale(KrampusRenderState state, PoseStack poseStack) {
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    @Override
    public Identifier getTextureLocation(KrampusRenderState state) {
        return TEXTURE;
    }
}
