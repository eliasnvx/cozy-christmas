package dev.eliasnvx.cozychristmas.present;

import com.mojang.serialization.Codec;
import dev.eliasnvx.cozychristmas.config.ConfigCodecs;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * The {@code presents.snow} config section: during the Christmas season, every {@code intervalTicks} each player has a
 * {@code chance} that a surprise present appears on snow {@code minDistance}..{@code maxDistance} blocks away, unless
 * another present is within {@code spacing} blocks of the spot (presents don't pile up where nobody opens them).
 */
public record SnowPresentSettings(boolean enabled, int intervalTicks, double chance, int minDistance, int maxDistance,
                                  int spacing) {

    public static final SnowPresentSettings DEFAULTS = new SnowPresentSettings(true, 1200, 0.08, 16, 40, 32);

    public static final Codec<SnowPresentSettings> CODEC = RecordCodecBuilder.<SnowPresentSettings>create(i -> i.group(
            ConfigCodecs.field(Codec.BOOL, "enabled", DEFAULTS.enabled).forGetter(SnowPresentSettings::enabled),
            ConfigCodecs.field(Codec.intRange(20, 72000), "interval_ticks", DEFAULTS.intervalTicks)
                    .forGetter(SnowPresentSettings::intervalTicks),
            ConfigCodecs.field(Codec.doubleRange(0.0, 1.0), "chance", DEFAULTS.chance)
                    .forGetter(SnowPresentSettings::chance),
            ConfigCodecs.field(Codec.intRange(0, 128), "min_distance", DEFAULTS.minDistance)
                    .forGetter(SnowPresentSettings::minDistance),
            ConfigCodecs.field(Codec.intRange(1, 128), "max_distance", DEFAULTS.maxDistance)
                    .forGetter(SnowPresentSettings::maxDistance),
            ConfigCodecs.field(Codec.intRange(0, 128), "spacing", DEFAULTS.spacing)
                    .forGetter(SnowPresentSettings::spacing)
    ).apply(i, SnowPresentSettings::new)).validate(SnowPresentSettings::validate);

    private static DataResult<SnowPresentSettings> validate(SnowPresentSettings settings) {
        return settings.minDistance <= settings.maxDistance
                ? DataResult.success(settings)
                : DataResult.error(() -> "min_distance must not be above max_distance");
    }
}
