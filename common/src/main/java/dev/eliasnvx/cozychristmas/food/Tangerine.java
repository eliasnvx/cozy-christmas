package dev.eliasnvx.cozychristmas.food;

/**
 * The tangerine's secret (SPEC: "an easter egg, no explanation"): eaten around midnight, a big bell strikes twelve
 * for whoever ate it, as the New Year comes in where tangerines are the smell of the holiday. Pure rules here; the
 * bell is {@link ChimesEffect}, hidden.
 */
public final class Tangerine {

    public static final int CHIMES = 12;
    /** Ticks between two strokes of the bell. */
    public static final int CHIME_INTERVAL = 30;
    /** The chimes effect lasts this long: its first tick strikes the first stroke, its last full interval the twelfth. */
    public static final int CHIMES_DURATION = CHIMES * CHIME_INTERVAL;
    private static final long TICKS_PER_DAY = 24000L;
    /** Clock time of midnight: the day-night clock starts at sunrise (0) and has noon at 6000. */
    private static final long MIDNIGHT = 18000L;
    /** How far from midnight still counts, either way: about half an hour of game time. */
    private static final long MIDNIGHT_SLACK = 500L;

    private Tangerine() {
    }

    /** True for a day-night clock time within half an hour or so of midnight, on any day. */
    public static boolean isMidnight(long clockTime) {
        long time = Math.floorMod(clockTime, TICKS_PER_DAY);
        return Math.abs(time - MIDNIGHT) <= MIDNIGHT_SLACK;
    }

    /** Whether the bell strikes with {@code remaining} ticks of the chimes effect left (checked before it counts down). */
    public static boolean strikesAt(int remaining) {
        return remaining > 0 && remaining % CHIME_INTERVAL == 0;
    }
}
