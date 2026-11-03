package dev.eliasnvx.cozychristmas.karma;

import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.config.CozyConfig;
import dev.eliasnvx.cozychristmas.platform.PlatformHelper;
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

    public static void set(ServerPlayer player, int value) {
        PlatformHelper.setKarma(player, new Karma(value, today(player), PlatformHelper.getKarma(player).giftDay()));
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
        return player.level().getServer().overworld().getGameTime() / TICKS_PER_DAY;
    }

    private static int decayPerDay() {
        return CozyConfig.common().karma().decayPerDay();
    }
}
