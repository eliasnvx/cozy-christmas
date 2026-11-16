package dev.eliasnvx.cozychristmas.forge;

import dev.architectury.platform.forge.EventBuses;
import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.cosmetic.CosmeticsEvents;
import dev.eliasnvx.cozychristmas.forge.compat.CuriosSlots;
import dev.eliasnvx.cozychristmas.platform.forge.PlatformHelperImpl;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.gametest.ForgeGameTestHooks;

@Mod(CozyChristmas.MOD_ID)
public final class CozyChristmasForge {

    public CozyChristmasForge(FMLJavaModLoadingContext context) {
        IEventBus modBus = context.getModEventBus();
        // Architectury's DeferredRegister and events on Forge need the mod bus before any registration
        EventBuses.registerModEventBus(CozyChristmas.MOD_ID, modBus);
        PlatformHelperImpl.init(modBus);
        CozyChristmas.init();
        if (ModList.get().isLoaded(CuriosSlots.MOD_ID)) {
            CuriosSlots.register();
        }
        if (ForgeGameTestHooks.isGametestEnabled()) {
            registerGameTests(modBus); // dev runs only: the class isn't in the released jar
        }
        // Architectury 9 has no start-tracking event: sync the outfits of players coming into view
        MinecraftForge.EVENT_BUS.addListener((PlayerEvent.StartTracking event) -> {
            if (event.getEntity() instanceof ServerPlayer watcher) {
                CosmeticsEvents.onStartTracking(event.getTarget(), watcher);
            }
        });
        // Forge 47's @Mod has no dist attribute: client-only setup lives in a class only loaded on the client
        if (FMLEnvironment.dist == Dist.CLIENT) {
            CozyChristmasForgeClient.init(modBus);
        }
    }

    private static void registerGameTests(IEventBus modBus) {
        try {
            Class.forName("dev.eliasnvx.cozychristmas.forge.dev.CozyGameTestsForge")
                    .getMethod("register", IEventBus.class).invoke(null, modBus);
        } catch (ReflectiveOperationException e) {
            // fail the run: a test server that registered none (or few) of the tests would pass the build
            throw new IllegalStateException("GameTests couldn't be registered", e);
        }
    }
}
