package dev.eliasnvx.cozychristmas.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.eliasnvx.cozychristmas.cosmetic.ToastySettings;
import dev.eliasnvx.cozychristmas.karma.KarmaSettings;
import dev.eliasnvx.cozychristmas.present.PresentSettings;
import dev.eliasnvx.cozychristmas.season.SeasonSettings;

/**
 * Server/common options ({@code config/cozychristmas-common.json}). Each feature owns its section's record and codec;
 * this record only puts them together. Loot, structures and spawns live in data packs and tags, not here.
 */
public record CommonConfig(SeasonSettings season, KarmaSettings karma, PresentSettings presents,
                           ToastySettings toastySet) {

    public static final CommonConfig DEFAULTS = new CommonConfig(SeasonSettings.DEFAULTS, KarmaSettings.DEFAULTS,
            PresentSettings.DEFAULTS, ToastySettings.DEFAULTS);

    public static final Codec<CommonConfig> CODEC = RecordCodecBuilder.create(i -> i.group(
            SeasonSettings.CODEC.fieldOf("season").orElse(DEFAULTS.season).forGetter(CommonConfig::season),
            KarmaSettings.CODEC.fieldOf("karma").orElse(DEFAULTS.karma).forGetter(CommonConfig::karma),
            PresentSettings.CODEC.fieldOf("presents").orElse(DEFAULTS.presents).forGetter(CommonConfig::presents),
            ToastySettings.CODEC.fieldOf("toasty_set").orElse(DEFAULTS.toastySet).forGetter(CommonConfig::toastySet)
    ).apply(i, CommonConfig::new));
}
