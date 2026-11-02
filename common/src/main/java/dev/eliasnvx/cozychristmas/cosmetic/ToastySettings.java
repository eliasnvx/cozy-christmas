package dev.eliasnvx.cozychristmas.cosmetic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * The {@code toasty_set} config section. With the whole set on, {@code keepsWarm} stops freezing in powder snow and
 * {@code snowSpeedBonus} is added to the base walking speed on {@code #cozychristmas:toasty_snow} (0.1 = +10%, 0 = off).
 */
public record ToastySettings(boolean keepsWarm, double snowSpeedBonus) {

    public static final ToastySettings DEFAULTS = new ToastySettings(true, 0.1);

    public static final Codec<ToastySettings> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.BOOL.fieldOf("keeps_warm").orElse(DEFAULTS.keepsWarm).forGetter(ToastySettings::keepsWarm),
            Codec.doubleRange(0.0, 1.0).fieldOf("snow_speed_bonus").orElse(DEFAULTS.snowSpeedBonus).forGetter(ToastySettings::snowSpeedBonus)
    ).apply(i, ToastySettings::new));
}
