package dev.eliasnvx.cozychristmas.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.eliasnvx.cozychristmas.cosmetic.ToastySettings;
import dev.eliasnvx.cozychristmas.food.FoodSettings;
import dev.eliasnvx.cozychristmas.goblin.GoblinSettings;
import dev.eliasnvx.cozychristmas.karma.KarmaSettings;
import dev.eliasnvx.cozychristmas.krampus.KrampusSettings;
import dev.eliasnvx.cozychristmas.present.PresentSettings;
import dev.eliasnvx.cozychristmas.reindeer.ReindeerSettings;
import dev.eliasnvx.cozychristmas.season.SeasonSettings;
import dev.eliasnvx.cozychristmas.snowman.EvilSnowmanSettings;
import dev.eliasnvx.cozychristmas.stocking.StockingSettings;
import dev.eliasnvx.cozychristmas.tree.TreeSettings;
import dev.eliasnvx.cozychristmas.wood.CitrusSettings;
import dev.eliasnvx.cozychristmas.workshop.WorkshopSettings;

/**
 * Server/common options ({@code config/cozychristmas-common.json}). Each feature owns its section's record and codec;
 * this record only puts them together. Loot, structures and spawns live in data packs and tags, not here.
 */
public record CommonConfig(SeasonSettings season, KarmaSettings karma, PresentSettings presents,
                           ToastySettings toastySet, WorkshopSettings workshop, ReindeerSettings reindeer,
                           GoblinSettings goblins, StockingSettings stockings, EvilSnowmanSettings evilSnowman,
                           KrampusSettings krampus, FoodSettings food, TreeSettings tree, CitrusSettings citrus) {

    public static final CommonConfig DEFAULTS = new CommonConfig(SeasonSettings.DEFAULTS, KarmaSettings.DEFAULTS,
            PresentSettings.DEFAULTS, ToastySettings.DEFAULTS, WorkshopSettings.DEFAULTS, ReindeerSettings.DEFAULTS,
            GoblinSettings.DEFAULTS, StockingSettings.DEFAULTS, EvilSnowmanSettings.DEFAULTS,
            KrampusSettings.DEFAULTS, FoodSettings.DEFAULTS, TreeSettings.DEFAULTS, CitrusSettings.DEFAULTS);

    public static final Codec<CommonConfig> CODEC = RecordCodecBuilder.create(i -> i.group(
            ConfigCodecs.field(SeasonSettings.CODEC, "season", DEFAULTS.season).forGetter(CommonConfig::season),
            ConfigCodecs.field(KarmaSettings.CODEC, "karma", DEFAULTS.karma).forGetter(CommonConfig::karma),
            ConfigCodecs.field(PresentSettings.CODEC, "presents", DEFAULTS.presents).forGetter(CommonConfig::presents),
            ConfigCodecs.field(ToastySettings.CODEC, "toasty_set", DEFAULTS.toastySet)
                    .forGetter(CommonConfig::toastySet),
            ConfigCodecs.field(WorkshopSettings.CODEC, "workshop", DEFAULTS.workshop).forGetter(CommonConfig::workshop),
            ConfigCodecs.field(ReindeerSettings.CODEC, "reindeer", DEFAULTS.reindeer).forGetter(CommonConfig::reindeer),
            ConfigCodecs.field(GoblinSettings.CODEC, "goblins", DEFAULTS.goblins).forGetter(CommonConfig::goblins),
            ConfigCodecs.field(StockingSettings.CODEC, "stockings", DEFAULTS.stockings)
                    .forGetter(CommonConfig::stockings),
            ConfigCodecs.field(EvilSnowmanSettings.CODEC, "evil_snowman", DEFAULTS.evilSnowman)
                    .forGetter(CommonConfig::evilSnowman),
            ConfigCodecs.field(KrampusSettings.CODEC, "krampus", DEFAULTS.krampus).forGetter(CommonConfig::krampus),
            ConfigCodecs.field(FoodSettings.CODEC, "food", DEFAULTS.food).forGetter(CommonConfig::food),
            ConfigCodecs.field(TreeSettings.CODEC, "tree", DEFAULTS.tree).forGetter(CommonConfig::tree),
            ConfigCodecs.field(CitrusSettings.CODEC, "citrus", DEFAULTS.citrus).forGetter(CommonConfig::citrus)
    ).apply(i, CommonConfig::new));
}
