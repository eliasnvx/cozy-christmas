package dev.eliasnvx.cozychristmas.client.cosmetic;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Draws everything a player wears. Added to every player renderer on both loaders. Like leather armor, the dyed base
 * is drawn first and the overlay right after it on the same faces.
 */
public final class CosmeticLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    private static final int WHITE = 0xFFFFFFFF;

    /** Baked models by shape: [fit, over armor][wide, slim]. */
    private final Map<CosmeticShape, CosmeticModel[][]> models = new EnumMap<>(CosmeticShape.class);
    /** Filled per draw; no render states in 1.21.1. */
    private final PlayerPose state = new PlayerPose();

    public CosmeticLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent, EntityModelSet modelSet) {
        super(parent);
        for (CosmeticShape shape : CosmeticShape.values()) {
            CosmeticModel[][] baked = new CosmeticModel[2][2];
            for (int size = 0; size < 2; size++) {
                for (int arms = 0; arms < 2; arms++) {
                    // the armor size has wide arms on every skin (see CosmeticShape#layer)
                    boolean slim = arms == 1 && size == 0;
                    baked[size][arms] = new CosmeticModel(shape.bake(modelSet, size == 1, slim), slim);
                }
            }
            models.put(shape, baked);
        }
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffers, int light, AbstractClientPlayer player, float limbSwing,
                       float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (player.isInvisible()) {
            return;
        }
        state.capture(player, limbSwing, limbSwingAmount, ageInTicks);
        CosmeticRenderData data = CosmeticRenderData.capture(player, state);
        if (data == null) {
            return;
        }
        state.cosmetics = data;
        int overlay = LivingEntityRenderer.getOverlayCoords(player, 0.0F);
        int arms = state.slim ? 1 : 0;
        PlayerModel<AbstractClientPlayer> parent = getParentModel();
        List<CosmeticRenderData.Worn> worn = data.worn();
        for (int i = 0; i < worn.size(); i++) {
            CosmeticRenderData.Worn item = worn.get(i);
            CosmeticLook look = CosmeticLooks.get(item.item());
            if (look == null) {
                continue;
            }
            int size = armorShows(player, item.slot().armor()) ? 1 : 0;
            CosmeticModel model = models.get(look.shape())[size][arms];
            model.poseLike(parent);
            model.setupAnim(state);
            model.renderToBuffer(poseStack, buffers.getBuffer(look.base()), light, overlay,
                    FastColor.ARGB32.opaque(look.baseColor(item.dye())));
            if (look.overlay() != null) {
                model.renderToBuffer(poseStack, buffers.getBuffer(look.overlay()), light, overlay, WHITE);
            }
        }
    }

    /** Whether vanilla draws an armor model in this slot (the same check as its armor layer). */
    private static boolean armorShows(Player player, EquipmentSlot slot) {
        return player.getItemBySlot(slot).getItem() instanceof ArmorItem armor && armor.getEquipmentSlot() == slot;
    }
}
