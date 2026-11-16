package dev.eliasnvx.cozychristmas.sleigh;

import com.mojang.serialization.Codec;
import dev.eliasnvx.cozychristmas.config.ConfigCodecs;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * The {@code workshop.sleigh} config section. On delivery days (season {@code workshop_delivery}) elves and players
 * load Santa's sleigh, up to {@code capacity} presents; with {@code flies} Santa takes off with it each night of those
 * days and is back at dawn.
 */
public record SleighSettings(boolean flies, int capacity) {

    public static final SleighSettings DEFAULTS = new SleighSettings(true, 24);

    public static final Codec<SleighSettings> CODEC = RecordCodecBuilder.create(i -> i.group(
            ConfigCodecs.field(Codec.BOOL, "flies", DEFAULTS.flies).forGetter(SleighSettings::flies),
            ConfigCodecs.field(Codec.intRange(1, 1000), "capacity", DEFAULTS.capacity)
                    .forGetter(SleighSettings::capacity)
    ).apply(i, SleighSettings::new));
}
