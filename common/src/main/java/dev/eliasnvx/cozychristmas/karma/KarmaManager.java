package dev.eliasnvx.cozychristmas.karma;

import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.config.CozyConfig;
import dev.eliasnvx.cozychristmas.platform.PlatformHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * Server-side karma of a player. Every read settles the daily drift first, so callers always see today's value.
 * Clients never learn the number: they see its signs (coal, Santa's mood, ticking presents).
 */
public final class KarmaManager {

    private static final long TICKS_PER_DAY = 24000L;

    private KarmaManager() {
    }

    public static int get(ServerPlayer player) {
        return current(player).value();
    }

    public static KarmaTier tier(ServerPlayer player) {
        return CozyConfig.common().karma().tier(get(player));
    }

    /** Adds {@code delta} (negative for naughty deeds) and returns the new value. */
    public static int add(ServerPlayer player, int delta) {
        if (delta == 0) {
            return get(player);
        }
        Karma updated = PlatformHelper.getKarma(player).plus(delta, today(player), decayPerDay());
        PlatformHelper.setKarma(player, updated);
        CozyChristmas.LOGGER.debug("Karma of {} {} {} -> {}", player.getScoreboardName(), delta > 0 ? "+" : "-",
                Math.abs(delta), updated.value());
        return updated.value();
    }

    /**
     * Someone else opened a gift this player wrapped: {@code gift_given} karma, once per game day (so two friends
     * can't farm it by wrapping dirt for each other).
     * @return whether karma was given
     */
    public static boolean rewardGift(ServerPlayer giver) {
        Karma before = PlatformHelper.getKarma(giver);
        long today = today(giver);
        if (before.giftDay() == today) {
            return false;
        }
        PlatformHelper.setKarma(giver, before.plusGift(CozyConfig.common().karma().giftGiven(), today, decayPerDay()));
        return true;
    }

    /**
     * Santa gives a nice player one present a game day: marks today and returns true, or false if they already had
     * today's.
     */
    public static boolean takeSantaGift(ServerPlayer player) {
        Karma before = PlatformHelper.getKarma(player);
        long today = today(player);
        if (before.santaGiftDay() == today) {
            return false;
        }
        PlatformHelper.setKarma(player, before.withSantaGift(today, decayPerDay()));
        return true;
    }

    /**
     * One stocking of a player is filled a morning: marks {@code morning} (a day of the day-night clock) and returns
     * true, or false if one was already filled that morning. The clock, not game time: sleeping brings the morning.
     */
    public static boolean takeStockingGift(ServerPlayer player, long morning) {
        Karma before = PlatformHelper.getKarma(player);
        if (before.stockingDay() == morning) {
            return false;
        }
        PlatformHelper.setKarma(player, before.withStocking(morning));
        return true;
    }

    public static void set(ServerPlayer player, int value) {
        PlatformHelper.setKarma(player, PlatformHelper.getKarma(player).withValue(value, today(player)));
    }

    /** Game days from the overworld clock, as karma counts them. */
    public static long today(ServerLevel level) {
        return level.getServer().overworld().getGameTime() / TICKS_PER_DAY;
    }

    private static Karma current(ServerPlayer player) {
        Karma stored = PlatformHelper.getKarma(player);
        Karma settled = stored.settle(today(player), decayPerDay());
        if (settled != stored) {
            PlatformHelper.setKarma(player, settled);
        }
        return settled;
    }

    /** Game days from the overworld clock: sleeping skips the night but not the day count, so it can't wash karma. */
    private static long today(ServerPlayer player) {
        return today(player.level());
    }

    private static int decayPerDay() {
        return CozyConfig.common().karma().decayPerDay();
    }
}
