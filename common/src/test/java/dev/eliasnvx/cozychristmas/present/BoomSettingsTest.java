package dev.eliasnvx.cozychristmas.present;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BoomSettingsTest {

    private static final BoomSettings BOOM = new BoomSettings(4.0F, 6.0F, 3.0, 1.0F, false);

    @Test
    void damageFallsFromMaxToMinAcrossTheRadius() {
        assertEquals(6.0F, BOOM.damageAt(0.0));
        assertEquals(5.0F, BOOM.damageAt(1.5), 1e-6);
        assertEquals(4.0F, BOOM.damageAt(3.0));
    }

    @Test
    void nothingBeyondTheRadius() {
        assertEquals(0.0F, BOOM.damageAt(3.01));
    }

    @Test
    void defaultsMatchTheSpec() {
        assertEquals(4.0F, BoomSettings.DEFAULTS.minDamage());
        assertEquals(6.0F, BoomSettings.DEFAULTS.maxDamage());
        assertEquals(false, BoomSettings.DEFAULTS.breaksBlocks());
    }

    @Test
    void rejectsMinAboveMax() {
        var json = JsonParser.parseString("{\"min_damage\": 8, \"max_damage\": 2}");
        assertTrue(BoomSettings.CODEC.parse(JsonOps.INSTANCE, json).isError());
    }
}
