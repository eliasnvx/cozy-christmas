package dev.eliasnvx.cozychristmas.present;

import dev.architectury.event.events.common.TickEvent;
import dev.eliasnvx.cozychristmas.config.CozyConfig;
import dev.eliasnvx.cozychristmas.registry.CozyBlocks;
import dev.eliasnvx.cozychristmas.registry.CozyTags;
import dev.eliasnvx.cozychristmas.season.ChristmasSeason;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * SPEC "now and then, just in the snow in December": during the Christmas season surprise presents appear on snow
 * around players ({@link SnowPresentSettings}). A spot counts if it's a thin snow layer on solid ground (the present
 * replaces it, so it sits in the snow) or the top of {@code #cozychristmas:snow_present_ground} (snow blocks, powder
 * snow). Only in loaded chunks; nothing is generated or loaded for it.
 */
public final class SnowPresents {

    private SnowPresents() {
    }

    public static void register() {
        TickEvent.SERVER_LEVEL_POST.register(SnowPresents::tick);
    }

    private static void tick(ServerLevel level) {
        SnowPresentSettings snow = settings();
        if (!snow.enabled() || level.getGameTime() % snow.intervalTicks() != 0 || !ChristmasSeason.isActive()) {
            return;
        }
        for (ServerPlayer player : level.players()) {
            if (!player.isSpectator() && level.getRandom().nextDouble() < snow.chance()) {
                trySpawnNear(level, player.blockPosition(), level.getRandom());
            }
        }
    }

    /** Picks a column in the ring around {@code center} and tries its surface. */
    public static boolean trySpawnNear(ServerLevel level, BlockPos center, RandomSource random) {
        SnowPresentSettings snow = settings();
        float angle = random.nextFloat() * Mth.TWO_PI;
        double distance = Mth.lerp(random.nextDouble(), snow.minDistance(), snow.maxDistance());
        int x = center.getX() + Mth.floor(Mth.cos(angle) * distance);
        int z = center.getZ() + Mth.floor(Mth.sin(angle) * distance);
        BlockPos column = new BlockPos(x, 0, z);
        if (!level.isLoaded(column)) {
            return false;
        }
        BlockPos surface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, column);
        return trySpawnAt(level, surface, random, snow.spacing());
    }

    /**
     * Puts a surprise present in a random design at {@code pos} (the first free block above the ground, as the motion
     * heightmap gives it) if that's in the snow and no present is within {@code spacing}.
     */
    public static boolean trySpawnAt(ServerLevel level, BlockPos pos, RandomSource random, int spacing) {
        if (!isSnowySpot(level, pos) || hasPresentNear(level, pos, spacing)) {
            return false;
        }
        PresentDesign design = PresentDesign.values()[random.nextInt(PresentDesign.values().length)];
        return level.setBlock(pos, CozyBlocks.PRESENT.get().defaultBlockState().setValue(PresentBlock.DESIGN, design),
                Block.UPDATE_ALL);
    }

    private static boolean isSnowySpot(ServerLevel level, BlockPos pos) {
        BlockState at = level.getBlockState(pos);
        BlockPos groundPos = pos.below();
        BlockState ground = level.getBlockState(groundPos);
        if (!at.getFluidState().isEmpty()) {
            return false;
        }
        if (at.is(Blocks.SNOW)) {
            // thin layers don't block motion, so the heightmap stops in them: the present takes the layer's place
            return ground.isFaceSturdy(level, groundPos, Direction.UP);
        }
        return at.isAir() && ground.is(CozyTags.SNOW_PRESENT_GROUND);
    }

    /** Looks at the block entities of the loaded chunks around {@code pos}; never loads one. */
    private static boolean hasPresentNear(ServerLevel level, BlockPos pos, int spacing) {
        if (spacing <= 0) {
            return false;
        }
        double maxDistanceSqr = (double) spacing * spacing;
        int minChunkX = (pos.getX() - spacing) >> 4;
        int maxChunkX = (pos.getX() + spacing) >> 4;
        int minChunkZ = (pos.getZ() - spacing) >> 4;
        int maxChunkZ = (pos.getZ() + spacing) >> 4;
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(chunkX, chunkZ);
                if (chunk == null) {
                    continue;
                }
                for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                    if (blockEntity instanceof PresentBlockEntity && blockEntity.getBlockPos().distSqr(pos) <= maxDistanceSqr) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static SnowPresentSettings settings() {
        return CozyConfig.common().presents().snow();
    }
}
