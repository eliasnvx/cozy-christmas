package dev.eliasnvx.cozychristmas.neoforge.dev;

import dev.eliasnvx.cozychristmas.gametest.DevCheck;
import net.neoforged.neoforge.client.gui.ModListScreen;

/** Starts the shared {@link DevCheck} with NeoForge's mod list. Dev runs only, not in the released jar. */
public final class DevCheckNeoForge {

    private DevCheckNeoForge() {
    }

    public static void start() {
        DevCheck.start(() -> new ModListScreen(null));
    }
}
