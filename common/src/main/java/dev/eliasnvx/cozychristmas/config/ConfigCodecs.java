package dev.eliasnvx.cozychristmas.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import java.util.Optional;

/** Codec helpers for the config's sections. */
public final class ConfigCodecs {

    private ConfigCodecs() {
    }

    /**
     * An option: a missing key takes {@code fallback} (so upgrades fill new options in), a present but invalid one is
     * an error (the file is then kept as .broken and the error logged, instead of a value quietly replaced), and it is
     * always written, defaults included, so the file shows every option.
     */
    public static <A> MapCodec<A> field(Codec<A> codec, String name, A fallback) {
        return codec.optionalFieldOf(name).xmap(value -> value.orElse(fallback), Optional::of);
    }
}
