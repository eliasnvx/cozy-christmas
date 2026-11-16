package dev.eliasnvx.cozychristmas.season;

import com.mojang.serialization.Codec;
import dev.eliasnvx.cozychristmas.config.ConfigCodecs;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

/**
 * {@code season.serene_seasons}: with {@code source: serene_seasons} the Christmas season is these {@code subSeasons}
 * of the Overworld's Serene Seasons calendar, and the sleigh is loaded on the first {@code deliveryDays} days of
 * {@code deliverySubSeason} (Christmas falls early in winter; late winter is January's).
 */
public record SereneSeasonsSettings(List<SubSeason> subSeasons, SubSeason deliverySubSeason, int deliveryDays) {

    public static final SereneSeasonsSettings DEFAULTS = new SereneSeasonsSettings(
            List.of(SubSeason.EARLY_WINTER, SubSeason.MID_WINTER), SubSeason.MID_WINTER, 2);

    public static final Codec<SereneSeasonsSettings> CODEC = RecordCodecBuilder.create(i -> i.group(
            ConfigCodecs.field(SubSeason.CODEC.listOf(), "sub_seasons", DEFAULTS.subSeasons)
                    .forGetter(SereneSeasonsSettings::subSeasons),
            ConfigCodecs.field(SubSeason.CODEC, "delivery_sub_season", DEFAULTS.deliverySubSeason)
                    .forGetter(SereneSeasonsSettings::deliverySubSeason),
            ConfigCodecs.field(Codec.intRange(0, 64), "delivery_days", DEFAULTS.deliveryDays)
                    .forGetter(SereneSeasonsSettings::deliveryDays)
    ).apply(i, SereneSeasonsSettings::new));

    public boolean isChristmas(SeasonMoment moment) {
        return subSeasons.contains(moment.subSeason());
    }

    public boolean isDelivery(SeasonMoment moment) {
        return isChristmas(moment) && moment.subSeason() == deliverySubSeason && moment.day() < deliveryDays;
    }
}
