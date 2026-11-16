package dev.eliasnvx.cozychristmas.karma;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KarmaSettingsTest {

    private final KarmaSettings settings = KarmaSettings.DEFAULTS;

    @Test
    void thresholdsAreExclusive() {
        assertEquals(KarmaTier.NICE, settings.tier(31));
        assertEquals(KarmaTier.NEUTRAL, settings.tier(30));
        assertEquals(KarmaTier.NEUTRAL, settings.tier(0));
        assertEquals(KarmaTier.NEUTRAL, settings.tier(-30));
        assertEquals(KarmaTier.NAUGHTY, settings.tier(-31));
    }

    @Test
    void rejectsCrossedThresholds() {
        var json = JsonParser.parseString("{\"nice_above\": -40, \"naughty_below\": 40}");
        assertTrue(KarmaSettings.CODEC.parse(JsonOps.INSTANCE, json).error().isPresent());
    }

    @Test
    void emptySectionGivesDefaults() {
        var json = JsonParser.parseString("{}");
        assertEquals(KarmaSettings.DEFAULTS, KarmaSettings.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(false, e -> { }));
    }
}
