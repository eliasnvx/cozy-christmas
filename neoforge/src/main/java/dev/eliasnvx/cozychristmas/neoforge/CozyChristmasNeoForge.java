package dev.eliasnvx.cozychristmas.neoforge;

import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.neoforge.compat.CuriosSlots;
import dev.eliasnvx.cozychristmas.platform.neoforge.PlatformHelperImpl;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.gametest.GameTestHooks;

@Mod(CozyChristmas.MOD_ID)
public final class CozyChristmasNeoForge {

    public CozyChristmasNeoForge(IEventBus modBus) {
        PlatformHelperImpl.init(modBus);
        CozyChristmas.init();
        if (ModList.get().isLoaded(CuriosSlots.MOD_ID)) {
            CuriosSlots.register();
        }
        if (GameTestHooks.isGametestEnabled()) {
            registerGameTests(modBus); // dev runs only: the class isn't in the released jar
        }
    }

    private static void registerGameTests(IEventBus modBus) {
        try {
            Class.forName("dev.eliasnvx.cozychristmas.neoforge.dev.CozyGameTestsNeoForge")
                    .getMethod("register", IEventBus.class).invoke(null, modBus);
        } catch (ReflectiveOperationException e) {
            // fail the run: a test server that registered none (or few) of the tests would pass the build
            throw new IllegalStateException("GameTests couldn't be registered", e);
        }
    }
}
