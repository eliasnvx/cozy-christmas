package dev.eliasnvx.cozychristmas.wood;

import dev.eliasnvx.cozychristmas.config.CozyConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

import java.util.function.Supplier;

/**
 * Leaves of a fruit tree. {@code age} 0 is plain foliage, 1 blossoms, 2 ripe fruit: random ticks move it on
 * ({@link CitrusSettings#growChance()}) and so does bone meal (a stage each time, always), a right click picks the fruit
 * and sends it back to plain leaves, and breaking ripe leaves drops the fruit too (loot table).
 */
public class FruitLeavesBlock extends LeavesBlock implements BonemealableBlock {

    public static final IntegerProperty AGE = BlockStateProperties.AGE_2;
    public static final int RIPE = 2;
    private static final int MIN_LIGHT = 9;

    private final Supplier<Item> fruit;

    public FruitLeavesBlock(Supplier<Item> fruit, Properties properties) {
        super(properties);
        this.fruit = fruit;
        registerDefaultState(defaultBlockState().setValue(AGE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(AGE);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return super.isRandomlyTicking(state) || state.getValue(AGE) < RIPE;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (decaying(state)) {
            super.randomTick(state, level, pos, random);
            return;
        }
        int age = state.getValue(AGE);
        if (age < RIPE && level.getRawBrightness(pos.above(), 0) >= MIN_LIGHT
                && random.nextFloat() < CozyConfig.common().citrus().growChance()) {
            level.setBlock(pos, state.setValue(AGE, age + 1), Block.UPDATE_CLIENTS);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(state));
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (state.getValue(AGE) < RIPE) {
            return InteractionResult.PASS;
        }
        if (level instanceof ServerLevel server) {
            int count = CozyConfig.common().citrus().fruit(server.getRandom().nextFloat());
            if (count > 0) {
                Block.popResource(server, pos, new ItemStack(fruit.get(), count));
            }
            server.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F,
                    0.8F + server.getRandom().nextFloat() * 0.4F);
            BlockState picked = state.setValue(AGE, 0);
            server.setBlock(pos, picked, Block.UPDATE_CLIENTS);
            server.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, picked));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, boolean isClient) {
        return state.getValue(AGE) < RIPE && !decaying(state);
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        level.setBlock(pos, state.setValue(AGE, Math.min(RIPE, state.getValue(AGE) + 1)), Block.UPDATE_CLIENTS);
    }
}
