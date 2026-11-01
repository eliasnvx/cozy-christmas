package dev.eliasnvx.cozychristmas.neoforge;

import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.platform.neoforge.PlatformHelperImpl;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.gametest.GameTestHooks;

@Mod(CozyChristmas.MOD_ID)
public final class CozyChristmasNeoForge {

    public CozyChristmasNeoForge(IEventBus modBus) {
        PlatformHelperImpl.init(modBus);
        CozyChristmas.init();
        if (GameTestHooks.isGametestEnabled()) {
            registerGameTests(modBus); // dev runs only: the class isn't in the released jar
        }
    }

    private static void registerGameTests(IEventBus modBus) {
        try {
            Class.forName("dev.eliasnvx.cozychristmas.neoforge.dev.CozyGameTestsNeoForge")
                    .getMethod("register", IEventBus.class).invoke(null, modBus);
        } catch (ReflectiveOperationException e) {
            CozyChristmas.LOGGER.error("GameTests couldn't be registered", e);
        }
    }
}
