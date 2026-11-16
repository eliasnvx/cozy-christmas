package dev.eliasnvx.cozychristmas.neoforge;

import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.client.CozyChristmasClient;
import dev.eliasnvx.cozychristmas.client.cosmetic.CosmeticLayer;
import dev.eliasnvx.cozychristmas.client.present.PresentRenderer;
import dev.eliasnvx.cozychristmas.client.present.WrappingTableScreen;
import dev.eliasnvx.cozychristmas.registry.CozyBlocks;
import dev.eliasnvx.cozychristmas.registry.CozyMenus;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@Mod(value = CozyChristmas.MOD_ID, dist = Dist.CLIENT)
public final class CozyChristmasNeoForgeClient {

    public CozyChristmasNeoForgeClient(IEventBus modBus) {
        CozyChristmasClient.init();
        if (Boolean.getBoolean("cozychristmas.devcheck")) {
            startDevCheck(); // dev runs only: the class isn't in the released jar
        }
        modBus.addListener((EntityRenderersEvent.AddLayers event) -> {
            for (PlayerSkin.Model skin : event.getSkins()) {
                if (event.getSkin(skin) instanceof PlayerRenderer renderer) {
                    renderer.addLayer(new CosmeticLayer(renderer, event.getEntityModels()));
                }
            }
        });
        modBus.addListener((EntityRenderersEvent.RegisterRenderers event) ->
                event.registerBlockEntityRenderer(CozyBlocks.PRESENT_ENTITY.get(), PresentRenderer::new));
        modBus.addListener((RegisterMenuScreensEvent event) ->
                event.register(CozyMenus.WRAPPING_TABLE.get(), WrappingTableScreen::new));
    }

    private static void startDevCheck() {
        try {
            Class.forName("dev.eliasnvx.cozychristmas.neoforge.dev.DevCheckNeoForge").getMethod("start").invoke(null);
        } catch (ReflectiveOperationException e) {
            CozyChristmas.LOGGER.error("DEVCHECK couldn't start", e);
        }
    }
}
