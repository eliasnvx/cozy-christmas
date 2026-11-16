package dev.eliasnvx.cozychristmas.krampus;

import com.mojang.serialization.Codec;
import dev.eliasnvx.cozychristmas.config.ConfigCodecs;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * The {@code krampus} config section. The boss has {@code health}, {@code armor}, hits with his birch switch for
 * {@code attackDamage} and walks at {@code speed}. His chain lashes a target {@code lashMinRange}..{@code lashRange}
 * blocks away for {@code lashDamage}, pulling it in, every {@code lashCooldown} ticks; up close he puts a player in his
 * sack for {@code sackTicks} (SPEC: 2 s) every {@code sackCooldown} ticks. Below two thirds and one third of his health
 * he calls {@code summonGoblins} present goblins. Killing him clears the bad karma of everyone who fought him when
 * {@code clearsKarma} is on. {@link KrampusVisitSettings} decides when he comes to a very naughty player himself.
 */
public record KrampusSettings(double health, double armor, double attackDamage, double speed, double lashDamage,
                              double lashMinRange, double lashRange, int lashCooldown, int sackTicks, int sackCooldown,
                              int summonGoblins, boolean clearsKarma, KrampusVisitSettings visits) {

    public static final KrampusSettings DEFAULTS = new KrampusSettings(200.0, 6.0, 9.0, 0.3, 6.0, 3.0, 8.0, 70, 40, 240,
            2, true, KrampusVisitSettings.DEFAULTS);

    public static final Codec<KrampusSettings> CODEC = RecordCodecBuilder.<KrampusSettings>create(i -> i.group(
            ConfigCodecs.field(Codec.doubleRange(1.0, 2048.0), "health", DEFAULTS.health)
                    .forGetter(KrampusSettings::health),
            ConfigCodecs.field(Codec.doubleRange(0.0, 30.0), "armor", DEFAULTS.armor).forGetter(KrampusSettings::armor),
            ConfigCodecs.field(Codec.doubleRange(0.0, 100.0), "attack_damage", DEFAULTS.attackDamage)
                    .forGetter(KrampusSettings::attackDamage),
            ConfigCodecs.field(Codec.doubleRange(0.0, 1.0), "speed", DEFAULTS.speed).forGetter(KrampusSettings::speed),
            ConfigCodecs.field(Codec.doubleRange(0.0, 100.0), "lash_damage", DEFAULTS.lashDamage)
                    .forGetter(KrampusSettings::lashDamage),
            ConfigCodecs.field(Codec.doubleRange(0.0, 16.0), "lash_min_range", DEFAULTS.lashMinRange)
                    .forGetter(KrampusSettings::lashMinRange),
            ConfigCodecs.field(Codec.doubleRange(1.0, 16.0), "lash_range", DEFAULTS.lashRange)
                    .forGetter(KrampusSettings::lashRange),
            ConfigCodecs.field(Codec.intRange(10, 1200), "lash_cooldown", DEFAULTS.lashCooldown)
                    .forGetter(KrampusSettings::lashCooldown),
            ConfigCodecs.field(Codec.intRange(0, 200), "sack_ticks", DEFAULTS.sackTicks)
                    .forGetter(KrampusSettings::sackTicks),
            ConfigCodecs.field(Codec.intRange(20, 6000), "sack_cooldown", DEFAULTS.sackCooldown)
                    .forGetter(KrampusSettings::sackCooldown),
            ConfigCodecs.field(Codec.intRange(0, 8), "summon_goblins", DEFAULTS.summonGoblins)
                    .forGetter(KrampusSettings::summonGoblins),
            ConfigCodecs.field(Codec.BOOL, "clears_karma", DEFAULTS.clearsKarma)
                    .forGetter(KrampusSettings::clearsKarma),
            ConfigCodecs.field(KrampusVisitSettings.CODEC, "visits", DEFAULTS.visits).forGetter(KrampusSettings::visits)
    ).apply(i, KrampusSettings::new)).validate(KrampusSettings::validate);

    private static DataResult<KrampusSettings> validate(KrampusSettings settings) {
        return settings.lashMinRange < settings.lashRange
                ? DataResult.success(settings)
                : DataResult.error(() -> "lash_min_range must be below lash_range");
    }

    /** How many times he has called goblins by the time his health is down to {@code healthFraction} (0..1). */
    public static int summonsDue(float healthFraction) {
        if (healthFraction <= 1.0F / 3.0F) {
            return 2;
        }
        return healthFraction <= 2.0F / 3.0F ? 1 : 0;
    }

    /** Karma after beating him: bad karma is cleared, good karma is kept. */
    public static int karmaAfterDefeat(int karma) {
        return Math.max(0, karma);
    }
}
