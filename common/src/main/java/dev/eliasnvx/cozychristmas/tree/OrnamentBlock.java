package dev.eliasnvx.cozychristmas.tree;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

/**
 * A glass ornament: hangs under a block, on its side (from a little hook) or sits on top, like a button. Unlike a
 * button it holds on to leaves, which aren't sturdy: that's where ornaments go. Counted around a tree star
 * ({@code #cozychristmas:tree_ornaments}). No collision.
 */
public class OrnamentBlock extends FaceAttachedHorizontalDirectionalBlock {

    private static final VoxelShape HANGING = Block.box(5.0, 5.0, 5.0, 11.0, 16.0, 11.0);
    private static final VoxelShape STANDING = Block.box(5.0, 0.0, 5.0, 11.0, 7.0, 11.0);
    /** On a wall, by the direction it faces (out of the wall). */
    private static final Map<Direction, VoxelShape> ON_WALL = new EnumMap<>(Direction.class);

    static {
        ON_WALL.put(Direction.NORTH, Block.box(5.0, 4.0, 9.0, 11.0, 13.0, 16.0));
        ON_WALL.put(Direction.SOUTH, Block.box(5.0, 4.0, 0.0, 11.0, 13.0, 7.0));
        ON_WALL.put(Direction.WEST, Block.box(9.0, 4.0, 5.0, 16.0, 13.0, 11.0));
        ON_WALL.put(Direction.EAST, Block.box(0.0, 4.0, 5.0, 7.0, 13.0, 11.0));
    }

    private static final Codec<OrnamentColor> COLOR_CODEC = Codec.STRING.comapFlatMap(
            name -> Arrays.stream(OrnamentColor.values()).filter(color -> color.name().toLowerCase(Locale.ROOT).equals(name))
                    .findFirst().map(DataResult::success)
                    .orElseGet(() -> DataResult.error(() -> "Unknown ornament color: " + name)),
            color -> color.name().toLowerCase(Locale.ROOT));
    public static final MapCodec<OrnamentBlock> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            COLOR_CODEC.fieldOf("color").forGetter(OrnamentBlock::color),
            propertiesCodec()
    ).apply(i, OrnamentBlock::new));

    private final OrnamentColor color;

    public OrnamentBlock(OrnamentColor color, Properties properties) {
        super(properties);
        this.color = color;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(FACE, AttachFace.CEILING));
    }

    @Override
    protected MapCodec<? extends OrnamentBlock> codec() {
        return CODEC;
    }

    public OrnamentColor color() {
        return color;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, FACE);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACE)) {
            case CEILING -> HANGING;
            case FLOOR -> STANDING;
            case WALL -> ON_WALL.get(state.getValue(FACING));
        };
    }

    /** On leaves (any face) or on a sturdy face, as a button would be. */
    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction toHolder = getConnectedDirection(state).getOpposite();
        BlockPos holder = pos.relative(toHolder);
        BlockState holderState = level.getBlockState(holder);
        return holderState.is(BlockTags.LEAVES) || holderState.isFaceSturdy(level, holder, toHolder.getOpposite());
    }
}
