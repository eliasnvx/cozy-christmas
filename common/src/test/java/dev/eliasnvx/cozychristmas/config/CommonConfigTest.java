package dev.eliasnvx.cozychristmas.config;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import dev.eliasnvx.cozychristmas.cosmetic.ToastySettings;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.io.TempDir;

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
    void anOutOfRangeValueIsAnErrorNotSilentlyTheDefault() {
        var tooFast = JsonParser.parseString("{\"toasty_set\": {\"snow_speed_bonus\": 5.0}}");
        assertTrue(CommonConfig.CODEC.parse(JsonOps.INSTANCE, tooFast).isError());
        var off = JsonParser.parseString("{\"toasty_set\": {\"snow_speed_bonus\": 0}}");
        ToastySettings toasty = CommonConfig.CODEC.parse(JsonOps.INSTANCE, off).getOrThrow().toastySet();
        assertEquals(0.0, toasty.snowSpeedBonus());
        assertTrue(toasty.keepsWarm());
    }

    /** A section that fails its own check mustn't quietly come back as defaults (here: visits switched back on). */
    @Test
    void aSectionFailingItsCheckIsAnError() {
        var crossed = JsonParser.parseString(
                "{\"krampus\": {\"lash_min_range\": 8, \"lash_range\": 6, \"visits\": {\"enabled\": false}}}");
        assertTrue(CommonConfig.CODEC.parse(JsonOps.INSTANCE, crossed).isError());
    }

    /** A broken file is kept as .broken next to the fresh defaults, and the defaults are used. */
    @Test
    void aBrokenFileIsKept(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("cozychristmas-common.json");
        String broken = "{\"karma\": {\"naughty_below\": 30, \"nice_above\": 30}}";
        Files.writeString(file, broken);
        ConfigFile<CommonConfig> config = new ConfigFile<>(file, CommonConfig.CODEC, CommonConfig.DEFAULTS);
        assertEquals(CommonConfig.DEFAULTS, config.load());
        assertEquals(broken, Files.readString(dir.resolve("cozychristmas-common.json.broken")));
    }
}
