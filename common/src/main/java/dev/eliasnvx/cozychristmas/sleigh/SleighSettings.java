package dev.eliasnvx.cozychristmas.sleigh;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * The {@code workshop.sleigh} config section. On delivery days (season {@code workshop_delivery}) elves and players
 * load Santa's sleigh, up to {@code capacity} presents; with {@code flies} Santa takes off with it each night of those
 * days and is back at dawn.
 */
public record SleighSettings(boolean flies, int capacity) {

    public static final SleighSettings DEFAULTS = new SleighSettings(true, 24);

    public static final Codec<SleighSettings> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.BOOL.fieldOf("flies").orElse(DEFAULTS.flies).forGetter(SleighSettings::flies),
            Codec.intRange(1, 1000).fieldOf("capacity").orElse(DEFAULTS.capacity).forGetter(SleighSettings::capacity)
    ).apply(i, SleighSettings::new));
}
