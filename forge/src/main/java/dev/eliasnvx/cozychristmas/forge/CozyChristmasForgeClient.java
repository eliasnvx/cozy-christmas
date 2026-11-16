package dev.eliasnvx.cozychristmas.forge;

import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.client.CozyChristmasClient;
import dev.eliasnvx.cozychristmas.client.cosmetic.CosmeticLayer;
import dev.eliasnvx.cozychristmas.client.present.PresentRenderer;
import dev.eliasnvx.cozychristmas.client.present.WrappingTableScreen;
import dev.eliasnvx.cozychristmas.registry.CozyBlocks;
import dev.eliasnvx.cozychristmas.registry.CozyMenus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/** Client-only setup, called from {@link CozyChristmasForge} on the physical client. */
final class CozyChristmasForgeClient {

    private CozyChristmasForgeClient() {
    }

    static void init(IEventBus modBus) {
        CozyChristmasClient.init();
        if (Boolean.getBoolean("cozychristmas.devcheck")) {
            startDevCheck(); // dev runs only: the class isn't in the released jar
        }
        modBus.addListener((EntityRenderersEvent.AddLayers event) -> {
            for (String skin : event.getSkins()) {
                EntityRenderer<? extends Player> renderer = event.getSkin(skin);
                if (renderer instanceof PlayerRenderer player) {
                    player.addLayer(new CosmeticLayer(player, event.getEntityModels()));
                }
            }
        });
        modBus.addListener((EntityRenderersEvent.RegisterRenderers event) ->
                event.registerBlockEntityRenderer(CozyBlocks.PRESENT_ENTITY.get(), PresentRenderer::new));
        // Forge 47 has no menu screen event; MenuScreens isn't thread-safe: register on the main thread
        modBus.addListener((FMLClientSetupEvent event) -> event.enqueueWork(() ->
                MenuScreens.register(CozyMenus.WRAPPING_TABLE.get(), WrappingTableScreen::new)));
    }

    private static void startDevCheck() {
        try {
            Class.forName("dev.eliasnvx.cozychristmas.forge.dev.DevCheckForge").getMethod("start").invoke(null);
        } catch (ReflectiveOperationException e) {
            CozyChristmas.LOGGER.error("DEVCHECK couldn't start", e);
        }
    }
}
