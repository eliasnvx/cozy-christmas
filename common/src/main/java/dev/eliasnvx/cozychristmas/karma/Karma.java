package dev.eliasnvx.cozychristmas.karma;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;

/**
 * A player's hidden karma as stored: the {@code value} (-100..100) as of game day {@code day}. Drift toward 0 is
 * applied lazily when the karma is read or changed ({@link #settle}), so nothing ticks per player and days spent
 * offline count too. {@code giftDay} is the last day a gift of theirs earned karma (once a day, see
 * {@code KarmaManager#rewardGift}), {@code santaGiftDay} the last day Santa gave them a present (once a day, see
 * {@code KarmaManager#takeSantaGift}), {@code stockingDay} the last morning one of their stockings was filled (by the
 * day-night clock, see {@code KarmaManager#takeStockingGift}), {@code krampusDay} the last day Krampus came for them
 * (see {@code KrampusVisits}). Immutable: the attachment always holds a whole new record.
 */
public record Karma(int value, long day, long giftDay, long santaGiftDay, long stockingDay, long krampusDay) {

    public static final int MIN = -100;
    public static final int MAX = 100;
    /** {@link #giftDay} / {@link #santaGiftDay} / {@link #stockingDay} / {@link #krampusDay} of a player who never had one. */
    public static final long NO_GIFT = Long.MIN_VALUE;

    public static final Karma ZERO = new Karma(0, 0);

    public static final Codec<Karma> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.fieldOf("value").forGetter(Karma::value), // lenient: the constructor clamps
            Codec.LONG.fieldOf("day").forGetter(Karma::day),
            Codec.LONG.optionalFieldOf("gift_day", NO_GIFT).forGetter(Karma::giftDay),
            Codec.LONG.optionalFieldOf("santa_gift_day", NO_GIFT).forGetter(Karma::santaGiftDay),
            Codec.LONG.optionalFieldOf("stocking_day", NO_GIFT).forGetter(Karma::stockingDay),
            Codec.LONG.optionalFieldOf("krampus_day", NO_GIFT).forGetter(Karma::krampusDay)
    ).apply(i, Karma::new));

    public Karma {
        value = clamp(value);
    }

    public Karma(int value, long day) {
        this(value, day, NO_GIFT, NO_GIFT, NO_GIFT, NO_GIFT);
    }

    public Karma(int value, long day, long giftDay) {
        this(value, day, giftDay, NO_GIFT, NO_GIFT, NO_GIFT);
    }

    public Karma(int value, long day, long giftDay, long santaGiftDay, long stockingDay) {
        this(value, day, giftDay, santaGiftDay, stockingDay, NO_GIFT);
    }

    /** This karma on {@code today}, after {@code decayPerDay} of drift toward 0 for every day since {@link #day}. */
    public Karma settle(long today, int decayPerDay) {
        if (today == day) {
            return this;
        }
        if (today < day || value == 0) {
            return withValue(value, today); // a new world's clock, or nothing to drift
        }
        long days = Math.min(today - day, MAX - MIN); // enough to cross the whole range, and no overflow below
        int drift = (int) Math.min(Math.abs(value), days * decayPerDay);
        return withValue(value > 0 ? value - drift : value + drift, today);
    }

    /** Settles, then adds {@code delta}; the result stays in range. */
    public Karma plus(int delta, long today, int decayPerDay) {
        Karma settled = settle(today, decayPerDay);
        return withValue(clamp((long) settled.value + delta), today);
    }

    /** Settles, then adds {@code gift} unless a gift already earned karma today; marks today either way. */
    public Karma plusGift(int gift, long today, int decayPerDay) {
        if (giftDay == today) {
            return settle(today, decayPerDay);
        }
        Karma rewarded = plus(gift, today, decayPerDay);
        return new Karma(rewarded.value, today, today, santaGiftDay, stockingDay, krampusDay);
    }

    /** Settles and marks {@code today} as the day Santa gave a present. */
    public Karma withSantaGift(long today, int decayPerDay) {
        Karma settled = settle(today, decayPerDay);
        return new Karma(settled.value, today, giftDay, today, stockingDay, krampusDay);
    }

    /** Marks {@code morning} (a day of the day-night clock, not of {@link #day}) as the day a stocking was filled. */
    public Karma withStocking(long morning) {
        return new Karma(value, day, giftDay, santaGiftDay, morning, krampusDay);
    }

    /** Marks {@code today} (a karma game day) as the day Krampus came. */
    public Karma withKrampusVisit(long today) {
        return new Karma(value, day, giftDay, santaGiftDay, stockingDay, today);
    }

    /** The same days, another value as of {@code newDay}. */
    public Karma withValue(int newValue, long newDay) {
        return new Karma(newValue, newDay, giftDay, santaGiftDay, stockingDay, krampusDay);
    }

    public static int clamp(long value) {
        return (int) Mth.clamp(value, MIN, MAX);
    }
}
