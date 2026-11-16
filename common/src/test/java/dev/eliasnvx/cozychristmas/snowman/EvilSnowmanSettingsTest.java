package dev.eliasnvx.cozychristmas.snowman;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EvilSnowmanSettingsTest {

    @Test
    void emptySectionGivesDefaults() {
        var json = JsonParser.parseString("{}");
        assertEquals(EvilSnowmanSettings.DEFAULTS, EvilSnowmanSettings.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(false, e -> { }));
    }

    @Test
    void iceSnowballTakesPartialSettings() {
        var json = JsonParser.parseString("{\"ice_snowball\": {\"chill_ticks\": 0}}");
        IceSnowballSettings ice = EvilSnowmanSettings.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(false, e -> { }).iceSnowball();
        assertEquals(0, ice.chillTicks());
        assertEquals(IceSnowballSettings.DEFAULTS.damage(), ice.damage());
    }

    @Test
    void rejectsACrossedGroupSize() {
        var json = JsonParser.parseString("{\"min_group\": 3, \"max_group\": 1}");
        assertTrue(EvilSnowmanSettings.CODEC.parse(JsonOps.INSTANCE, json).error().isPresent());
    }

    @Test
    void fireHurtsByTheMultiplier() {
        assertEquals(3.0F, EvilSnowmanSettings.DEFAULTS.fireDamage(1.0F));
    }

    @Test
    void throwsAtMostHalfAgainTheInterval() {
        assertEquals(45, EvilSnowmanSettings.DEFAULTS.maxThrowInterval());
    }
}
