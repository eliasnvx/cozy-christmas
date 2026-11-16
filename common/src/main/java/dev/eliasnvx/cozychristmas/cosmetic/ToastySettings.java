package dev.eliasnvx.cozychristmas.cosmetic;

import com.mojang.serialization.Codec;
import dev.eliasnvx.cozychristmas.config.ConfigCodecs;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * The {@code toasty_set} config section. With the whole set on, {@code keepsWarm} stops freezing in powder snow and
 * {@code snowSpeedBonus} is added to the base walking speed on {@code #cozychristmas:toasty_snow} (0.1 = +10%, 0 = off).
 */
public record ToastySettings(boolean keepsWarm, double snowSpeedBonus) {

    public static final ToastySettings DEFAULTS = new ToastySettings(true, 0.1);

    public static final Codec<ToastySettings> CODEC = RecordCodecBuilder.create(i -> i.group(
            ConfigCodecs.field(Codec.BOOL, "keeps_warm", DEFAULTS.keepsWarm).forGetter(ToastySettings::keepsWarm),
            ConfigCodecs.field(Codec.doubleRange(0.0, 1.0), "snow_speed_bonus", DEFAULTS.snowSpeedBonus)
                    .forGetter(ToastySettings::snowSpeedBonus)
    ).apply(i, ToastySettings::new));
}
