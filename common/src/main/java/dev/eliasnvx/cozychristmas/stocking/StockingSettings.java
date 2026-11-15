package dev.eliasnvx.cozychristmas.stocking;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * The {@code stockings} config section. A stocking hung over a fireplace fills once a morning (at most one stocking
 * per player a day): only during the Christmas season with {@code seasonOnly}; a naughty owner finds
 * {@code coalMin}..{@code coalMax} coal.
 */
public record StockingSettings(boolean seasonOnly, int coalMin, int coalMax) {

    public static final StockingSettings DEFAULTS = new StockingSettings(true, 1, 3);

    public static final Codec<StockingSettings> CODEC = RecordCodecBuilder.<StockingSettings>create(i -> i.group(
            Codec.BOOL.fieldOf("season_only").orElse(DEFAULTS.seasonOnly).forGetter(StockingSettings::seasonOnly),
            Codec.intRange(1, 64).fieldOf("coal_min").orElse(DEFAULTS.coalMin).forGetter(StockingSettings::coalMin),
            Codec.intRange(1, 64).fieldOf("coal_max").orElse(DEFAULTS.coalMax).forGetter(StockingSettings::coalMax)
    ).apply(i, StockingSettings::new)).validate(StockingSettings::validate);

    private static DataResult<StockingSettings> validate(StockingSettings settings) {
        return settings.coalMin <= settings.coalMax
                ? DataResult.success(settings)
                : DataResult.error(() -> "coal_min must not be above coal_max");
    }
}
