package dev.eliasnvx.cozychristmas.present;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.eliasnvx.cozychristmas.karma.KarmaTier;

/**
 * The {@code presents} config section: what's inside a surprise present for each karma tier, how close a player must
 * come for a boom to tick ({@code warningRadius}, SPEC: 3 blocks) and the {@link BoomSettings boom} itself.
 */
public record PresentSettings(PresentOdds nice, PresentOdds neutral, PresentOdds naughty, double warningRadius,
                              BoomSettings boom) {

    public static final PresentSettings DEFAULTS = new PresentSettings(
            new PresentOdds(80, 10, 10),
            new PresentOdds(60, 20, 20),
            new PresentOdds(30, 40, 30),
            3.0,
            BoomSettings.DEFAULTS);

    public static final Codec<PresentSettings> CODEC = RecordCodecBuilder.create(i -> i.group(
            PresentOdds.CODEC.fieldOf("nice").orElse(DEFAULTS.nice).forGetter(PresentSettings::nice),
            PresentOdds.CODEC.fieldOf("neutral").orElse(DEFAULTS.neutral).forGetter(PresentSettings::neutral),
            PresentOdds.CODEC.fieldOf("naughty").orElse(DEFAULTS.naughty).forGetter(PresentSettings::naughty),
            Codec.doubleRange(0.0, 16.0).fieldOf("warning_radius").orElse(DEFAULTS.warningRadius).forGetter(PresentSettings::warningRadius),
            BoomSettings.CODEC.fieldOf("boom").orElse(DEFAULTS.boom).forGetter(PresentSettings::boom)
    ).apply(i, PresentSettings::new));

    public PresentOdds odds(KarmaTier tier) {
        return switch (tier) {
            case NICE -> nice;
            case NEUTRAL -> neutral;
            case NAUGHTY -> naughty;
        };
    }
}
