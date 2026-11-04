package dev.eliasnvx.cozychristmas.present;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SnowPresentSettingsTest {

    @Test
    void rejectsACrossedRing() {
        var json = JsonParser.parseString("{\"min_distance\": 50, \"max_distance\": 20}");
        assertTrue(SnowPresentSettings.CODEC.parse(JsonOps.INSTANCE, json).isError());
    }

    @Test
    void emptySectionGivesDefaults() {
        var json = JsonParser.parseString("{}");
        assertEquals(SnowPresentSettings.DEFAULTS, SnowPresentSettings.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow());
    }

    @Test
    void defaultsArePresentsNowAndThen() {
        SnowPresentSettings snow = SnowPresentSettings.DEFAULTS;
        assertTrue(snow.enabled());
        // about one try in twelve a minute per player: a find every ten minutes or more, fewer where snow is patchy
        assertTrue(snow.chance() > 0.0 && snow.chance() <= 0.1);
        assertTrue(snow.minDistance() >= 16, "out of the player's immediate sight");
    }
}
