package dev.eliasnvx.cozychristmas.tree;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * The {@code tree} config section. A tree is a column of {@code #cozychristmas:tree_parts} (leaves, logs) under a
 * tree star; it counts as decorated with at least {@code minOrnaments} ornaments ({@code #cozychristmas:tree_ornaments})
 * within {@code reach} blocks of the trunk, down to its base and at most {@code height} blocks under the star. Every
 * morning (only during the Christmas season with {@code seasonOnly}) a present appears on the ground within
 * {@code presentRadius} of the trunk for the star's owner, while fewer than {@code maxPresents} lie there; one tree a
 * morning per player.
 */
public record TreeSettings(boolean seasonOnly, int minOrnaments, int reach, int height, int presentRadius, int maxPresents) {

    public static final TreeSettings DEFAULTS = new TreeSettings(true, 6, 5, 24, 3, 3);

    public static final Codec<TreeSettings> CODEC = RecordCodecBuilder.<TreeSettings>create(i -> i.group(
            Codec.BOOL.fieldOf("season_only").orElse(DEFAULTS.seasonOnly).forGetter(TreeSettings::seasonOnly),
            Codec.intRange(0, 256).fieldOf("min_ornaments").orElse(DEFAULTS.minOrnaments).forGetter(TreeSettings::minOrnaments),
            Codec.intRange(1, 16).fieldOf("reach").orElse(DEFAULTS.reach).forGetter(TreeSettings::reach),
            Codec.intRange(2, 64).fieldOf("height").orElse(DEFAULTS.height).forGetter(TreeSettings::height),
            Codec.intRange(1, 8).fieldOf("present_radius").orElse(DEFAULTS.presentRadius).forGetter(TreeSettings::presentRadius),
            Codec.intRange(0, 64).fieldOf("max_presents").orElse(DEFAULTS.maxPresents).forGetter(TreeSettings::maxPresents)
    ).apply(i, TreeSettings::new)).validate(TreeSettings::validate);

    private static DataResult<TreeSettings> validate(TreeSettings settings) {
        return settings.presentRadius <= settings.reach
                ? DataResult.success(settings)
                : DataResult.error(() -> "present_radius must not be above reach");
    }

    /** Enough ornaments for a present. */
    public boolean isDecorated(int ornaments) {
        return ornaments >= minOrnaments;
    }

    /** Room for one more present under a tree that already has {@code presents} around it. */
    public boolean hasRoomFor(int presents) {
        return presents < maxPresents;
    }
}
