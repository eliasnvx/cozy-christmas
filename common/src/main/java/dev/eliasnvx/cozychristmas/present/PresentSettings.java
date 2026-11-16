package dev.eliasnvx.cozychristmas.present;

import com.mojang.serialization.Codec;
import dev.eliasnvx.cozychristmas.config.ConfigCodecs;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.eliasnvx.cozychristmas.karma.KarmaTier;

/**
 * The {@code presents} config section: what's inside a surprise present for each karma tier, how close a player must
 * come for a boom to tick ({@code warningRadius}, SPEC: 3 blocks), the {@link BoomSettings boom} itself and presents
 * appearing {@link SnowPresentSettings in the snow}.
 */
public record PresentSettings(PresentOdds nice, PresentOdds neutral, PresentOdds naughty, double warningRadius,
                              BoomSettings boom, SnowPresentSettings snow) {

    public static final PresentSettings DEFAULTS = new PresentSettings(
            new PresentOdds(80, 10, 10),
            new PresentOdds(60, 20, 20),
            new PresentOdds(30, 40, 30),
            3.0,
            BoomSettings.DEFAULTS,
            SnowPresentSettings.DEFAULTS);

    public static final Codec<PresentSettings> CODEC = RecordCodecBuilder.create(i -> i.group(
            ConfigCodecs.field(PresentOdds.CODEC, "nice", DEFAULTS.nice).forGetter(PresentSettings::nice),
            ConfigCodecs.field(PresentOdds.CODEC, "neutral", DEFAULTS.neutral).forGetter(PresentSettings::neutral),
            ConfigCodecs.field(PresentOdds.CODEC, "naughty", DEFAULTS.naughty).forGetter(PresentSettings::naughty),
            ConfigCodecs.field(Codec.doubleRange(0.0, 16.0), "warning_radius", DEFAULTS.warningRadius)
                    .forGetter(PresentSettings::warningRadius),
            ConfigCodecs.field(BoomSettings.CODEC, "boom", DEFAULTS.boom).forGetter(PresentSettings::boom),
            ConfigCodecs.field(SnowPresentSettings.CODEC, "snow", DEFAULTS.snow).forGetter(PresentSettings::snow)
    ).apply(i, PresentSettings::new));

    public PresentOdds odds(KarmaTier tier) {
        return switch (tier) {
            case NICE -> nice;
            case NEUTRAL -> neutral;
            case NAUGHTY -> naughty;
        };
    }
}
