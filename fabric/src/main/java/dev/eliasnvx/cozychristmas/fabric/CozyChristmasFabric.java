package dev.eliasnvx.cozychristmas.fabric;

import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.cosmetic.CosmeticsEvents;
import dev.eliasnvx.cozychristmas.platform.fabric.PlatformHelperImpl;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;

public final class CozyChristmasFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        PlatformHelperImpl.init();
        CozyChristmas.init();
        // Architectury 13 has no start-tracking event: sync the outfits of players coming into view
        EntityTrackingEvents.START_TRACKING.register(CosmeticsEvents::onStartTracking);
    }
}
