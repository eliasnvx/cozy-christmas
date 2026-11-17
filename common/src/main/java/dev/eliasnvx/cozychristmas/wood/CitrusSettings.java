package dev.eliasnvx.cozychristmas.wood;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.eliasnvx.cozychristmas.config.ConfigCodecs;

/**
 * The {@code citrus} config section: tangerine and orange trees. Each random tick a leaf block that can bear fruit
 * ({@link FruitLeavesBlock}) moves one stage on (leaves, blossom, fruit) with {@code growChance} while there is light
 * above it; picking ripe fruit gives {@code minFruit} to {@code maxFruit} of them. With {@code wildTrees} groves grow
 * in the biomes of {@code #cozychristmas:has_tangerine_trees} and {@code has_orange_trees}. The config is read at startup.
 */
public record CitrusSettings(float growChance, int minFruit, int maxFruit, boolean wildTrees) {

    public static final CitrusSettings DEFAULTS = new CitrusSettings(0.15F, 1, 3, true);

    public static final Codec<CitrusSettings> CODEC = RecordCodecBuilder.<CitrusSettings>create(i -> i.group(
            ConfigCodecs.field(Codec.floatRange(0.0F, 1.0F), "grow_chance", DEFAULTS.growChance)
                    .forGetter(CitrusSettings::growChance),
            ConfigCodecs.field(Codec.intRange(0, 16), "min_fruit", DEFAULTS.minFruit).forGetter(CitrusSettings::minFruit),
            ConfigCodecs.field(Codec.intRange(0, 16), "max_fruit", DEFAULTS.maxFruit).forGetter(CitrusSettings::maxFruit),
            ConfigCodecs.field(Codec.BOOL, "wild_trees", DEFAULTS.wildTrees).forGetter(CitrusSettings::wildTrees)
    ).apply(i, CitrusSettings::new)).flatXmap(CitrusSettings::validate, DataResult::success);

    private static DataResult<CitrusSettings> validate(CitrusSettings settings) {
        return settings.minFruit <= settings.maxFruit
                ? DataResult.success(settings)
                : DataResult.error(() -> "min_fruit must not be above max_fruit");
    }

    /** How many fruit one harvest gives for a random roll in {@code 0..1}. */
    public int fruit(float roll) {
        return minFruit + Math.min(maxFruit - minFruit, (int) (roll * (maxFruit - minFruit + 1)));
    }
}
