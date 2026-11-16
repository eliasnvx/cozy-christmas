package dev.eliasnvx.cozychristmas.season;

import com.mojang.serialization.Codec;
import dev.eliasnvx.cozychristmas.config.ConfigCodecs;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * The {@code season} config section. In season reindeer fly, presents turn up in the snow and under trees and Santa
 * hands out daily gifts; on the {@code workshopDelivery} days elves load the sleigh. Outside it everything else works.
 * The dates count with {@code source: real_date}; {@code serene_seasons} reads that mod's calendar instead.
 */
public record SeasonSettings(SeasonSource source, DateWindow season, DateWindow workshopDelivery,
                             SereneSeasonsSettings sereneSeasons) {

    public static final SeasonSettings DEFAULTS = new SeasonSettings(SeasonSource.REAL_DATE,
            DateWindow.of(12, 1, 1, 7), DateWindow.of(12, 24, 12, 25), SereneSeasonsSettings.DEFAULTS);

    public static final Codec<SeasonSettings> CODEC = RecordCodecBuilder.create(i -> i.group(
            ConfigCodecs.field(SeasonSource.CODEC, "source", DEFAULTS.source).forGetter(SeasonSettings::source),
            ConfigCodecs.field(DateWindow.CODEC, "season", DEFAULTS.season).forGetter(SeasonSettings::season),
            ConfigCodecs.field(DateWindow.CODEC, "workshop_delivery", DEFAULTS.workshopDelivery)
                    .forGetter(SeasonSettings::workshopDelivery),
            ConfigCodecs.field(SereneSeasonsSettings.CODEC, "serene_seasons", DEFAULTS.sereneSeasons)
                    .forGetter(SeasonSettings::sereneSeasons)
    ).apply(i, SeasonSettings::new));
}
