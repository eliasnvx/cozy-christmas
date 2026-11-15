package dev.eliasnvx.cozychristmas.stocking;

import dev.eliasnvx.cozychristmas.registry.CozyTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

/**
 * A brick fireplace with a mantel, its hearth open toward {@code facing}. Light it with {@code #cozychristmas:lights_fireplaces}
 * (flint and steel, fire charges), put it out with a shovel. Lit, it glows, crackles and smokes; it never sets anything
 * alight. A stocking hung on the wall above it ({@code #cozychristmas:fireplaces}) fills on Christmas mornings,
 * whether the fire burns or not.
 */
public class FireplaceBlock extends HorizontalDirectionalBlock {

    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final int LIGHT = 13;

    public FireplaceBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }

    /** The hearth faces the player who places it. */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                          InteractionHand hand, BlockHitResult hit) {
        boolean lit = state.getValue(LIT);
        if (!lit && stack.is(CozyTags.LIGHTS_FIREPLACES)) {
            setLit(level, pos, state, true, player);
            level.playSound(player, pos, stack.is(Items.FIRE_CHARGE) ? SoundEvents.FIRECHARGE_USE : SoundEvents.FLINTANDSTEEL_USE,
                    SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);
            if (!level.isClientSide()) {
                if (stack.isDamageableItem()) {
                    stack.hurtAndBreak(1, player, hand.asEquipmentSlot());
                } else {
                    stack.consume(1, player);
                }
            }
            return InteractionResult.SUCCESS;
        }
        if (lit && stack.is(ItemTags.SHOVELS)) {
            setLit(level, pos, state, false, player);
            level.playSound(player, pos, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 0.6F, 1.0F);
            if (!level.isClientSide()) {
                stack.hurtAndBreak(1, player, hand.asEquipmentSlot());
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    private static void setLit(Level level, BlockPos pos, BlockState state, boolean lit, Player player) {
        level.setBlock(pos, state.setValue(LIT, lit), Block.UPDATE_ALL_IMMEDIATE);
        level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
    }

    /** Flames and a little smoke in the hearth, smoke out of the top, and the campfire's crackle now and then. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) {
            return;
        }
        Direction facing = state.getValue(FACING);
        double x = pos.getX() + 0.5 + facing.getStepX() * 0.2;
        double z = pos.getZ() + 0.5 + facing.getStepZ() * 0.2;
        double across = random.nextDouble() * 0.5 - 0.25;
        double ax = facing.getAxis() == Direction.Axis.Z ? across : 0.0;
        double az = facing.getAxis() == Direction.Axis.X ? across : 0.0;
        level.addParticle(ParticleTypes.FLAME, x + ax, pos.getY() + 0.2 + random.nextDouble() * 0.2, z + az, 0.0, 0.0, 0.0);
        if (random.nextInt(4) == 0) {
            level.addParticle(ParticleTypes.SMOKE, x + ax, pos.getY() + 0.5, z + az, 0.0, 0.02, 0.0);
        }
        if (random.nextInt(5) == 0) {
            level.addParticle(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 1.05, pos.getZ() + 0.5, 0.0, 0.04, 0.0);
        }
        if (random.nextInt(10) == 0) {
            level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.CAMPFIRE_CRACKLE,
                    SoundSource.BLOCKS, 0.5F + random.nextFloat(), random.nextFloat() * 0.7F + 0.6F, false);
        }
    }
}
