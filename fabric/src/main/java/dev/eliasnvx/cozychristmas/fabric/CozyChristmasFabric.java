package dev.eliasnvx.cozychristmas.fabric;

import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.platform.fabric.PlatformHelperImpl;
import net.fabricmc.api.ModInitializer;

public final class CozyChristmasFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        PlatformHelperImpl.init();
        CozyChristmas.init();
    }
}
