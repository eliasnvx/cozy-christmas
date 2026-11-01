package dev.eliasnvx.cozychristmas.season;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * The {@code season} config section. In season reindeer fly, presents turn up in the snow and under trees and Santa
 * hands out daily gifts; on the {@code workshopDelivery} days elves load the sleigh. Outside it everything else works.
 */
public record SeasonSettings(SeasonSource source, DateWindow season, DateWindow workshopDelivery) {

    public static final SeasonSettings DEFAULTS = new SeasonSettings(SeasonSource.REAL_DATE,
            DateWindow.of(12, 1, 1, 7), DateWindow.of(12, 24, 12, 25));

    public static final Codec<SeasonSettings> CODEC = RecordCodecBuilder.create(i -> i.group(
            SeasonSource.CODEC.fieldOf("source").orElse(DEFAULTS.source).forGetter(SeasonSettings::source),
            DateWindow.CODEC.fieldOf("season").orElse(DEFAULTS.season).forGetter(SeasonSettings::season),
            DateWindow.CODEC.fieldOf("workshop_delivery").orElse(DEFAULTS.workshopDelivery).forGetter(SeasonSettings::workshopDelivery)
    ).apply(i, SeasonSettings::new));
}
