package dev.eliasnvx.cozychristmas.food;

import com.mojang.serialization.Codec;
import dev.eliasnvx.cozychristmas.config.ConfigCodecs;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * The {@code food} config section. Leaves of the blocks in {@code chestnutLeaves} drop chestnuts with
 * {@code chestnutChance} when they break or decay, like oak leaves drop apples (0 = never). The config is read at
 * startup: a change takes a restart (loot tables re-run on /reload, but with the values read then).
 */
public record FoodSettings(float chestnutChance, List<ResourceLocation> chestnutLeaves) {

    public static final FoodSettings DEFAULTS = new FoodSettings(0.02F,
            List.of(ResourceLocation.withDefaultNamespace("dark_oak_leaves")));

    public static final Codec<FoodSettings> CODEC = RecordCodecBuilder.create(i -> i.group(
            ConfigCodecs.field(Codec.floatRange(0.0F, 1.0F), "chestnut_chance", DEFAULTS.chestnutChance)
                    .forGetter(FoodSettings::chestnutChance),
            ConfigCodecs.field(ResourceLocation.CODEC.listOf(), "chestnut_leaves", DEFAULTS.chestnutLeaves)
                    .forGetter(FoodSettings::chestnutLeaves)
    ).apply(i, FoodSettings::new));
}
