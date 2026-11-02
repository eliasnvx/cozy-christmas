package dev.eliasnvx.cozychristmas.neoforge;

import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.client.CozyChristmasClient;
import dev.eliasnvx.cozychristmas.client.cosmetic.CosmeticLayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.world.entity.player.PlayerModelType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@Mod(value = CozyChristmas.MOD_ID, dist = Dist.CLIENT)
public final class CozyChristmasNeoForgeClient {

    public CozyChristmasNeoForgeClient(IEventBus modBus) {
        CozyChristmasClient.init();
        modBus.addListener((EntityRenderersEvent.AddLayers event) -> {
            for (PlayerModelType skin : event.getSkins()) {
                AvatarRenderer<?> renderer = event.getPlayerRenderer(skin);
                if (renderer != null) {
                    renderer.addLayer(new CosmeticLayer(renderer, event.getEntityModels()));
                }
            }
        });
    }
}
