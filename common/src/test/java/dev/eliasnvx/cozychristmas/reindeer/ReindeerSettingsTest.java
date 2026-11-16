package dev.eliasnvx.cozychristmas.reindeer;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReindeerSettingsTest {

    @Test
    void flightModes() {
        assertTrue(FlightMode.SEASON.allows(true));
        assertFalse(FlightMode.SEASON.allows(false));
        assertTrue(FlightMode.ALWAYS.allows(false));
        assertFalse(FlightMode.NEVER.allows(true));
    }

    @Test
    void flightIsSeasonalByDefault() {
        assertEquals(FlightMode.SEASON, ReindeerSettings.DEFAULTS.flight());
    }

    @Test
    void readsTheFlightMode() {
        var json = JsonParser.parseString("{\"flight\": \"always\"}");
        assertEquals(FlightMode.ALWAYS, ReindeerSettings.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(false, e -> { }).flight());
    }

    @Test
    void rejectsACrossedHerdSize() {
        var json = JsonParser.parseString("{\"min_group\": 5, \"max_group\": 2}");
        assertTrue(ReindeerSettings.CODEC.parse(JsonOps.INSTANCE, json).error().isPresent());
    }

    @Test
    void emptySectionGivesDefaults() {
        var json = JsonParser.parseString("{}");
        assertEquals(ReindeerSettings.DEFAULTS, ReindeerSettings.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(false, e -> { }));
    }
}
