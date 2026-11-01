package dev.eliasnvx.cozychristmas.karma;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;

/**
 * A player's hidden karma as stored: the {@code value} (-100..100) as of game day {@code day}. Drift toward 0 is
 * applied lazily when the karma is read or changed ({@link #settle}), so nothing ticks per player and days spent
 * offline count too. Immutable: the attachment always holds a whole new record.
 */
public record Karma(int value, long day) {

    public static final int MIN = -100;
    public static final int MAX = 100;

    public static final Karma ZERO = new Karma(0, 0);

    public static final Codec<Karma> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.fieldOf("value").forGetter(Karma::value), // lenient: the constructor clamps
            Codec.LONG.fieldOf("day").forGetter(Karma::day)
    ).apply(i, Karma::new));

    public Karma {
        value = clamp(value);
    }

    /** This karma on {@code today}, after {@code decayPerDay} of drift toward 0 for every day since {@link #day}. */
    public Karma settle(long today, int decayPerDay) {
        if (today == day) {
            return this;
        }
        if (today < day || value == 0) {
            return new Karma(value, today); // a new world's clock, or nothing to drift
        }
        long days = Math.min(today - day, MAX - MIN); // enough to cross the whole range, and no overflow below
        int drift = (int) Math.min(Math.abs(value), days * decayPerDay);
        return new Karma(value > 0 ? value - drift : value + drift, today);
    }

    /** Settles, then adds {@code delta}; the result stays in range. */
    public Karma plus(int delta, long today, int decayPerDay) {
        Karma settled = settle(today, decayPerDay);
        return new Karma(clamp((long) settled.value + delta), today);
    }

    public static int clamp(long value) {
        return (int) Mth.clamp(value, MIN, MAX);
    }
}
