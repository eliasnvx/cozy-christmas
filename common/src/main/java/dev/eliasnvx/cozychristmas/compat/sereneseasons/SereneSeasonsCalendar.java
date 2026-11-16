package dev.eliasnvx.cozychristmas.compat.sereneseasons;

import dev.architectury.event.events.common.LifecycleEvent;
import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.season.ChristmasSeason;
import dev.eliasnvx.cozychristmas.season.SeasonMoment;
import dev.eliasnvx.cozychristmas.season.SubSeason;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;
import sereneseasons.api.season.ISeasonState;
import sereneseasons.api.season.SeasonHelper;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Optional;

/**
 * Serene Seasons (SPEC compat, as Snowy Spirit does it): its Overworld calendar as the Christmas season's source
 * ({@code season.source: serene_seasons}). Registered only when that mod is installed; it reads the running server's
 * Overworld, so a client without one (or a world without seasons) falls back to the date.
 */
public final class SereneSeasonsCalendar {

    public static final String MOD_ID = "sereneseasons";

    private static volatile @Nullable MinecraftServer server;
    /** {@code ModConfig.seasons.isDimensionWhitelisted(ResourceKey<Level>)}; null if it couldn't be found. */
    private static @Nullable MethodHandle whitelisted;

    private SereneSeasonsCalendar() {
    }

    public static void register() {
        whitelisted = findWhitelistCheck();
        // the Overworld loading, not the server starting: the Fabric GameTest server doesn't pass the latter on
        LifecycleEvent.SERVER_LEVEL_LOAD.register(level -> server = level.getServer());
        LifecycleEvent.SERVER_STOPPED.register(stopped -> server = null);
        ChristmasSeason.setCalendar(SereneSeasonsCalendar::now);
    }

    private static Optional<SeasonMoment> now() {
        MinecraftServer current = server;
        if (current == null || !hasSeasons(current)) {
            return Optional.empty();
        }
        ISeasonState state = SeasonHelper.getSeasonState(current.overworld());
        int subSeasonTicks = Math.max(1, state.getSubSeasonDuration());
        int dayTicks = Math.max(1, state.getDayDuration());
        int day = (state.getSeasonCycleTicks() % subSeasonTicks) / dayTicks;
        return Optional.of(new SeasonMoment(SubSeason.valueOf(state.getSubSeason().name()), day));
    }

    /** Its 1.20.1 API has no {@code hasSeasons}: the Overworld has seasons when its config whitelists it. */
    private static boolean hasSeasons(MinecraftServer current) {
        MethodHandle check = whitelisted;
        if (check == null) {
            return true;
        }
        try {
            return (boolean) check.invokeExact(current.overworld().dimension());
        } catch (Throwable e) {
            return true; // its config isn't loaded yet: assume the default (the Overworld has seasons)
        }
    }

    /**
     * The config's class extends GlitchCore's, which isn't on our compile classpath, so the check is looked up by
     * name: {@code ModConfig.seasons.isDimensionWhitelisted(key)}, bound late because the config loads after us.
     */
    private static @Nullable MethodHandle findWhitelistCheck() {
        try {
            ClassLoader loader = SereneSeasonsCalendar.class.getClassLoader();
            Class<?> modConfig = Class.forName("sereneseasons.init.ModConfig", false, loader);
            Class<?> seasonsConfig = Class.forName("sereneseasons.config.SeasonsConfig", false, loader);
            MethodHandles.Lookup lookup = MethodHandles.publicLookup();
            MethodHandle config = lookup.findStaticGetter(modConfig, "seasons", seasonsConfig);
            MethodHandle check = lookup.findVirtual(seasonsConfig, "isDimensionWhitelisted",
                    MethodType.methodType(boolean.class, ResourceKey.class));
            // (key) -> check(config(), key)
            return MethodHandles.collectArguments(check, 0, config)
                    .asType(MethodType.methodType(boolean.class, ResourceKey.class));
        } catch (ReflectiveOperationException e) {
            CozyChristmas.LOGGER.warn("Serene Seasons' dimension whitelist couldn't be read; assuming the Overworld has seasons", e);
            return null;
        }
    }
}
