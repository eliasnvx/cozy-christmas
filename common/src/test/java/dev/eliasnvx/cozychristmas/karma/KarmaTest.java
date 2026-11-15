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

    @Test
    void giftsEarnKarmaOncePerDay() {
        Karma first = new Karma(10, 5).plusGift(3, 5, 1);
        assertEquals(new Karma(13, 5, 5), first);
        assertEquals(first, first.plusGift(3, 5, 1));
        assertEquals(new Karma(15, 6, 6), first.plusGift(3, 6, 1)); // drifted to 12, then +3
    }

    @Test
    void driftKeepsTheGiftDay() {
        assertEquals(new Karma(9, 8, 5), new Karma(10, 7, 5).settle(8, 1));
        assertEquals(new Karma(-1, 8, 5), new Karma(10, 7, 5).plus(-10, 8, 1));
    }

    @Test
    void santaGiftDayIsKeptThroughEveryChange() {
        Karma gifted = new Karma(40, 7, 5).withSantaGift(8, 1);
        assertEquals(new Karma(39, 8, 5, 8, Karma.NO_GIFT), gifted);
        assertEquals(8, gifted.settle(20, 1).santaGiftDay());
        assertEquals(8, gifted.plus(-10, 9, 1).santaGiftDay());
        assertEquals(8, gifted.plusGift(3, 9, 1).santaGiftDay());
        assertEquals(new Karma(5, 9, 5, 8, Karma.NO_GIFT), gifted.withValue(5, 9));
    }

    @Test
    void stockingDayIsKeptThroughEveryChange() {
        Karma filled = new Karma(40, 7, 5).withStocking(30);
        assertEquals(new Karma(40, 7, 5, Karma.NO_GIFT, 30), filled);
        assertEquals(30, filled.settle(20, 1).stockingDay());
        assertEquals(30, filled.plus(-10, 9, 1).stockingDay());
        assertEquals(30, filled.plusGift(3, 9, 1).stockingDay());
        assertEquals(30, filled.withSantaGift(9, 1).stockingDay());
    }
}
