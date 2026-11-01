package dev.eliasnvx.cozychristmas.season;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.MonthDay;

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
        SeasonSettings always = new SeasonSettings(SeasonSource.ALWAYS, DEFAULTS.season(), DEFAULTS.workshopDelivery());
        SeasonSettings never = new SeasonSettings(SeasonSource.NEVER, DEFAULTS.season(), DEFAULTS.workshopDelivery());
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
        assertEquals(DateWindow.of(2, 29, 3, 1), DateWindow.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow());
    }

    @Test
    void configRoundTrip() {
        var json = SeasonSettings.CODEC.encodeStart(JsonOps.INSTANCE, DEFAULTS).getOrThrow();
        assertEquals("12-01", json.getAsJsonObject().getAsJsonObject("season").get("start").getAsString());
        assertEquals(DEFAULTS, SeasonSettings.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow());
    }

    @Test
    void badDateIsAnError() {
        var json = JsonParser.parseString("{\"start\": \"13-01\", \"end\": \"01-07\"}");
        assertTrue(DateWindow.CODEC.parse(JsonOps.INSTANCE, json).isError());
    }

    private static boolean active(int year, int month, int day) {
        return ChristmasSeason.isActive(DEFAULTS, LocalDate.of(year, month, day));
    }
}
