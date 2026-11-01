package dev.eliasnvx.cozychristmas.karma;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class KarmaTest {

    @Test
    void constructorClamps() {
        assertEquals(Karma.MAX, new Karma(250, 0).value());
        assertEquals(Karma.MIN, new Karma(-250, 0).value());
    }

    @Test
    void driftsTowardZeroPerDay() {
        assertEquals(new Karma(37, 13), new Karma(40, 10).settle(13, 1));
        assertEquals(new Karma(-34, 13), new Karma(-40, 10).settle(13, 2));
    }

    @Test
    void driftStopsAtZero() {
        assertEquals(new Karma(0, 1000), new Karma(5, 10).settle(1000, 1));
        assertEquals(new Karma(0, 1000), new Karma(-5, 10).settle(1000, 1));
    }

    @Test
    void driftSurvivesHugeGaps() {
        assertEquals(new Karma(0, Long.MAX_VALUE / 2), new Karma(-100, 0).settle(Long.MAX_VALUE / 2, 100));
    }

    @Test
    void sameDayIsUnchanged() {
        Karma karma = new Karma(20, 7);
        assertSame(karma, karma.settle(7, 1));
    }

    @Test
    void clockGoingBackDoesNotDrift() {
        assertEquals(new Karma(20, 3), new Karma(20, 7).settle(3, 1));
    }

    @Test
    void noDecayKeepsTheValue() {
        assertEquals(new Karma(-60, 50), new Karma(-60, 0).settle(50, 0));
    }

    @Test
    void plusSettlesFirstThenAdds() {
        // 10 days of drift take 50 down to 40, then -45 lands at -5
        assertEquals(new Karma(-5, 10), new Karma(50, 0).plus(-45, 10, 1));
    }

    @Test
    void plusClampsWithoutOverflow() {
        assertEquals(Karma.MAX, new Karma(90, 0).plus(Integer.MAX_VALUE, 0, 1).value());
        assertEquals(Karma.MIN, new Karma(-90, 0).plus(Integer.MIN_VALUE, 0, 1).value());
    }
}
