package dev.eliasnvx.cozychristmas.forge.dev;

import dev.eliasnvx.cozychristmas.gametest.DevCheck;
import net.minecraftforge.client.gui.ModListScreen;

/** Starts the shared {@link DevCheck} with Forge's mod list. Dev runs only, not in the released jar. */
public final class DevCheckForge {

    private DevCheckForge() {
    }

    public static void start() {
        DevCheck.start(() -> new ModListScreen(null));
    }
}
