package dev.eliasnvx.cozychristmas.workshop;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * The {@code workshop} config section: the people of Santa's workshop. Where and how often the workshop generates is
 * data (structure set {@code cozychristmas:santas_workshop}, biome tag {@code #cozychristmas:has_structure/santas_workshop}).
 */
public record WorkshopSettings(SantaSettings santa, ElfSettings elf) {

    public static final WorkshopSettings DEFAULTS = new WorkshopSettings(SantaSettings.DEFAULTS, ElfSettings.DEFAULTS);

    public static final Codec<WorkshopSettings> CODEC = RecordCodecBuilder.create(i -> i.group(
            SantaSettings.CODEC.fieldOf("santa").orElse(DEFAULTS.santa).forGetter(WorkshopSettings::santa),
            ElfSettings.CODEC.fieldOf("elf").orElse(DEFAULTS.elf).forGetter(WorkshopSettings::elf)
    ).apply(i, WorkshopSettings::new));
}
