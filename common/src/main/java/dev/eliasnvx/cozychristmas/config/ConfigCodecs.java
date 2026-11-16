package dev.eliasnvx.cozychristmas.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;

import java.util.stream.Stream;

/** Codec helpers for the config's sections. */
public final class ConfigCodecs {

    private ConfigCodecs() {
    }

    /**
     * An option: a missing key takes {@code fallback} (so upgrades fill new options in), a present but invalid one is
     * an error (the file is then kept as .broken and the error logged, instead of a value quietly replaced), and it is
     * always written, defaults included, so the file shows every option. DFU 6's {@code optionalFieldOf} turns an
     * invalid value into a missing one, so this decodes the field itself.
     */
    public static <A> MapCodec<A> field(Codec<A> codec, String name, A fallback) {
        return new MapCodec<>() {
            @Override
            public <T> Stream<T> keys(DynamicOps<T> ops) {
                return Stream.of(ops.createString(name));
            }

            @Override
            public <T> DataResult<A> decode(DynamicOps<T> ops, MapLike<T> input) {
                T value = input.get(name);
                if (value == null) {
                    return DataResult.success(fallback);
                }
                return codec.parse(ops, value).mapError(error -> name + ": " + error);
            }

            @Override
            public <T> RecordBuilder<T> encode(A input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
                return prefix.add(name, codec.encodeStart(ops, input));
            }

            @Override
            public String toString() {
                return "ConfigField[" + name + ": " + codec + "]";
            }
        };
    }
}
