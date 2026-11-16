package dev.eliasnvx.cozychristmas.tree;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TreeSettingsTest {

    @Test
    void emptySectionGivesDefaults() {
        assertEquals(TreeSettings.DEFAULTS, TreeSettings.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString("{}")).getOrThrow());
        assertTrue(TreeSettings.DEFAULTS.seasonOnly(), "SPEC: presents under the tree in December");
    }

    @Test
    void decoratedFromTheMinimumOn() {
        TreeSettings settings = TreeSettings.DEFAULTS;
        assertFalse(settings.isDecorated(settings.minOrnaments() - 1));
        assertTrue(settings.isDecorated(settings.minOrnaments()));
    }

    @Test
    void presentsPileUpToTheMaximum() {
        TreeSettings settings = TreeSettings.DEFAULTS;
        assertTrue(settings.hasRoomFor(0));
        assertTrue(settings.hasRoomFor(settings.maxPresents() - 1));
        assertFalse(settings.hasRoomFor(settings.maxPresents()));
        assertFalse(new TreeSettings(true, 6, 5, 24, 3, 0).hasRoomFor(0), "0 turns tree presents off");
    }

    @Test
    void rejectsPresentsFartherThanTheTreeReaches() {
        assertTrue(TreeSettings.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString("{\"reach\": 2, \"present_radius\": 4}")).isError());
    }

    @Test
    void readsEveryField() {
        TreeSettings parsed = TreeSettings.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(
                "{\"season_only\": false, \"min_ornaments\": 2, \"reach\": 7, \"height\": 30, \"present_radius\": 5, \"max_presents\": 9}"))
                .getOrThrow();
        assertEquals(new TreeSettings(false, 2, 7, 30, 5, 9), parsed);
    }
}
