package dev.eliasnvx.cozychristmas.goblin;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GoblinSettingsTest {

    @Test
    void emptySectionGivesDefaults() {
        var json = JsonParser.parseString("{}");
        assertEquals(GoblinSettings.DEFAULTS, GoblinSettings.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow());
    }

    @Test
    void theftCanBeTurnedOff() {
        var json = JsonParser.parseString("{\"present_goblin\": {\"steals\": false}}");
        assertFalse(GoblinSettings.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow().presentGoblin().steals());
    }

    @Test
    void rejectsACrossedGroupSize() {
        var json = JsonParser.parseString("{\"min_group\": 3, \"max_group\": 1}");
        assertTrue(PresentGoblinSettings.CODEC.parse(JsonOps.INSTANCE, json).isError());
    }

    @Test
    void gingerbreadIsSoggyByDefault() {
        assertTrue(GoblinSettings.DEFAULTS.gingerbreadMan().soggy());
    }
}
