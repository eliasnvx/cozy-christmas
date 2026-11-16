package dev.eliasnvx.cozychristmas.season;

import com.mojang.serialization.Codec;
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
            SubSeason.CODEC.listOf().fieldOf("sub_seasons").orElse(DEFAULTS.subSeasons).forGetter(SereneSeasonsSettings::subSeasons),
            SubSeason.CODEC.fieldOf("delivery_sub_season").orElse(DEFAULTS.deliverySubSeason)
                    .forGetter(SereneSeasonsSettings::deliverySubSeason),
            Codec.intRange(0, 64).fieldOf("delivery_days").orElse(DEFAULTS.deliveryDays).forGetter(SereneSeasonsSettings::deliveryDays)
    ).apply(i, SereneSeasonsSettings::new));

    public boolean isChristmas(SeasonMoment moment) {
        return subSeasons.contains(moment.subSeason());
    }

    public boolean isDelivery(SeasonMoment moment) {
        return isChristmas(moment) && moment.subSeason() == deliverySubSeason && moment.day() < deliveryDays;
    }
}
