package dev.eliasnvx.cozychristmas.compat.sereneseasons;

import dev.architectury.event.events.common.LifecycleEvent;
import dev.eliasnvx.cozychristmas.season.ChristmasSeason;
import dev.eliasnvx.cozychristmas.season.SeasonMoment;
import dev.eliasnvx.cozychristmas.season.SubSeason;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;
import sereneseasons.api.season.ISeasonState;
import sereneseasons.api.season.SeasonHelper;

import java.util.Optional;

/**
 * Serene Seasons (SPEC compat, as Snowy Spirit does it): its Overworld calendar as the Christmas season's source
 * ({@code season.source: serene_seasons}). Registered only when that mod is installed; it reads the running server's
 * Overworld, so a client without one (or a world without seasons) falls back to the date.
 */
public final class SereneSeasonsCalendar {

    public static final String MOD_ID = "sereneseasons";

    private static volatile @Nullable MinecraftServer server;

    private SereneSeasonsCalendar() {
    }

    public static void register() {
        // the Overworld loading, not the server starting: the Fabric GameTest server doesn't pass the latter on
        LifecycleEvent.SERVER_LEVEL_LOAD.register(level -> server = level.getServer());
        LifecycleEvent.SERVER_STOPPED.register(stopped -> server = null);
        ChristmasSeason.setCalendar(SereneSeasonsCalendar::now);
    }

    private static Optional<SeasonMoment> now() {
        MinecraftServer current = server;
        if (current == null || !SeasonHelper.hasSeasons(current.overworld())) {
            return Optional.empty();
        }
        ISeasonState state = SeasonHelper.getSeasonState(current.overworld());
        int subSeasonTicks = Math.max(1, state.getSubSeasonDuration());
        int dayTicks = Math.max(1, state.getDayDuration());
        int day = (state.getSeasonCycleTicks() % subSeasonTicks) / dayTicks;
        return Optional.of(new SeasonMoment(SubSeason.valueOf(state.getSubSeason().name()), day));
    }
}
