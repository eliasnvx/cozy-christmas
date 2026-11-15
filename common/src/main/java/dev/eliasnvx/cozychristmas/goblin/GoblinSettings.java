package dev.eliasnvx.cozychristmas.goblin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** The {@code goblins} config section: present goblins and the gingerbread men around their camps. */
public record GoblinSettings(PresentGoblinSettings presentGoblin, GingerbreadManSettings gingerbreadMan) {

    public static final GoblinSettings DEFAULTS = new GoblinSettings(PresentGoblinSettings.DEFAULTS, GingerbreadManSettings.DEFAULTS);

    public static final Codec<GoblinSettings> CODEC = RecordCodecBuilder.create(i -> i.group(
            PresentGoblinSettings.CODEC.fieldOf("present_goblin").orElse(DEFAULTS.presentGoblin).forGetter(GoblinSettings::presentGoblin),
            GingerbreadManSettings.CODEC.fieldOf("gingerbread_man").orElse(DEFAULTS.gingerbreadMan).forGetter(GoblinSettings::gingerbreadMan)
    ).apply(i, GoblinSettings::new));
}
