package dev.eliasnvx.cozychristmas.season;

import dev.eliasnvx.cozychristmas.config.CozyConfig;

import java.time.Clock;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.Objects;

/**
 * Is it Christmas? Server side: the server's date decides, clients learn the result from the server once a feature
 * needs it there. The rules are static functions of (settings, date) so they are unit-tested without a game.
 */
public final class ChristmasSeason {

    private static volatile Clock clock = Clock.systemDefaultZone();

    private ChristmasSeason() {
    }

    /** Seasonal magic on: reindeer fly, free presents spawn, Santa gives daily gifts. */
    public static boolean isActive() {
        return isActive(CozyConfig.common().season(), today());
    }

    /** Santa's workshop loads the sleigh (24-25 December by default). Always inside the season. */
    public static boolean isWorkshopDelivery() {
        return isWorkshopDelivery(CozyConfig.common().season(), today());
    }

    public static boolean isActive(SeasonSettings settings, LocalDate date) {
        return switch (settings.source()) {
            case REAL_DATE -> settings.season().contains(MonthDay.from(date));
            case ALWAYS -> true;
            case NEVER -> false;
        };
    }

    /** {@link SeasonSource#ALWAYS} keeps the delivery on its real days, so the event still feels like an event. */
    public static boolean isWorkshopDelivery(SeasonSettings settings, LocalDate date) {
        return isActive(settings, date) && settings.workshopDelivery().contains(MonthDay.from(date));
    }

    public static LocalDate today() {
        return LocalDate.now(clock);
    }

    /** GameTests and dev checks pin the date; production never calls this. */
    public static void setClock(Clock newClock) {
        clock = Objects.requireNonNull(newClock);
    }
}
