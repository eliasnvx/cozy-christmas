package dev.eliasnvx.cozychristmas.present;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import dev.eliasnvx.cozychristmas.karma.KarmaTier;
import net.minecraft.util.RandomSource;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PresentOddsTest {

    @Test
    void ticketsMapInOrder() {
        PresentOdds odds = new PresentOdds(80, 10, 10);
        assertEquals(PresentOutcome.LOOT, odds.pick(0));
        assertEquals(PresentOutcome.LOOT, odds.pick(79));
        assertEquals(PresentOutcome.COAL, odds.pick(80));
        assertEquals(PresentOutcome.COAL, odds.pick(89));
        assertEquals(PresentOutcome.BOOM, odds.pick(90));
        assertEquals(PresentOutcome.BOOM, odds.pick(99));
        assertThrows(IllegalArgumentException.class, () -> odds.pick(100));
    }

    @Test
    void zeroWeightNeverRolls() {
        PresentOdds onlyCoal = new PresentOdds(0, 1, 0);
        RandomSource random = RandomSource.create(1);
        for (int i = 0; i < 1000; i++) {
            assertEquals(PresentOutcome.COAL, onlyCoal.roll(random));
        }
    }

    @Test
    void rollsFollowTheWeights() {
        PresentOdds naughty = PresentSettings.DEFAULTS.odds(KarmaTier.NAUGHTY);
        RandomSource random = RandomSource.create(42);
        Map<PresentOutcome, Integer> counts = new EnumMap<>(PresentOutcome.class);
        int rolls = 100_000;
        for (int i = 0; i < rolls; i++) {
            counts.merge(naughty.roll(random), 1, Integer::sum);
        }
        assertNear(0.30, counts.get(PresentOutcome.LOOT), rolls);
        assertNear(0.40, counts.get(PresentOutcome.COAL), rolls);
        assertNear(0.30, counts.get(PresentOutcome.BOOM), rolls);
    }

    @Test
    void defaultsMatchTheSpec() {
        PresentSettings defaults = PresentSettings.DEFAULTS;
        assertEquals(new PresentOdds(80, 10, 10), defaults.odds(KarmaTier.NICE));
        assertEquals(new PresentOdds(60, 20, 20), defaults.odds(KarmaTier.NEUTRAL));
        assertEquals(new PresentOdds(30, 40, 30), defaults.odds(KarmaTier.NAUGHTY));
        for (KarmaTier tier : KarmaTier.values()) {
            assertEquals(100, defaults.odds(tier).total(), "SPEC gives percentages for " + tier);
        }
    }

    @Test
    void allZeroIsAnError() {
        var json = JsonParser.parseString("{\"loot\": 0, \"coal\": 0, \"boom\": 0}");
        assertTrue(PresentOdds.CODEC.parse(JsonOps.INSTANCE, json).isError());
    }

    private static void assertNear(double expected, int count, int total) {
        double actual = (double) count / total;
        assertTrue(Math.abs(actual - expected) < 0.01, "expected ~" + expected + ", got " + actual);
    }
}
