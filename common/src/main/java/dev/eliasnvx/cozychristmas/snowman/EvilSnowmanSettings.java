package dev.eliasnvx.cozychristmas.snowman;

import com.mojang.serialization.Codec;
import dev.eliasnvx.cozychristmas.config.ConfigCodecs;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * The {@code evil_snowman} config section. A snowman has {@code health}, walks at {@code speed} and throws an ice
 * snowball ({@link IceSnowballSettings}) every {@code throwInterval} to {@code throwInterval * 1.5} ticks at targets
 * within {@code throwRange} blocks. Fire hurts it {@code fireMultiplier} times as much (burning, lava, warm biomes); so
 * does standing within {@code heatRadius} blocks of a lit {@code #cozychristmas:melts_snowmen} block (0 = off). It
 * spawns at night under the open sky in {@code #cozychristmas:spawns_evil_snowmen} with {@code spawnWeight} (0 = never),
 * in groups of {@code minGroup}..{@code maxGroup}.
 */
public record EvilSnowmanSettings(double health, double speed, int throwInterval, double throwRange, double fireMultiplier,
                                  int heatRadius, int spawnWeight, int minGroup, int maxGroup, IceSnowballSettings iceSnowball) {

    public static final EvilSnowmanSettings DEFAULTS = new EvilSnowmanSettings(16.0, 0.25, 30, 12.0, 3.0, 2, 20, 1, 2,
            IceSnowballSettings.DEFAULTS);

    public static final Codec<EvilSnowmanSettings> CODEC = RecordCodecBuilder.<EvilSnowmanSettings>create(i -> i.group(
            ConfigCodecs.field(Codec.doubleRange(1.0, 1024.0), "health", DEFAULTS.health)
                    .forGetter(EvilSnowmanSettings::health),
            ConfigCodecs.field(Codec.doubleRange(0.0, 1.0), "speed", DEFAULTS.speed)
                    .forGetter(EvilSnowmanSettings::speed),
            ConfigCodecs.field(Codec.intRange(5, 600), "throw_interval", DEFAULTS.throwInterval)
                    .forGetter(EvilSnowmanSettings::throwInterval),
            ConfigCodecs.field(Codec.doubleRange(2.0, 32.0), "throw_range", DEFAULTS.throwRange)
                    .forGetter(EvilSnowmanSettings::throwRange),
            ConfigCodecs.field(Codec.doubleRange(1.0, 100.0), "fire_multiplier", DEFAULTS.fireMultiplier)
                    .forGetter(EvilSnowmanSettings::fireMultiplier),
            ConfigCodecs.field(Codec.intRange(0, 4), "heat_radius", DEFAULTS.heatRadius)
                    .forGetter(EvilSnowmanSettings::heatRadius),
            ConfigCodecs.field(Codec.intRange(0, 1000), "spawn_weight", DEFAULTS.spawnWeight)
                    .forGetter(EvilSnowmanSettings::spawnWeight),
            ConfigCodecs.field(Codec.intRange(1, 16), "min_group", DEFAULTS.minGroup)
                    .forGetter(EvilSnowmanSettings::minGroup),
            ConfigCodecs.field(Codec.intRange(1, 16), "max_group", DEFAULTS.maxGroup)
                    .forGetter(EvilSnowmanSettings::maxGroup),
            ConfigCodecs.field(IceSnowballSettings.CODEC, "ice_snowball", DEFAULTS.iceSnowball)
                    .forGetter(EvilSnowmanSettings::iceSnowball)
    ).apply(i, EvilSnowmanSettings::new)).validate(EvilSnowmanSettings::validate);

    private static DataResult<EvilSnowmanSettings> validate(EvilSnowmanSettings settings) {
        return settings.minGroup <= settings.maxGroup
                ? DataResult.success(settings)
                : DataResult.error(() -> "min_group must not be above max_group");
    }

    /** The slowest throw: half again the interval. */
    public int maxThrowInterval() {
        return throwInterval + throwInterval / 2;
    }

    /** Fire damage after the snowman's weakness to it. */
    public float fireDamage(float amount) {
        return (float) (amount * fireMultiplier);
    }
}
