package dev.eliasnvx.cozychristmas.client.cosmetic;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Draws everything a player wears. Added to every player renderer on both loaders. */
public final class CosmeticLayer extends RenderLayer<AvatarRenderState, PlayerModel> {

    /** The base texture first, then the overlay on the same faces (like leather armor and its overlay). */
    private static final int BASE_ORDER = 0;
    private static final int OVERLAY_ORDER = 1;
    private static final int WHITE = 0xFFFFFFFF;

    /** Baked models by shape: [fit, over armor][wide, slim]. */
    private final Map<CosmeticShape, CosmeticModel[][]> models = new EnumMap<>(CosmeticShape.class);

    public CosmeticLayer(RenderLayerParent<AvatarRenderState, PlayerModel> parent, EntityModelSet modelSet) {
        super(parent);
        for (CosmeticShape shape : CosmeticShape.values()) {
            CosmeticModel[][] baked = new CosmeticModel[2][2];
            for (int size = 0; size < 2; size++) {
                for (int arms = 0; arms < 2; arms++) {
                    // the armor size has wide arms on every skin (see CosmeticShape#layer)
                    boolean slim = arms == 1 && size == 0;
                    baked[size][arms] = new CosmeticModel(modelSet.bakeLayer(shape.layer(size == 1, slim)), slim);
                }
            }
            models.put(shape, baked);
        }
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, AvatarRenderState state, float yRot, float xRot) {
        CosmeticRenderData data = ((CosmeticRenderStateAccess) state).cozychristmas$getCosmetics();
        if (data == null || state.isInvisible) {
            return;
        }
        int overlay = LivingEntityRenderer.getOverlayCoords(state, 0.0F);
        int arms = state.skin.model() == PlayerModelType.SLIM ? 1 : 0;
        List<CosmeticRenderData.Worn> worn = data.worn();
        for (int i = 0; i < worn.size(); i++) {
            CosmeticRenderData.Worn item = worn.get(i);
            CosmeticLook look = CosmeticLooks.get(item.item());
            if (look == null) {
                continue;
            }
            int size = armorShows(state, item.slot().armor()) ? 1 : 0;
            CosmeticModel model = models.get(look.shape())[size][arms];
            collector.order(BASE_ORDER).submitModel(model, state, poseStack, look.base(), light, overlay,
                    ARGB.opaque(look.baseColor(item.dye())), null, state.outlineColor);
            if (look.overlay() != null) {
                collector.order(OVERLAY_ORDER).submitModel(model, state, poseStack, look.overlay(), light, overlay,
                        WHITE, null, state.outlineColor);
            }
        }
    }

    /** Whether vanilla draws an armor model in this slot (the same check as its armor layer). */
    private static boolean armorShows(AvatarRenderState state, EquipmentSlot slot) {
        ItemStack stack = switch (slot) {
            case HEAD -> state.headEquipment;
            case CHEST -> state.chestEquipment;
            case LEGS -> state.legsEquipment;
            case FEET -> state.feetEquipment;
            default -> ItemStack.EMPTY;
        };
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        return equippable != null && equippable.slot() == slot && equippable.assetId().isPresent();
    }
}
