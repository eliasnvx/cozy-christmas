package dev.eliasnvx.cozychristmas.season;

import dev.eliasnvx.cozychristmas.config.CozyConfig;

import java.time.Clock;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Is it Christmas? Server side: the server's date decides, clients learn the result from the server once a feature
 * needs it there. The rules are static functions of (settings, date) so they are unit-tested without a game.
 */
public final class ChristmasSeason {

    private static volatile Clock clock = Clock.systemDefaultZone();
    /** A seasons mod's calendar, set by its compat when installed and a server runs; empty otherwise. */
    private static volatile Supplier<Optional<SeasonMoment>> calendar = Optional::empty;

    private ChristmasSeason() {
    }

    /** Seasonal magic on: reindeer fly, free presents spawn, Santa gives daily gifts. */
    public static boolean isActive() {
        return isActive(CozyConfig.common().season(), today(), calendar.get());
    }

    /** Santa's workshop loads the sleigh (24-25 December by default). Always inside the season. */
    public static boolean isWorkshopDelivery() {
        return isWorkshopDelivery(CozyConfig.common().season(), today(), calendar.get());
    }

    public static boolean isActive(SeasonSettings settings, LocalDate date) {
        return isActive(settings, date, Optional.empty());
    }

    /** {@code moment}: the seasons mod's calendar, if it's installed; without it Serene Seasons falls back to the date. */
    public static boolean isActive(SeasonSettings settings, LocalDate date, Optional<SeasonMoment> moment) {
        return switch (settings.source()) {
            case REAL_DATE -> settings.season().contains(MonthDay.from(date));
            case ALWAYS -> true;
            case NEVER -> false;
            case SERENE_SEASONS -> moment.map(settings.sereneSeasons()::isChristmas)
                    .orElseGet(() -> settings.season().contains(MonthDay.from(date)));
        };
    }

    public static boolean isWorkshopDelivery(SeasonSettings settings, LocalDate date) {
        return isWorkshopDelivery(settings, date, Optional.empty());
    }

    /** {@link SeasonSource#ALWAYS} keeps the delivery on its real days, so the event still feels like an event. */
    public static boolean isWorkshopDelivery(SeasonSettings settings, LocalDate date, Optional<SeasonMoment> moment) {
        if (settings.source() == SeasonSource.SERENE_SEASONS && moment.isPresent()) {
            return settings.sereneSeasons().isDelivery(moment.get());
        }
        return isActive(settings, date, moment) && settings.workshopDelivery().contains(MonthDay.from(date));
    }

    public static LocalDate today() {
        return LocalDate.now(clock);
    }

    /** Plugged in by a seasons mod's compat ({@code compat.sereneseasons}). */
    public static void setCalendar(Supplier<Optional<SeasonMoment>> newCalendar) {
        calendar = Objects.requireNonNull(newCalendar);
    }

    /** The calendar's moment now, if a seasons mod provides one. */
    public static Optional<SeasonMoment> calendarMoment() {
        return calendar.get();
    }

    /** GameTests and dev checks pin the date; production never calls this. */
    public static void setClock(Clock newClock) {
        clock = Objects.requireNonNull(newClock);
    }
}
