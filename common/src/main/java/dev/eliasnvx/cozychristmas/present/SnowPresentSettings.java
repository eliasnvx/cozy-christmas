package dev.eliasnvx.cozychristmas.present;

import com.mojang.serialization.Codec;
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
            Codec.BOOL.fieldOf("enabled").orElse(DEFAULTS.enabled).forGetter(SnowPresentSettings::enabled),
            Codec.intRange(20, 72000).fieldOf("interval_ticks").orElse(DEFAULTS.intervalTicks).forGetter(SnowPresentSettings::intervalTicks),
            Codec.doubleRange(0.0, 1.0).fieldOf("chance").orElse(DEFAULTS.chance).forGetter(SnowPresentSettings::chance),
            Codec.intRange(0, 128).fieldOf("min_distance").orElse(DEFAULTS.minDistance).forGetter(SnowPresentSettings::minDistance),
            Codec.intRange(1, 128).fieldOf("max_distance").orElse(DEFAULTS.maxDistance).forGetter(SnowPresentSettings::maxDistance),
            Codec.intRange(0, 128).fieldOf("spacing").orElse(DEFAULTS.spacing).forGetter(SnowPresentSettings::spacing)
    ).apply(i, SnowPresentSettings::new)).validate(SnowPresentSettings::validate);

    private static DataResult<SnowPresentSettings> validate(SnowPresentSettings settings) {
        return settings.minDistance <= settings.maxDistance
                ? DataResult.success(settings)
                : DataResult.error(() -> "min_distance must not be above max_distance");
    }
}
