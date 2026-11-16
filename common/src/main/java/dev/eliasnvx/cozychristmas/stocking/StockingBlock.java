package dev.eliasnvx.cozychristmas.stocking;

import dev.eliasnvx.cozychristmas.registry.CozyBlocks;
import dev.eliasnvx.cozychristmas.util.Players;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

/**
 * A stocking hung on a wall, facing out of it. Hung over a fireplace it fills on Christmas mornings
 * ({@link StockingBlockEntity}); its owner takes what's inside with a click. What sticks out of the top shows on the
 * block ({@link StockingFill}). It needs the wall behind it, like a wall banner, and has no collision.
 */
public class StockingBlock extends BaseEntityBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<StockingFill> FILL = EnumProperty.create("fill", StockingFill.class);
    public static final EnumProperty<StockingStyle> STYLE = EnumProperty.create("style", StockingStyle.class);

    /** Against the wall: 8 wide, the full height, 4 deep (the foot sticks out). */
    private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<>(Direction.class);

    static {
        SHAPES.put(Direction.SOUTH, Block.box(3.0, 0.0, 0.0, 13.0, 16.0, 4.0));
        SHAPES.put(Direction.NORTH, Block.box(3.0, 0.0, 12.0, 13.0, 16.0, 16.0));
        SHAPES.put(Direction.EAST, Block.box(0.0, 0.0, 3.0, 4.0, 16.0, 13.0));
        SHAPES.put(Direction.WEST, Block.box(12.0, 0.0, 3.0, 16.0, 16.0, 13.0));
    }

    public StockingBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(FILL, StockingFill.EMPTY)
                .setValue(STYLE, StockingStyle.STOCKING));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, FILL, STYLE);
    }

    /** {@link BaseEntityBlock} draws nothing by default; the stocking is a block model. */
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    /** Only on a wall: the clicked side, or failing that the first wall the player looks toward. */
    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        for (Direction direction : context.getNearestLookingDirections()) {
            if (direction.getAxis().isHorizontal()) {
                BlockState state = defaultBlockState().setValue(FACING, direction.getOpposite());
                if (state.canSurvive(context.getLevel(), context.getClickedPos())) {
                    return state;
                }
            }
        }
        return null;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos wall = pos.relative(facing.getOpposite());
        return level.getBlockState(wall).isFaceSturdy(level, wall, facing);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level,
                                  BlockPos pos, BlockPos neighborPos) {
        return direction == state.getValue(FACING).getOpposite() && !state.canSurvive(level, pos)
                ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (placer instanceof Player player && level.getBlockEntity(pos) instanceof StockingBlockEntity stocking) {
            stocking.hang(player, stack, StockingBlockEntity.morning(level));
        }
    }

    /** The owner takes what's inside (whatever is in hand); anyone else is told whose it is, an empty one says what it waits for. */
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        if (!(player instanceof ServerPlayer server) || !(level.getBlockEntity(pos) instanceof StockingBlockEntity stocking)) {
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        if (!stocking.isOwner(player) && Players.inPlay(player)) { // creative players may empty any
            server.displayClientMessage(Component.translatable("message.cozychristmas.stocking.not_yours"), true);
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        ItemStack taken = stocking.takeContents();
        if (taken.isEmpty()) {
            server.displayClientMessage(Component.translatable(StockingBlockEntity.hangsOverFireplace(level, pos, state)
                    ? "message.cozychristmas.stocking.empty" : "message.cozychristmas.stocking.no_fireplace"), true);
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        server.getInventory().placeItemBackInInventory(taken); // drops it if full, tells the client
        level.playSound(null, pos, SoundEvents.BUNDLE_REMOVE_ONE, SoundSource.BLOCKS, 0.8F, 1.0F);
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    /** Taking the stocking down (not a change of its fill): what it holds and the stocking itself drop. */
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof StockingBlockEntity stocking) {
            stocking.dropContents();
        }
        super.onRemove(state, level, pos, newState, moved);
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (player.isCreative() && level.getBlockEntity(pos) instanceof StockingBlockEntity stocking) {
            stocking.forgetHungItem();
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    /**
     * Only its owner (or a creative player) takes it down: otherwise anyone could break it and keep what it holds or
     * put up their own in its place.
     */
    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof StockingBlockEntity stocking && !stocking.isOwner(player) && Players.inPlay(player)) {
            return 0.0F;
        }
        return super.getDestroyProgress(state, player, level, pos);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new StockingBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, CozyBlocks.STOCKING_ENTITY.get(), StockingBlockEntity::serverTick);
    }
}
