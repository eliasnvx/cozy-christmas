package dev.eliasnvx.cozychristmas.food;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.List;

/**
 * The {@code food} config section. Leaves of the blocks in {@code chestnutLeaves} drop chestnuts with
 * {@code chestnutChance} when they break or decay, like oak leaves drop apples (0 = never). Read when loot tables
 * load: {@code /reload} picks up a change.
 */
public record FoodSettings(float chestnutChance, List<Identifier> chestnutLeaves) {

    public static final FoodSettings DEFAULTS = new FoodSettings(0.02F,
            List.of(Identifier.withDefaultNamespace("dark_oak_leaves")));

    public static final Codec<FoodSettings> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.floatRange(0.0F, 1.0F).fieldOf("chestnut_chance").orElse(DEFAULTS.chestnutChance).forGetter(FoodSettings::chestnutChance),
            Identifier.CODEC.listOf().fieldOf("chestnut_leaves").orElse(DEFAULTS.chestnutLeaves).forGetter(FoodSettings::chestnutLeaves)
    ).apply(i, FoodSettings::new));
}
