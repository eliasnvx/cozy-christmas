package dev.eliasnvx.cozychristmas.config;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CommonConfigTest {

    @Test
    void defaultsRoundTrip() {
        var json = CommonConfig.CODEC.encodeStart(JsonOps.INSTANCE, CommonConfig.DEFAULTS).getOrThrow();
        assertEquals(CommonConfig.DEFAULTS, CommonConfig.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow());
    }

    @Test
    void missingSectionsTakeDefaults() {
        var json = JsonParser.parseString("{\"karma\": {\"decay_per_day\": 3}}");
        CommonConfig config = CommonConfig.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
        assertEquals(3, config.karma().decayPerDay());
        assertEquals(CommonConfig.DEFAULTS.karma().niceAbove(), config.karma().niceAbove());
        assertEquals(CommonConfig.DEFAULTS.season(), config.season());
        assertEquals(CommonConfig.DEFAULTS.presents(), config.presents());
    }
}
