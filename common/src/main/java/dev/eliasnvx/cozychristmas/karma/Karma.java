package dev.eliasnvx.cozychristmas.karma;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;

/**
 * A player's hidden karma as stored: the {@code value} (-100..100) as of game day {@code day}. Drift toward 0 is
 * applied lazily when the karma is read or changed ({@link #settle}), so nothing ticks per player and days spent
 * offline count too. {@code giftDay} is the last day a gift of theirs earned karma (once a day, see
 * {@code KarmaManager#rewardGift}). Immutable: the attachment always holds a whole new record.
 */
public record Karma(int value, long day, long giftDay) {

    public static final int MIN = -100;
    public static final int MAX = 100;
    /** {@link #giftDay} of a player whose gifts never earned karma. */
    public static final long NO_GIFT = Long.MIN_VALUE;

    public static final Karma ZERO = new Karma(0, 0);

    public static final Codec<Karma> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.fieldOf("value").forGetter(Karma::value), // lenient: the constructor clamps
            Codec.LONG.fieldOf("day").forGetter(Karma::day),
            Codec.LONG.optionalFieldOf("gift_day", NO_GIFT).forGetter(Karma::giftDay)
    ).apply(i, Karma::new));

    public Karma {
        value = clamp(value);
    }

    public Karma(int value, long day) {
        this(value, day, NO_GIFT);
    }

    /** This karma on {@code today}, after {@code decayPerDay} of drift toward 0 for every day since {@link #day}. */
    public Karma settle(long today, int decayPerDay) {
        if (today == day) {
            return this;
        }
        if (today < day || value == 0) {
            return new Karma(value, today, giftDay); // a new world's clock, or nothing to drift
        }
        long days = Math.min(today - day, MAX - MIN); // enough to cross the whole range, and no overflow below
        int drift = (int) Math.min(Math.abs(value), days * decayPerDay);
        return new Karma(value > 0 ? value - drift : value + drift, today, giftDay);
    }

    /** Settles, then adds {@code delta}; the result stays in range. */
    public Karma plus(int delta, long today, int decayPerDay) {
        Karma settled = settle(today, decayPerDay);
        return new Karma(clamp((long) settled.value + delta), today, giftDay);
    }

    /** Settles, then adds {@code gift} unless a gift already earned karma today; marks today either way. */
    public Karma plusGift(int gift, long today, int decayPerDay) {
        if (giftDay == today) {
            return settle(today, decayPerDay);
        }
        Karma rewarded = plus(gift, today, decayPerDay);
        return new Karma(rewarded.value, today, today);
    }

    public static int clamp(long value) {
        return (int) Mth.clamp(value, MIN, MAX);
    }
}
