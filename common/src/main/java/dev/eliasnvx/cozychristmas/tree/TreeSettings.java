package dev.eliasnvx.cozychristmas.tree;

import com.mojang.serialization.Codec;
import dev.eliasnvx.cozychristmas.config.ConfigCodecs;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;

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

    public static final Codec<TreeSettings> CODEC = ExtraCodecs.validate(RecordCodecBuilder.<TreeSettings>create(i -> i.group(
            ConfigCodecs.field(Codec.BOOL, "season_only", DEFAULTS.seasonOnly).forGetter(TreeSettings::seasonOnly),
            ConfigCodecs.field(Codec.intRange(0, 256), "min_ornaments", DEFAULTS.minOrnaments)
                    .forGetter(TreeSettings::minOrnaments),
            ConfigCodecs.field(Codec.intRange(1, 16), "reach", DEFAULTS.reach).forGetter(TreeSettings::reach),
            ConfigCodecs.field(Codec.intRange(2, 64), "height", DEFAULTS.height).forGetter(TreeSettings::height),
            ConfigCodecs.field(Codec.intRange(1, 8), "present_radius", DEFAULTS.presentRadius)
                    .forGetter(TreeSettings::presentRadius),
            ConfigCodecs.field(Codec.intRange(0, 64), "max_presents", DEFAULTS.maxPresents)
                    .forGetter(TreeSettings::maxPresents)
    ).apply(i, TreeSettings::new)), TreeSettings::validate);

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
