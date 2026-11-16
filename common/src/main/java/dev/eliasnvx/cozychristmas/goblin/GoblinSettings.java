package dev.eliasnvx.cozychristmas.goblin;

import com.mojang.serialization.Codec;
import dev.eliasnvx.cozychristmas.config.ConfigCodecs;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** The {@code goblins} config section: present goblins and the gingerbread men around their camps. */
public record GoblinSettings(PresentGoblinSettings presentGoblin, GingerbreadManSettings gingerbreadMan) {

    public static final GoblinSettings DEFAULTS = new GoblinSettings(PresentGoblinSettings.DEFAULTS, GingerbreadManSettings.DEFAULTS);

    public static final Codec<GoblinSettings> CODEC = RecordCodecBuilder.create(i -> i.group(
            ConfigCodecs.field(PresentGoblinSettings.CODEC, "present_goblin", DEFAULTS.presentGoblin)
                    .forGetter(GoblinSettings::presentGoblin),
            ConfigCodecs.field(GingerbreadManSettings.CODEC, "gingerbread_man", DEFAULTS.gingerbreadMan)
                    .forGetter(GoblinSettings::gingerbreadMan)
    ).apply(i, GoblinSettings::new));
}
