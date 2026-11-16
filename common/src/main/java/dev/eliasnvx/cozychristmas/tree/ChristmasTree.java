package dev.eliasnvx.cozychristmas.tree;

import dev.eliasnvx.cozychristmas.registry.CozyBlocks;
import dev.eliasnvx.cozychristmas.registry.CozyTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Reads a Christmas tree off the world: the trunk under a star, the ornaments on it and the ground around its base.
 * Nothing here changes a block; {@link TreeStarBlockEntity} does that.
 */
public final class ChristmasTree {

    /** Presents may sit a block above or below the trunk's base, on uneven ground. */
    private static final int GROUND_STEP = 1;
    /** The levels tried for a present, nearest first. */
    private static final int[] GROUND_LEVELS = {0, GROUND_STEP, -GROUND_STEP};

    private ChristmasTree() {
    }

    /**
     * What a star at {@code star} stands on: {@code base} is the lowest block of the column of
     * {@code #cozychristmas:tree_parts} under it (null if the star doesn't stand on one), {@code ornaments} how many
     * {@code #cozychristmas:tree_ornaments} hang within {@code reach} of that column, from the base up to the star.
     */
    public record Scan(@Nullable BlockPos base, int ornaments) {

        public boolean isTree() {
            return base != null;
        }
    }

    /** What the star's owner is told about the tree. */
    public enum Status {
        /** The star doesn't stand on a tree. */
        NO_TREE,
        /** A tree, but too few ornaments on it. */
        BARE,
        /** Decorated: presents will come. */
        READY
    }

    public static Scan scan(LevelReader level, BlockPos star, TreeSettings settings) {
        BlockPos base = trunkBase(level, star, settings.height());
        if (base == null) {
            return new Scan(null, 0);
        }
        int ornaments = 0;
        int reach = settings.reach();
        for (BlockPos pos : BlockPos.betweenClosed(base.getX() - reach, base.getY(), base.getZ() - reach,
                star.getX() + reach, star.getY() - 1, star.getZ() + reach)) {
            if (level.getBlockState(pos).is(CozyTags.TREE_ORNAMENTS)) {
                ornaments++;
            }
        }
        return new Scan(base, ornaments);
    }

    public static Status status(Scan scan, TreeSettings settings) {
        if (!scan.isTree()) {
            return Status.NO_TREE;
        }
        return settings.isDecorated(scan.ornaments()) ? Status.READY : Status.BARE;
    }

    /** The lowest tree part of the unbroken column under the star, at most {@code height} down; null if none. */
    public static @Nullable BlockPos trunkBase(LevelReader level, BlockPos star, int height) {
        BlockPos base = null;
        BlockPos.MutableBlockPos pos = star.mutable();
        for (int i = 0; i < height; i++) {
            pos.move(Direction.DOWN);
            if (level.isOutsideBuildHeight(pos) || !level.getBlockState(pos).is(CozyTags.TREE_PARTS)) {
                break;
            }
            base = pos.immutable();
        }
        return base;
    }

    /** Presents lying within {@code presentRadius} of the trunk, around the base's level. */
    public static int presentsAround(LevelReader level, BlockPos base, TreeSettings settings) {
        int presents = 0;
        int radius = settings.presentRadius();
        for (BlockPos pos : BlockPos.betweenClosed(base.offset(-radius, -GROUND_STEP, -radius), base.offset(radius, GROUND_STEP, radius))) {
            if (level.getBlockState(pos).is(CozyBlocks.PRESENT.get())) {
                presents++;
            }
        }
        return presents;
    }

    /**
     * Free spots on the ground around the trunk: within {@code presentRadius} of it, not the trunk's own column, a
     * block that can be replaced (air, a snow layer, grass; no water) on top of a sturdy one or snow, at the base's
     * level or a step up or down (the nearest one counts).
     */
    public static List<BlockPos> presentSpots(LevelReader level, BlockPos base, TreeSettings settings) {
        List<BlockPos> spots = new ArrayList<>();
        int radius = settings.presentRadius();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx == 0 && dz == 0) {
                    continue;
                }
                for (int dy : GROUND_LEVELS) {
                    BlockPos pos = base.offset(dx, dy, dz);
                    if (isFreeGround(level, pos)) {
                        spots.add(pos);
                        break;
                    }
                }
            }
        }
        return spots;
    }

    /** One of {@link #presentSpots}, or null if there's none. */
    public static @Nullable BlockPos pickSpot(LevelReader level, BlockPos base, TreeSettings settings, RandomSource random) {
        List<BlockPos> spots = presentSpots(level, base, settings);
        return spots.isEmpty() ? null : spots.get(random.nextInt(spots.size()));
    }

    private static boolean isFreeGround(LevelReader level, BlockPos pos) {
        BlockState at = level.getBlockState(pos);
        if (!at.canBeReplaced() || !at.getFluidState().isEmpty()) {
            return false;
        }
        BlockPos groundPos = pos.below();
        BlockState ground = level.getBlockState(groundPos);
        return ground.isFaceSturdy(level, groundPos, Direction.UP) || ground.is(CozyTags.SNOW_PRESENT_GROUND);
    }
}
