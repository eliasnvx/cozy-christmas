package dev.eliasnvx.cozychristmas.fabric;

import dev.eliasnvx.cozychristmas.client.CozyChristmasClient;
import dev.eliasnvx.cozychristmas.client.cosmetic.CosmeticLayer;
import dev.eliasnvx.cozychristmas.client.present.PresentRenderer;
import dev.eliasnvx.cozychristmas.client.present.WrappingTableScreen;
import dev.eliasnvx.cozychristmas.registry.CozyBlocks;
import dev.eliasnvx.cozychristmas.registry.CozyMenus;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;

public final class CozyChristmasFabricClient implements ClientModInitializer {

    @Override
    @SuppressWarnings("unchecked")
    public void onInitializeClient() {
        CozyChristmasClient.init();
        BlockEntityRenderers.register(CozyBlocks.PRESENT_ENTITY.get(), PresentRenderer::new);
        MenuScreens.register(CozyMenus.WRAPPING_TABLE.get(), WrappingTableScreen::new);
        LivingEntityRenderLayerRegistrationCallback.EVENT.register((type, renderer, helper, context) -> {
            if (renderer instanceof AvatarRenderer<?> avatar) {
                helper.register(new CosmeticLayer((RenderLayerParent<AvatarRenderState, PlayerModel>) avatar, context.getModelSet()));
            }
        });
    }
}
