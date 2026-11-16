package dev.eliasnvx.cozychristmas.krampus;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * The {@code krampus.visits} config section (SPEC: "comes to a player with very bad karma himself"). With
 * {@code enabled}, every {@code checkTicks} at night each survival player in the Overworld whose karma is at or below
 * {@code karmaAtMost} has a {@code chance} of a visit, at most once every {@code cooldownDays} game days, and only in
 * the Christmas season when {@code seasonOnly}. He turns up {@code minDistance}..{@code maxDistance} blocks away and
 * leaves at dawn if nobody beat him.
 */
public record KrampusVisitSettings(boolean enabled, boolean seasonOnly, int karmaAtMost, int checkTicks, double chance,
                                   int cooldownDays, int minDistance, int maxDistance) {

    public static final KrampusVisitSettings DEFAULTS = new KrampusVisitSettings(true, true, -70, 600, 0.08, 3, 16, 24);

    public static final Codec<KrampusVisitSettings> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.BOOL.fieldOf("enabled").orElse(DEFAULTS.enabled).forGetter(KrampusVisitSettings::enabled),
            Codec.BOOL.fieldOf("season_only").orElse(DEFAULTS.seasonOnly).forGetter(KrampusVisitSettings::seasonOnly),
            Codec.intRange(-100, 0).fieldOf("karma_at_most").orElse(DEFAULTS.karmaAtMost).forGetter(KrampusVisitSettings::karmaAtMost),
            Codec.intRange(20, 72000).fieldOf("check_ticks").orElse(DEFAULTS.checkTicks).forGetter(KrampusVisitSettings::checkTicks),
            Codec.doubleRange(0.0, 1.0).fieldOf("chance").orElse(DEFAULTS.chance).forGetter(KrampusVisitSettings::chance),
            Codec.intRange(0, 365).fieldOf("cooldown_days").orElse(DEFAULTS.cooldownDays).forGetter(KrampusVisitSettings::cooldownDays),
            Codec.intRange(4, 64).fieldOf("min_distance").orElse(DEFAULTS.minDistance).forGetter(KrampusVisitSettings::minDistance),
            Codec.intRange(4, 64).fieldOf("max_distance").orElse(DEFAULTS.maxDistance).forGetter(KrampusVisitSettings::maxDistance)
    ).apply(i, KrampusVisitSettings::new));

    /**
     * Whether a player with {@code karma} may get a visit now: visits on, the season if it matters, night, karma low
     * enough and the last visit ({@code lastVisitDay}, a karma game day) at least {@code cooldownDays} ago.
     */
    public boolean mayVisit(int karma, boolean night, boolean inSeason, long today, long lastVisitDay) {
        return enabled && night && (inSeason || !seasonOnly) && karma <= karmaAtMost
                && (lastVisitDay == Long.MIN_VALUE || today - lastVisitDay >= cooldownDays);
    }
}
