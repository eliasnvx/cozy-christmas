package dev.eliasnvx.cozychristmas.workshop;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.eliasnvx.cozychristmas.sleigh.SleighSettings;

/**
 * The {@code workshop} config section: the people of Santa's workshop. Where and how often the workshop generates is
 * data (structure set {@code cozychristmas:santas_workshop}, biome tag {@code #cozychristmas:has_structure/santas_workshop}).
 */
public record WorkshopSettings(SantaSettings santa, ElfSettings elf, SleighSettings sleigh) {

    public static final WorkshopSettings DEFAULTS = new WorkshopSettings(SantaSettings.DEFAULTS, ElfSettings.DEFAULTS, SleighSettings.DEFAULTS);

    public static final Codec<WorkshopSettings> CODEC = RecordCodecBuilder.create(i -> i.group(
            SantaSettings.CODEC.fieldOf("santa").orElse(DEFAULTS.santa).forGetter(WorkshopSettings::santa),
            ElfSettings.CODEC.fieldOf("elf").orElse(DEFAULTS.elf).forGetter(WorkshopSettings::elf),
            SleighSettings.CODEC.fieldOf("sleigh").orElse(DEFAULTS.sleigh).forGetter(WorkshopSettings::sleigh)
    ).apply(i, WorkshopSettings::new));
}
