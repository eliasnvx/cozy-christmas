package dev.eliasnvx.cozychristmas.client.cosmetic;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.eliasnvx.cozychristmas.cosmetic.CosmeticInventory;
import dev.eliasnvx.cozychristmas.cosmetic.CosmeticSlot;
import dev.eliasnvx.cozychristmas.cosmetic.CosmeticsManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

/**
 * The sweater sleeve and the mitten on the first-person hand, called from PlayerRendererHandMixin. The first-person
 * arm never shows armor, so the fit size is used.
 */
public final class FirstPersonCosmetics {

    private static final CosmeticSlot[] ON_ARMS = {CosmeticSlot.BODY, CosmeticSlot.HANDS};

    /** Baked fit-size roots by shape: [wide, slim]. */
    private static final Map<CosmeticShape, ModelPart[]> BAKED = new EnumMap<>(CosmeticShape.class);
    private static @Nullable EntityModelSet bakedFrom;

    private FirstPersonCosmetics() {
    }

    public static void render(PlayerModel<AbstractClientPlayer> model, ModelPart arm, PoseStack pose, MultiBufferSource buffers,
                              int light, AbstractClientPlayer player) {
        CosmeticInventory worn = CosmeticsManager.looks(player);
        if (worn.isEmpty()) {
            return;
        }
        EntityModelSet models = Minecraft.getInstance().getEntityModels();
        if (models != bakedFrom) { // re-bake after resource reloads
            bake(models);
        }
        int arms = PlayerPose.SLIM.equals(player.getModelName()) ? 1 : 0;
        String side = arm == model.rightArm ? "right_arm" : "left_arm";
        for (CosmeticSlot slot : ON_ARMS) {
            ItemStack stack = worn.get(slot);
            CosmeticLook look = stack.isEmpty() ? null : CosmeticLooks.get(stack.getItem());
            ModelPart[] roots = look == null ? null : BAKED.get(look.shape());
            if (roots == null) {
                continue;
            }
            ModelPart part = roots[arms].getChild(side);
            part.copyFrom(arm); // field by field: loadPose(storePose()) would make a PartPose every frame
            int rgb = look.baseColor(CosmeticRenderData.dyeOf(stack));
            part.render(pose, buffers.getBuffer(look.base()), light, OverlayTexture.NO_OVERLAY,
                    CosmeticLook.red(rgb), CosmeticLook.green(rgb), CosmeticLook.blue(rgb), 1.0F);
            if (look.overlay() != null) {
                part.render(pose, buffers.getBuffer(look.overlay()), light, OverlayTexture.NO_OVERLAY);
            }
        }
    }

    private static void bake(EntityModelSet models) {
        BAKED.clear();
        for (CosmeticShape shape : new CosmeticShape[]{CosmeticShape.SWEATER, CosmeticShape.MITTENS}) {
            BAKED.put(shape, new ModelPart[]{shape.bake(models, false, false), shape.bake(models, false, true)});
        }
        bakedFrom = models;
    }
}
