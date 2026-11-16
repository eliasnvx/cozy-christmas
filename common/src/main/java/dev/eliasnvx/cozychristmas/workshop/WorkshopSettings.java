package dev.eliasnvx.cozychristmas.workshop;

import com.mojang.serialization.Codec;
import dev.eliasnvx.cozychristmas.config.ConfigCodecs;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.eliasnvx.cozychristmas.sleigh.SleighSettings;

/**
 * The {@code workshop} config section: the people of Santa's workshop. Where and how often the workshop generates is
 * data (structure set {@code cozychristmas:santas_workshop}, biome tag {@code #cozychristmas:has_structure/santas_workshop}).
 */
public record WorkshopSettings(SantaSettings santa, ElfSettings elf, SleighSettings sleigh) {

    public static final WorkshopSettings DEFAULTS = new WorkshopSettings(SantaSettings.DEFAULTS, ElfSettings.DEFAULTS, SleighSettings.DEFAULTS);

    public static final Codec<WorkshopSettings> CODEC = RecordCodecBuilder.create(i -> i.group(
            ConfigCodecs.field(SantaSettings.CODEC, "santa", DEFAULTS.santa).forGetter(WorkshopSettings::santa),
            ConfigCodecs.field(ElfSettings.CODEC, "elf", DEFAULTS.elf).forGetter(WorkshopSettings::elf),
            ConfigCodecs.field(SleighSettings.CODEC, "sleigh", DEFAULTS.sleigh).forGetter(WorkshopSettings::sleigh)
    ).apply(i, WorkshopSettings::new));
}
