package dev.eliasnvx.cozychristmas.present;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.eliasnvx.cozychristmas.karma.KarmaTier;

/** The {@code presents} config section: what's inside a present for each karma tier. */
public record PresentSettings(PresentOdds nice, PresentOdds neutral, PresentOdds naughty) {

    public static final PresentSettings DEFAULTS = new PresentSettings(
            new PresentOdds(80, 10, 10),
            new PresentOdds(60, 20, 20),
            new PresentOdds(30, 40, 30));

    public static final Codec<PresentSettings> CODEC = RecordCodecBuilder.create(i -> i.group(
            PresentOdds.CODEC.fieldOf("nice").orElse(DEFAULTS.nice).forGetter(PresentSettings::nice),
            PresentOdds.CODEC.fieldOf("neutral").orElse(DEFAULTS.neutral).forGetter(PresentSettings::neutral),
            PresentOdds.CODEC.fieldOf("naughty").orElse(DEFAULTS.naughty).forGetter(PresentSettings::naughty)
    ).apply(i, PresentSettings::new));

    public PresentOdds odds(KarmaTier tier) {
        return switch (tier) {
            case NICE -> nice;
            case NEUTRAL -> neutral;
            case NAUGHTY -> naughty;
        };
    }
}
