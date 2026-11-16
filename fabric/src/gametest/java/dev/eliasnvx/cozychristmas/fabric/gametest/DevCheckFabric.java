package dev.eliasnvx.cozychristmas.fabric.gametest;

import dev.eliasnvx.cozychristmas.gametest.DevCheck;
import net.fabricmc.api.ClientModInitializer;

/** Starts the shared {@link DevCheck} in {@code ./gradlew :fabric:runDevCheck} (no client GameTest API on 1.20.1). */
public final class DevCheckFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        if (Boolean.getBoolean("cozychristmas.devcheck")) {
            DevCheck.start(null);
        }
    }
}
