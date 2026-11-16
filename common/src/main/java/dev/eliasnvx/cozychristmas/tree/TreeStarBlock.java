package dev.eliasnvx.cozychristmas.tree;

import dev.eliasnvx.cozychristmas.config.CozyConfig;
import dev.eliasnvx.cozychristmas.registry.CozyBlocks;
import dev.eliasnvx.cozychristmas.registry.CozyTags;
import dev.eliasnvx.cozychristmas.season.ChristmasSeason;
import dev.eliasnvx.cozychristmas.stocking.StockingBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * The star on top of a Christmas tree (SPEC: a tree with ornaments, presents under it once a day in December). It
 * stands on a tree part ({@code #cozychristmas:tree_parts}) or a sturdy top, glows steadily and has no collision.
 * Whoever puts it up owns the tree ({@link TreeStarBlockEntity}); a click tells how the tree is doing.
 */
public class TreeStarBlock extends BaseEntityBlock {

    /** A star should shine; steady, never flickering. */
    public static final int LIGHT = 15;
    private static final VoxelShape SHAPE = Block.box(3.0, 0.0, 3.0, 13.0, 15.0, 13.0);

    public TreeStarBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        BlockState under = level.getBlockState(below);
        return under.is(CozyTags.TREE_PARTS) || under.isFaceSturdy(level, below, Direction.UP);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
                                     Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        return direction == Direction.DOWN && !state.canSurvive(level, pos)
                ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, level, ticks, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (placer instanceof ServerPlayer player && level.getBlockEntity(pos) instanceof TreeStarBlockEntity star) {
            star.putUp(player, StockingBlockEntity.morning(level));
            player.sendOverlayMessage(statusMessage(level, pos));
        }
    }

    /** The owner hears how the tree is doing; anyone else, whose it is. */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (player instanceof ServerPlayer server && level.getBlockEntity(pos) instanceof TreeStarBlockEntity star) {
            server.sendOverlayMessage(star.isOwner(player)
                    ? statusMessage(level, pos)
                    : Component.translatable("message.cozychristmas.tree.not_yours", star.ownerName()));
        }
        return InteractionResult.SUCCESS;
    }

    /** "The star needs a tree", "3 of 6 ornaments" or "a present every morning" (or "in the season"). */
    public static Component statusMessage(LevelReader level, BlockPos star) {
        TreeSettings settings = CozyConfig.common().tree();
        ChristmasTree.Scan scan = ChristmasTree.scan(level, star, settings);
        return switch (ChristmasTree.status(scan, settings)) {
            case NO_TREE -> Component.translatable("message.cozychristmas.tree.no_tree");
            case BARE -> Component.translatable("message.cozychristmas.tree.bare", scan.ornaments(), settings.minOrnaments());
            case READY -> Component.translatable(!settings.seasonOnly() || ChristmasSeason.isActive()
                    ? "message.cozychristmas.tree.ready" : "message.cozychristmas.tree.ready_off_season");
        };
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TreeStarBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, CozyBlocks.TREE_STAR_ENTITY.get(), TreeStarBlockEntity::serverTick);
    }
}
