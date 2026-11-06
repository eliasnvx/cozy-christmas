package dev.eliasnvx.cozychristmas.workshop;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * The {@code workshop.elf} config section: an elf's {@code health} and walking {@code speed}. Elves work at
 * {@code #cozychristmas:elf_workstations} within {@code workRange} blocks.
 */
public record ElfSettings(double health, double speed, int workRange) {

    public static final ElfSettings DEFAULTS = new ElfSettings(12.0, 0.3, 10);

    public static final Codec<ElfSettings> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.doubleRange(1.0, 1024.0).fieldOf("health").orElse(DEFAULTS.health).forGetter(ElfSettings::health),
            Codec.doubleRange(0.0, 1.0).fieldOf("speed").orElse(DEFAULTS.speed).forGetter(ElfSettings::speed),
            Codec.intRange(1, 32).fieldOf("work_range").orElse(DEFAULTS.workRange).forGetter(ElfSettings::workRange)
    ).apply(i, ElfSettings::new));
}
