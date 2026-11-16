package dev.eliasnvx.cozychristmas.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.eliasnvx.cozychristmas.cosmetic.ToastySettings;
import dev.eliasnvx.cozychristmas.goblin.GoblinSettings;
import dev.eliasnvx.cozychristmas.karma.KarmaSettings;
import dev.eliasnvx.cozychristmas.krampus.KrampusSettings;
import dev.eliasnvx.cozychristmas.present.PresentSettings;
import dev.eliasnvx.cozychristmas.reindeer.ReindeerSettings;
import dev.eliasnvx.cozychristmas.season.SeasonSettings;
import dev.eliasnvx.cozychristmas.snowman.EvilSnowmanSettings;
import dev.eliasnvx.cozychristmas.stocking.StockingSettings;
import dev.eliasnvx.cozychristmas.workshop.WorkshopSettings;

/**
 * Server/common options ({@code config/cozychristmas-common.json}). Each feature owns its section's record and codec;
 * this record only puts them together. Loot, structures and spawns live in data packs and tags, not here.
 */
public record CommonConfig(SeasonSettings season, KarmaSettings karma, PresentSettings presents,
                           ToastySettings toastySet, WorkshopSettings workshop, ReindeerSettings reindeer,
                           GoblinSettings goblins, StockingSettings stockings, EvilSnowmanSettings evilSnowman,
                           KrampusSettings krampus) {

    public static final CommonConfig DEFAULTS = new CommonConfig(SeasonSettings.DEFAULTS, KarmaSettings.DEFAULTS,
            PresentSettings.DEFAULTS, ToastySettings.DEFAULTS, WorkshopSettings.DEFAULTS, ReindeerSettings.DEFAULTS,
            GoblinSettings.DEFAULTS, StockingSettings.DEFAULTS, EvilSnowmanSettings.DEFAULTS,
            KrampusSettings.DEFAULTS);

    public static final Codec<CommonConfig> CODEC = RecordCodecBuilder.create(i -> i.group(
            SeasonSettings.CODEC.fieldOf("season").orElse(DEFAULTS.season).forGetter(CommonConfig::season),
            KarmaSettings.CODEC.fieldOf("karma").orElse(DEFAULTS.karma).forGetter(CommonConfig::karma),
            PresentSettings.CODEC.fieldOf("presents").orElse(DEFAULTS.presents).forGetter(CommonConfig::presents),
            ToastySettings.CODEC.fieldOf("toasty_set").orElse(DEFAULTS.toastySet).forGetter(CommonConfig::toastySet),
            WorkshopSettings.CODEC.fieldOf("workshop").orElse(DEFAULTS.workshop).forGetter(CommonConfig::workshop),
            ReindeerSettings.CODEC.fieldOf("reindeer").orElse(DEFAULTS.reindeer).forGetter(CommonConfig::reindeer),
            GoblinSettings.CODEC.fieldOf("goblins").orElse(DEFAULTS.goblins).forGetter(CommonConfig::goblins),
            StockingSettings.CODEC.fieldOf("stockings").orElse(DEFAULTS.stockings).forGetter(CommonConfig::stockings),
            EvilSnowmanSettings.CODEC.fieldOf("evil_snowman").orElse(DEFAULTS.evilSnowman).forGetter(CommonConfig::evilSnowman),
            KrampusSettings.CODEC.fieldOf("krampus").orElse(DEFAULTS.krampus).forGetter(CommonConfig::krampus)
    ).apply(i, CommonConfig::new));
}
