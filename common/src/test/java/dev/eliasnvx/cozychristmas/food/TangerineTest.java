package dev.eliasnvx.cozychristmas.food;

import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TangerineTest {

    private static final long DAY = 24000L;

    @Test
    void midnightIsAroundEighteenThousandOnAnyDay() {
        assertTrue(Tangerine.isMidnight(18000));
        assertTrue(Tangerine.isMidnight(17500));
        assertTrue(Tangerine.isMidnight(18500));
        assertTrue(Tangerine.isMidnight(18000 + 7 * DAY));
        assertFalse(Tangerine.isMidnight(17499));
        assertFalse(Tangerine.isMidnight(18501));
        assertFalse(Tangerine.isMidnight(6000));
        assertFalse(Tangerine.isMidnight(0));
    }

    @Test
    void theBellStrikesTwelveTimes() {
        long strokes = IntStream.rangeClosed(1, Tangerine.CHIMES_DURATION).filter(Tangerine::strikesAt).count();
        assertEquals(Tangerine.CHIMES, strokes);
        assertTrue(Tangerine.strikesAt(Tangerine.CHIMES_DURATION), "the first stroke comes at once");
        assertFalse(Tangerine.strikesAt(0));
    }
}
