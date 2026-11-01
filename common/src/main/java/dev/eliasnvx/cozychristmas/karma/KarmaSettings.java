package dev.eliasnvx.cozychristmas.karma;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * The {@code karma} config section. Karma above {@code niceAbove} is nice, below {@code naughtyBelow} naughty. It drifts
 * {@code decayPerDay} toward 0 every game day. Hurting a mob in {@code #cozychristmas:karma_protected} costs
 * {@code hurtProtected} (at most once per victim every {@code hurtCooldownTicks}), killing one {@code killProtected}.
 */
public record KarmaSettings(int decayPerDay, int niceAbove, int naughtyBelow, int hurtProtected, int killProtected,
                            int hurtCooldownTicks) {

    public static final KarmaSettings DEFAULTS = new KarmaSettings(1, 30, -30, -2, -10, 40);

    private static final Codec<Integer> KARMA = Codec.intRange(Karma.MIN, Karma.MAX);
    private static final Codec<Integer> PENALTY = Codec.intRange(Karma.MIN, 0);

    public static final Codec<KarmaSettings> CODEC = RecordCodecBuilder.<KarmaSettings>create(i -> i.group(
            Codec.intRange(0, Karma.MAX).fieldOf("decay_per_day").orElse(DEFAULTS.decayPerDay).forGetter(KarmaSettings::decayPerDay),
            KARMA.fieldOf("nice_above").orElse(DEFAULTS.niceAbove).forGetter(KarmaSettings::niceAbove),
            KARMA.fieldOf("naughty_below").orElse(DEFAULTS.naughtyBelow).forGetter(KarmaSettings::naughtyBelow),
            PENALTY.fieldOf("hurt_protected").orElse(DEFAULTS.hurtProtected).forGetter(KarmaSettings::hurtProtected),
            PENALTY.fieldOf("kill_protected").orElse(DEFAULTS.killProtected).forGetter(KarmaSettings::killProtected),
            Codec.intRange(0, 72000).fieldOf("hurt_cooldown_ticks").orElse(DEFAULTS.hurtCooldownTicks).forGetter(KarmaSettings::hurtCooldownTicks)
    ).apply(i, KarmaSettings::new)).validate(KarmaSettings::validate);

    private static DataResult<KarmaSettings> validate(KarmaSettings settings) {
        return settings.naughtyBelow < settings.niceAbove
                ? DataResult.success(settings)
                : DataResult.error(() -> "naughty_below must be lower than nice_above");
    }

    public KarmaTier tier(int karma) {
        if (karma > niceAbove) {
            return KarmaTier.NICE;
        }
        return karma < naughtyBelow ? KarmaTier.NAUGHTY : KarmaTier.NEUTRAL;
    }
}
