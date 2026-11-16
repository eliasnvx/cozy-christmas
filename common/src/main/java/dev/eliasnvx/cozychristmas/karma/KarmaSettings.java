package dev.eliasnvx.cozychristmas.karma;

import com.mojang.serialization.Codec;
import dev.eliasnvx.cozychristmas.config.ConfigCodecs;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * The {@code karma} config section. Karma above {@code niceAbove} is nice, below {@code naughtyBelow} naughty. It drifts
 * {@code decayPerDay} toward 0 every game day. Hurting a mob in {@code #cozychristmas:karma_protected} costs
 * {@code hurtProtected} (at most once per victim every {@code hurtCooldownTicks}), killing one {@code killProtected}.
 * A wrapped gift opened by another player earns its giver {@code giftGiven}, once per game day. Opening or breaking a
 * present from the workshop warehouse costs {@code stealPresent}; feeding a reindeer earns {@code feedReindeer}, once per
 * reindeer per game day. Killing a present goblin that carries stolen loot earns {@code returnStolen}.
 */
public record KarmaSettings(int decayPerDay, int niceAbove, int naughtyBelow, int hurtProtected, int killProtected,
                            int hurtCooldownTicks, int giftGiven, int stealPresent, int feedReindeer,
                            int returnStolen) {

    public static final KarmaSettings DEFAULTS = new KarmaSettings(1, 30, -30, -2, -10, 40, 3, -15, 1, 3);

    private static final Codec<Integer> KARMA = Codec.intRange(Karma.MIN, Karma.MAX);
    private static final Codec<Integer> PENALTY = Codec.intRange(Karma.MIN, 0);

    public static final Codec<KarmaSettings> CODEC = RecordCodecBuilder.<KarmaSettings>create(i -> i.group(
            ConfigCodecs.field(Codec.intRange(0, Karma.MAX), "decay_per_day", DEFAULTS.decayPerDay)
                    .forGetter(KarmaSettings::decayPerDay),
            ConfigCodecs.field(KARMA, "nice_above", DEFAULTS.niceAbove).forGetter(KarmaSettings::niceAbove),
            ConfigCodecs.field(KARMA, "naughty_below", DEFAULTS.naughtyBelow).forGetter(KarmaSettings::naughtyBelow),
            ConfigCodecs.field(PENALTY, "hurt_protected", DEFAULTS.hurtProtected)
                    .forGetter(KarmaSettings::hurtProtected),
            ConfigCodecs.field(PENALTY, "kill_protected", DEFAULTS.killProtected)
                    .forGetter(KarmaSettings::killProtected),
            ConfigCodecs.field(Codec.intRange(0, 72000), "hurt_cooldown_ticks", DEFAULTS.hurtCooldownTicks)
                    .forGetter(KarmaSettings::hurtCooldownTicks),
            ConfigCodecs.field(Codec.intRange(0, Karma.MAX), "gift_given", DEFAULTS.giftGiven)
                    .forGetter(KarmaSettings::giftGiven),
            ConfigCodecs.field(PENALTY, "steal_present", DEFAULTS.stealPresent).forGetter(KarmaSettings::stealPresent),
            ConfigCodecs.field(Codec.intRange(0, Karma.MAX), "feed_reindeer", DEFAULTS.feedReindeer)
                    .forGetter(KarmaSettings::feedReindeer),
            ConfigCodecs.field(Codec.intRange(0, Karma.MAX), "return_stolen", DEFAULTS.returnStolen)
                    .forGetter(KarmaSettings::returnStolen)
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
