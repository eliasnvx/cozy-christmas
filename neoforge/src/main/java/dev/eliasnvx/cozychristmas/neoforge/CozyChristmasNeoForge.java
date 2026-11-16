package dev.eliasnvx.cozychristmas.neoforge;

import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.cosmetic.CosmeticsEvents;
import dev.eliasnvx.cozychristmas.neoforge.compat.CuriosSlots;
import dev.eliasnvx.cozychristmas.platform.neoforge.PlatformHelperImpl;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
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
        // Architectury 13 has no start-tracking event: sync the outfits of players coming into view
        NeoForge.EVENT_BUS.addListener((PlayerEvent.StartTracking event) -> {
            if (event.getEntity() instanceof ServerPlayer watcher) {
                CosmeticsEvents.onStartTracking(event.getTarget(), watcher);
            }
        });
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
