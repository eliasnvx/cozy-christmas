package dev.eliasnvx.cozychristmas.stocking;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import dev.eliasnvx.cozychristmas.karma.KarmaTier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StockingTest {

    @Test
    void karmaDecidesWhatsInside() {
        assertEquals(StockingGift.GIFT, StockingGift.forTier(KarmaTier.NICE));
        assertEquals(StockingGift.SURPRISE, StockingGift.forTier(KarmaTier.NEUTRAL));
        assertEquals(StockingGift.COAL, StockingGift.forTier(KarmaTier.NAUGHTY));
    }

    @Test
    void coalShowsAsCoalEverythingElseAsAPresent() {
        assertEquals(StockingFill.COAL, StockingGift.COAL.fill());
        assertEquals(StockingFill.PRESENT, StockingGift.GIFT.fill());
        assertEquals(StockingFill.PRESENT, StockingGift.SURPRISE.fill());
    }

    @Test
    void emptySectionGivesDefaults() {
        assertEquals(StockingSettings.DEFAULTS, StockingSettings.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString("{}")).getOrThrow(false, e -> { }));
        assertTrue(StockingSettings.DEFAULTS.seasonOnly());
    }

    @Test
    void rejectsCrossedCoalCounts() {
        assertTrue(StockingSettings.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString("{\"coal_min\": 4, \"coal_max\": 2}")).error().isPresent());
    }
}
