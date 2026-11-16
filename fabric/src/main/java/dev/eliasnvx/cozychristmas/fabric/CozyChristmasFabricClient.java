package dev.eliasnvx.cozychristmas.fabric;

import dev.eliasnvx.cozychristmas.client.CozyChristmasClient;
import dev.eliasnvx.cozychristmas.client.cosmetic.CosmeticLayer;
import dev.eliasnvx.cozychristmas.client.present.PresentRenderer;
import dev.eliasnvx.cozychristmas.client.present.WrappingTableScreen;
import dev.eliasnvx.cozychristmas.registry.CozyBlocks;
import dev.eliasnvx.cozychristmas.registry.CozyMenus;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;

public final class CozyChristmasFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        CozyChristmasClient.init();
        BlockEntityRenderers.register(CozyBlocks.PRESENT_ENTITY.get(), PresentRenderer::new);
        MenuScreens.register(CozyMenus.WRAPPING_TABLE.get(), WrappingTableScreen::new);
        LivingEntityFeatureRendererRegistrationCallback.EVENT.register((type, renderer, helper, context) -> {
            if (renderer instanceof PlayerRenderer player) {
                helper.register(new CosmeticLayer(player, context.getModelSet()));
            }
        });
    }
}
