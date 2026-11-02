package dev.eliasnvx.cozychristmas.config;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import dev.eliasnvx.cozychristmas.cosmetic.ToastySettings;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        assertEquals(CommonConfig.DEFAULTS.toastySet(), config.toastySet());
    }

    @Test
    void toastySpeedBonusOutOfRangeTakesTheDefault() {
        var tooFast = JsonParser.parseString("{\"toasty_set\": {\"snow_speed_bonus\": 5.0}}");
        assertEquals(ToastySettings.DEFAULTS.snowSpeedBonus(),
                CommonConfig.CODEC.parse(JsonOps.INSTANCE, tooFast).getOrThrow().toastySet().snowSpeedBonus());
        var off = JsonParser.parseString("{\"toasty_set\": {\"snow_speed_bonus\": 0}}");
        ToastySettings toasty = CommonConfig.CODEC.parse(JsonOps.INSTANCE, off).getOrThrow().toastySet();
        assertEquals(0.0, toasty.snowSpeedBonus());
        assertTrue(toasty.keepsWarm());
    }
}
