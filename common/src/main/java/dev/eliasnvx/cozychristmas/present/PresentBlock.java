package dev.eliasnvx.cozychristmas.present;

import dev.eliasnvx.cozychristmas.registry.CozyBlocks;
import dev.eliasnvx.cozychristmas.workshop.WorkshopAlarm;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

/**
 * A present on the ground. Right-click opens it ({@link PresentBlockEntity#open}); breaking it drops the present with
 * its contents, so it can be carried and given away. While it's going to go boom and a player stands close it ticks:
 * {@link #TICKING} switches its block model off and {@code PresentRenderer} draws it shaking instead.
 */
public class PresentBlock extends BaseEntityBlock {

    public static final EnumProperty<PresentDesign> DESIGN = EnumProperty.create("design", PresentDesign.class);
    public static final BooleanProperty TICKING = BooleanProperty.create("ticking");

    /** The box and the bow on top. */
    private static final VoxelShape SHAPE = Shapes.or(Block.box(2.0, 0.0, 2.0, 14.0, 10.0, 14.0),
            Block.box(5.0, 10.0, 5.0, 11.0, 13.0, 11.0));

    public PresentBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(DESIGN, PresentDesign.RED).setValue(TICKING, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DESIGN, TICKING);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PresentBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, CozyBlocks.PRESENT_ENTITY.get(), PresentBlockEntity::serverTick);
    }

    /** Breaking a present of the workshop warehouse is theft too (like gold guarded by piglins). */
    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (player instanceof ServerPlayer thief && level.getBlockEntity(pos) instanceof PresentBlockEntity present
                && present.isWorkshopStock()) {
            WorkshopAlarm.theft(thief.level(), pos, thief);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    /** Blowing up the warehouse is theft too, by whoever lit the TNT or fired the shot. */
    @Override
    protected void onExplosionHit(BlockState state, ServerLevel level, BlockPos pos, Explosion explosion,
                                  BiConsumer<ItemStack, BlockPos> drops) {
        if (explosion.getIndirectSourceEntity() instanceof ServerPlayer thief
                && level.getBlockEntity(pos) instanceof PresentBlockEntity present && present.isWorkshopStock()) {
            WorkshopAlarm.theft(level, pos, thief);
        }
        super.onExplosionHit(state, level, pos, explosion, drops);
    }

    /** Any hand opens it: block use comes before item use, and sneaking with an item still places blocks against it. */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (player instanceof ServerPlayer opener && level.getBlockEntity(pos) instanceof PresentBlockEntity present) {
            present.open(opener);
        }
        return InteractionResult.SUCCESS;
    }
}
