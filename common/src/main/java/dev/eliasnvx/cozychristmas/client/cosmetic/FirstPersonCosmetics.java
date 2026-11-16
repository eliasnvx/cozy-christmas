package dev.eliasnvx.cozychristmas.client.cosmetic;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.eliasnvx.cozychristmas.cosmetic.CosmeticInventory;
import dev.eliasnvx.cozychristmas.cosmetic.CosmeticSlot;
import dev.eliasnvx.cozychristmas.cosmetic.CosmeticsManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

/**
 * The sweater sleeve and the mitten on the first-person hand, called from AvatarRendererHandMixin. The first-person
 * arm never shows armor, so the fit size is used.
 */
public final class FirstPersonCosmetics {

    private static final CosmeticSlot[] ON_ARMS = {CosmeticSlot.BODY, CosmeticSlot.HANDS};
    private static final int BASE_ORDER = 0;
    private static final int OVERLAY_ORDER = 1;
    private static final int WHITE = 0xFFFFFFFF;

    /** Baked fit-size roots by shape: [wide, slim]. */
    private static final Map<CosmeticShape, ModelPart[]> BAKED = new EnumMap<>(CosmeticShape.class);
    private static @Nullable EntityModelSet bakedFrom;

    private FirstPersonCosmetics() {
    }

    public static void submit(PlayerModel model, ModelPart arm, PoseStack pose, SubmitNodeCollector collector, int light) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        CosmeticInventory worn = CosmeticsManager.worn(player);
        if (worn.isEmpty()) {
            return;
        }
        EntityModelSet models = Minecraft.getInstance().getEntityModels();
        if (models != bakedFrom) { // re-bake after resource reloads
            bake(models);
        }
        int arms = player.getSkin().model() == PlayerModelType.SLIM ? 1 : 0;
        String side = arm == model.rightArm ? "right_arm" : "left_arm";
        for (CosmeticSlot slot : ON_ARMS) {
            ItemStack stack = worn.get(slot);
            CosmeticLook look = stack.isEmpty() ? null : CosmeticLooks.get(stack.getItem());
            ModelPart[] roots = look == null ? null : BAKED.get(look.shape());
            if (roots == null) {
                continue;
            }
            ModelPart part = roots[arms].getChild(side);
            copyPose(arm, part);
            collector.order(BASE_ORDER).submitModelPart(part, pose, look.base(), light, OverlayTexture.NO_OVERLAY, null,
                    ARGB.opaque(look.baseColor(CosmeticRenderData.dyeOf(stack))));
            if (look.overlay() != null) {
                collector.order(OVERLAY_ORDER).submitModelPart(part, pose, look.overlay(), light, OverlayTexture.NO_OVERLAY,
                        null, WHITE);
            }
        }
    }

    private static void bake(EntityModelSet models) {
        BAKED.clear();
        for (CosmeticShape shape : new CosmeticShape[]{CosmeticShape.SWEATER, CosmeticShape.MITTENS}) {
            BAKED.put(shape, new ModelPart[]{models.bakeLayer(shape.layer(false, false)), models.bakeLayer(shape.layer(false, true))});
        }
        bakedFrom = models;
    }

    /** The arm's pose onto the piece, field by field: storePose/loadPose would make a PartPose every frame. */
    private static void copyPose(ModelPart from, ModelPart to) {
        to.x = from.x;
        to.y = from.y;
        to.z = from.z;
        to.xRot = from.xRot;
        to.yRot = from.yRot;
        to.zRot = from.zRot;
        to.xScale = from.xScale;
        to.yScale = from.yScale;
        to.zScale = from.zScale;
    }
}
