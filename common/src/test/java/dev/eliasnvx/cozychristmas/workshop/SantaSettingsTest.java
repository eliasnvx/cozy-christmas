package dev.eliasnvx.cozychristmas.workshop;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SantaSettingsTest {

    @Test
    void niceDiscountRoundsDown() {
        SantaSettings santa = SantaSettings.DEFAULTS; // a quarter off
        assertEquals(0, santa.discount(1), "a one-emerald price stays");
        assertEquals(0, santa.discount(3));
        assertEquals(1, santa.discount(4));
        assertEquals(1, santa.discount(7));
        assertEquals(4, santa.discount(16));
    }

    @Test
    void noDiscountWhenOff() {
        SantaSettings santa = new SantaSettings(80, 8, 0.3, 1200, 32, 0.0, true, 2);
        assertEquals(0, santa.discount(64));
    }

    @Test
    void emptySectionGivesDefaults() {
        var json = JsonParser.parseString("{}");
        assertEquals(WorkshopSettings.DEFAULTS, WorkshopSettings.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow());
    }

    @Test
    void defaultsGiveDailyGiftsAndAWideAlarm() {
        SantaSettings santa = SantaSettings.DEFAULTS;
        assertTrue(santa.dailyGifts());
        assertTrue(santa.alarmRadius() >= 24, "the alarm reaches across the workshop");
    }
}
