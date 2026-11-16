package dev.eliasnvx.cozychristmas.krampus;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import dev.eliasnvx.cozychristmas.karma.Karma;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KrampusSettingsTest {

    private static final KrampusVisitSettings VISITS = KrampusVisitSettings.DEFAULTS;
    private static final long TODAY = 40;

    @Test
    void emptySectionGivesDefaults() {
        var json = JsonParser.parseString("{}");
        assertEquals(KrampusSettings.DEFAULTS, KrampusSettings.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow());
    }

    @Test
    void visitsCanBeTurnedOff() {
        var json = JsonParser.parseString("{\"visits\": {\"enabled\": false}}");
        KrampusVisitSettings off = KrampusSettings.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow().visits();
        assertFalse(off.mayVisit(-100, true, true, TODAY, Karma.NO_GIFT));
    }

    @Test
    void rejectsALashThatCantReach() {
        var json = JsonParser.parseString("{\"lash_min_range\": 9, \"lash_range\": 5}");
        assertTrue(KrampusSettings.CODEC.parse(JsonOps.INSTANCE, json).isError());
    }

    @Test
    void callsGoblinsAtTwoThirdsAndOneThird() {
        assertEquals(0, KrampusSettings.summonsDue(1.0F));
        assertEquals(0, KrampusSettings.summonsDue(0.7F));
        assertEquals(1, KrampusSettings.summonsDue(0.6F));
        assertEquals(2, KrampusSettings.summonsDue(0.3F));
        assertEquals(2, KrampusSettings.summonsDue(0.0F));
    }

    @Test
    void beatingHimClearsOnlyBadKarma() {
        assertEquals(0, KrampusSettings.karmaAfterDefeat(-90));
        assertEquals(0, KrampusSettings.karmaAfterDefeat(0));
        assertEquals(40, KrampusSettings.karmaAfterDefeat(40));
    }

    @Test
    void visitsOnlyTheVeryNaughtyAtNightInSeason() {
        assertTrue(VISITS.mayVisit(-80, true, true, TODAY, Karma.NO_GIFT));
        assertFalse(VISITS.mayVisit(-50, true, true, TODAY, Karma.NO_GIFT), "naughty is not very naughty");
        assertFalse(VISITS.mayVisit(-80, false, true, TODAY, Karma.NO_GIFT), "not by day");
        assertFalse(VISITS.mayVisit(-80, true, false, TODAY, Karma.NO_GIFT), "not out of season by default");
    }

    @Test
    void waitsBetweenVisits() {
        assertFalse(VISITS.mayVisit(-80, true, true, TODAY, TODAY - VISITS.cooldownDays() + 1));
        assertTrue(VISITS.mayVisit(-80, true, true, TODAY, TODAY - VISITS.cooldownDays()));
    }

    @Test
    void karmaRemembersTheVisit() {
        Karma visited = new Karma(-80, 5).withKrampusVisit(5);
        assertEquals(5, visited.krampusDay());
        assertEquals(5, visited.withValue(0, 6).krampusDay(), "beating him keeps the day");
        var json = Karma.CODEC.encodeStart(JsonOps.INSTANCE, visited).getOrThrow();
        assertEquals(visited, Karma.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow());
    }
}
