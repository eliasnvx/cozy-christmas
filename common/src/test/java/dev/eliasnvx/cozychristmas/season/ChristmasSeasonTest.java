package dev.eliasnvx.cozychristmas.season;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChristmasSeasonTest {

    private static final SeasonSettings DEFAULTS = SeasonSettings.DEFAULTS;

    @Test
    void defaultSeasonWrapsOverTheNewYear() {
        assertFalse(active(2026, 11, 30));
        assertTrue(active(2026, 12, 1));
        assertTrue(active(2026, 12, 31));
        assertTrue(active(2027, 1, 1));
        assertTrue(active(2027, 1, 7));
        assertFalse(active(2027, 1, 8));
        assertFalse(active(2027, 7, 15));
    }

    @Test
    void windowInsideOneYear() {
        DateWindow window = DateWindow.of(12, 24, 12, 25);
        assertFalse(window.contains(MonthDay.of(12, 23)));
        assertTrue(window.contains(MonthDay.of(12, 24)));
        assertTrue(window.contains(MonthDay.of(12, 25)));
        assertFalse(window.contains(MonthDay.of(12, 26)));
    }

    @Test
    void deliveryOnlyOnItsDays() {
        assertFalse(ChristmasSeason.isWorkshopDelivery(DEFAULTS, LocalDate.of(2026, 12, 23)));
        assertTrue(ChristmasSeason.isWorkshopDelivery(DEFAULTS, LocalDate.of(2026, 12, 24)));
        assertTrue(ChristmasSeason.isWorkshopDelivery(DEFAULTS, LocalDate.of(2026, 12, 25)));
    }

    @Test
    void alwaysAndNever() {
        SeasonSettings always = new SeasonSettings(SeasonSource.ALWAYS, DEFAULTS.season(), DEFAULTS.workshopDelivery(), DEFAULTS.sereneSeasons());
        SeasonSettings never = new SeasonSettings(SeasonSource.NEVER, DEFAULTS.season(), DEFAULTS.workshopDelivery(), DEFAULTS.sereneSeasons());
        LocalDate summer = LocalDate.of(2027, 7, 15);
        LocalDate christmas = LocalDate.of(2026, 12, 24);
        assertTrue(ChristmasSeason.isActive(always, summer));
        assertFalse(ChristmasSeason.isWorkshopDelivery(always, summer));
        assertTrue(ChristmasSeason.isWorkshopDelivery(always, christmas));
        assertFalse(ChristmasSeason.isActive(never, christmas));
        assertFalse(ChristmasSeason.isWorkshopDelivery(never, christmas));
    }

    @Test
    void leapDayParses() {
        var json = JsonParser.parseString("{\"start\": \"02-29\", \"end\": \"03-01\"}");
        assertEquals(DateWindow.of(2, 29, 3, 1), DateWindow.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(false, e -> { }));
    }

    @Test
    void configRoundTrip() {
        var json = SeasonSettings.CODEC.encodeStart(JsonOps.INSTANCE, DEFAULTS).getOrThrow(false, e -> { });
        assertEquals("12-01", json.getAsJsonObject().getAsJsonObject("season").get("start").getAsString());
        assertEquals(DEFAULTS, SeasonSettings.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(false, e -> { }));
    }

    @Test
    void badDateIsAnError() {
        var json = JsonParser.parseString("{\"start\": \"13-01\", \"end\": \"01-07\"}");
        assertTrue(DateWindow.CODEC.parse(JsonOps.INSTANCE, json).error().isPresent());
    }

    private static boolean active(int year, int month, int day) {
        return ChristmasSeason.isActive(DEFAULTS, LocalDate.of(year, month, day));
    }

    @Test
    void sereneSeasonsFollowsItsCalendar() {
        SeasonSettings serene = new SeasonSettings(SeasonSource.SERENE_SEASONS, DEFAULTS.season(), DEFAULTS.workshopDelivery(),
                DEFAULTS.sereneSeasons());
        LocalDate july = LocalDate.of(2026, 7, 1);
        LocalDate christmasEve = LocalDate.of(2026, 12, 24);
        assertTrue(ChristmasSeason.isActive(serene, july, Optional.of(new SeasonMoment(SubSeason.EARLY_WINTER, 3))),
                "early winter is Christmas, whatever the date");
        assertTrue(ChristmasSeason.isActive(serene, july, Optional.of(new SeasonMoment(SubSeason.MID_WINTER, 7))));
        assertFalse(ChristmasSeason.isActive(serene, christmasEve, Optional.of(new SeasonMoment(SubSeason.LATE_WINTER, 0))),
                "late winter isn't, whatever the date");
        assertFalse(ChristmasSeason.isActive(serene, christmasEve, Optional.of(new SeasonMoment(SubSeason.MID_SUMMER, 0))));
    }

    @Test
    void sereneSeasonsDeliveryIsTheStartOfMidWinter() {
        SeasonSettings serene = new SeasonSettings(SeasonSource.SERENE_SEASONS, DEFAULTS.season(), DEFAULTS.workshopDelivery(),
                DEFAULTS.sereneSeasons());
        LocalDate july = LocalDate.of(2026, 7, 1);
        assertTrue(ChristmasSeason.isWorkshopDelivery(serene, july, Optional.of(new SeasonMoment(SubSeason.MID_WINTER, 0))));
        assertTrue(ChristmasSeason.isWorkshopDelivery(serene, july, Optional.of(new SeasonMoment(SubSeason.MID_WINTER, 1))));
        assertFalse(ChristmasSeason.isWorkshopDelivery(serene, july, Optional.of(new SeasonMoment(SubSeason.MID_WINTER, 2))));
        assertFalse(ChristmasSeason.isWorkshopDelivery(serene, july, Optional.of(new SeasonMoment(SubSeason.EARLY_WINTER, 0))));
    }

    @Test
    void sereneSeasonsWithoutTheModFollowsTheDate() {
        SeasonSettings serene = new SeasonSettings(SeasonSource.SERENE_SEASONS, DEFAULTS.season(), DEFAULTS.workshopDelivery(),
                DEFAULTS.sereneSeasons());
        assertTrue(ChristmasSeason.isActive(serene, LocalDate.of(2026, 12, 10), Optional.empty()));
        assertFalse(ChristmasSeason.isActive(serene, LocalDate.of(2026, 7, 1), Optional.empty()));
        assertTrue(ChristmasSeason.isWorkshopDelivery(serene, LocalDate.of(2026, 12, 24), Optional.empty()));
    }

    @Test
    void sereneSeasonsSectionReadsSubSeasonNames() {
        SereneSeasonsSettings parsed = SereneSeasonsSettings.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(
                "{\"sub_seasons\": [\"late_autumn\", \"early_winter\"], \"delivery_sub_season\": \"early_winter\", \"delivery_days\": 3}"))
                .getOrThrow(false, e -> { });
        assertEquals(new SereneSeasonsSettings(List.of(SubSeason.LATE_AUTUMN, SubSeason.EARLY_WINTER), SubSeason.EARLY_WINTER, 3), parsed);
        assertEquals(SereneSeasonsSettings.DEFAULTS, SereneSeasonsSettings.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString("{}")).getOrThrow(false, e -> { }));
    }
}
