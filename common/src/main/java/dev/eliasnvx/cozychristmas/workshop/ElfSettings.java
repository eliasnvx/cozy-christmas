package dev.eliasnvx.cozychristmas.workshop;

import com.mojang.serialization.Codec;
import dev.eliasnvx.cozychristmas.config.ConfigCodecs;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * The {@code workshop.elf} config section: an elf's {@code health} and walking {@code speed}. Elves work at
 * {@code #cozychristmas:elf_workstations} within {@code workRange} blocks.
 */
public record ElfSettings(double health, double speed, int workRange) {

    public static final ElfSettings DEFAULTS = new ElfSettings(12.0, 0.3, 10);

    public static final Codec<ElfSettings> CODEC = RecordCodecBuilder.create(i -> i.group(
            ConfigCodecs.field(Codec.doubleRange(1.0, 1024.0), "health", DEFAULTS.health)
                    .forGetter(ElfSettings::health),
            ConfigCodecs.field(Codec.doubleRange(0.0, 1.0), "speed", DEFAULTS.speed).forGetter(ElfSettings::speed),
            ConfigCodecs.field(Codec.intRange(1, 32), "work_range", DEFAULTS.workRange)
                    .forGetter(ElfSettings::workRange)
    ).apply(i, ElfSettings::new));
}
