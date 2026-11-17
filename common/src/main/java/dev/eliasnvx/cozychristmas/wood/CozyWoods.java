package dev.eliasnvx.cozychristmas.wood;

import dev.architectury.event.EventResult;
import dev.architectury.registry.level.biome.BiomeModifications;
import dev.eliasnvx.cozychristmas.config.CozyConfig;
import dev.eliasnvx.cozychristmas.registry.CozyTags;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import dev.architectury.event.events.common.InteractionEvent;
import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.mixin.FireBlockInvoker;
import dev.eliasnvx.cozychristmas.registry.CozyItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.MapColor;

import java.util.List;

/**
 * The tangerine and orange woods: registered with the mod, made to burn, and stripped with an axe. Their data (recipes,
 * tags, loot, worldgen) is in the data pack, generated like the rest of the resources.
 */
public final class CozyWoods {

    public static final WoodSet TANGERINE = new WoodSet("tangerine", MapColor.COLOR_ORANGE, MapColor.TERRACOTTA_BROWN,
            () -> CozyItems.TANGERINE.get());
    public static final WoodSet ORANGE = new WoodSet("orange", MapColor.TERRACOTTA_ORANGE, MapColor.COLOR_BROWN,
            () -> CozyItems.ORANGE.get());
    public static final List<WoodSet> ALL = List.of(TANGERINE, ORANGE);

    /** Fire spreads to (ignite odds) and eats (burn odds) these: the same numbers as vanilla's planks, logs and leaves. */
    private static final int PLANKS_IGNITE = 5;
    private static final int PLANKS_BURN = 20;
    private static final int LOG_IGNITE = 5;
    private static final int LOG_BURN = 5;
    private static final int LEAVES_IGNITE = 30;
    private static final int LEAVES_BURN = 60;

    private CozyWoods() {
    }

    /** Loads the class (queuing the registrations) and hooks up the behavior; call before the registries are written. */
    public static void init() {
        for (WoodSet set : ALL) {
            flammable(set);
        }
        InteractionEvent.RIGHT_CLICK_BLOCK.register(CozyWoods::strip);
    }

    /** Wild groves, through the biomes' tags; call after the config is loaded. */
    public static void initWorldgen() {
        if (!CozyConfig.common().citrus().wildTrees()) {
            return;
        }
        grove(CozyTags.HAS_TANGERINE_TREES, "tangerine_grove");
        grove(CozyTags.HAS_ORANGE_TREES, "orange_grove");
    }

    private static void grove(TagKey<Biome> biomes, String feature) {
        ResourceKey<PlacedFeature> key = ResourceKey.create(Registries.PLACED_FEATURE, CozyChristmas.id(feature));
        BiomeModifications.addProperties(context -> context.hasTag(biomes), (context, properties) ->
                properties.getGenerationProperties().addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, key));
    }

    private static void flammable(WoodSet set) {
        set.planks.listen(block -> burn(block, PLANKS_IGNITE, PLANKS_BURN));
        set.slab.listen(block -> burn(block, PLANKS_IGNITE, PLANKS_BURN));
        set.stairs.listen(block -> burn(block, PLANKS_IGNITE, PLANKS_BURN));
        set.fence.listen(block -> burn(block, PLANKS_IGNITE, PLANKS_BURN));
        set.fenceGate.listen(block -> burn(block, PLANKS_IGNITE, PLANKS_BURN));
        for (var log : List.of(set.log, set.strippedLog, set.wood, set.strippedWood)) {
            log.listen(block -> burn(block, LOG_IGNITE, LOG_BURN));
        }
        set.leaves.listen(block -> burn(block, LEAVES_IGNITE, LEAVES_BURN));
    }

    private static void burn(Block block, int igniteOdds, int burnOdds) {
        ((FireBlockInvoker) Blocks.FIRE).cozychristmas$setFlammable(block, igniteOdds, burnOdds);
    }

    /** A sign of one of our woods: the sign block entity types accept it ({@code BlockEntityTypeMixin}). */
    public static boolean isSign(Block block) {
        return ours(block) && (block instanceof StandingSignBlock || block instanceof WallSignBlock);
    }

    public static boolean isHangingSign(Block block) {
        return ours(block) && (block instanceof CeilingHangingSignBlock || block instanceof WallHangingSignBlock);
    }

    private static boolean ours(Block block) {
        return BuiltInRegistries.BLOCK.getKey(block).getNamespace().equals(CozyChristmas.MOD_ID);
    }

    /** The stripped version of one of our logs or woods, keeping the axis. */
    public static BlockState stripped(BlockState state) {
        for (WoodSet set : ALL) {
            Block to = null;
            if (state.is(set.log.get())) {
                to = set.strippedLog.get();
            } else if (state.is(set.wood.get())) {
                to = set.strippedWood.get();
            }
            if (to != null) {
                return to.defaultBlockState().setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS));
            }
        }
        return null;
    }

    /** Vanilla's axe strips what its block transformer lists: that list is a registry, so ours is done here. */
    private static EventResult strip(Player player, InteractionHand hand, BlockPos pos, Direction face) {
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.is(ItemTags.AXES) || player.isShiftKeyDown()) {
            return EventResult.pass();
        }
        Level level = player.level();
        BlockState stripped = stripped(level.getBlockState(pos));
        if (stripped == null) {
            return EventResult.pass();
        }
        if (!level.isClientSide()) {
            level.setBlock(pos, stripped, Block.UPDATE_ALL_IMMEDIATE);
            level.playSound(null, pos, SoundEvents.AXE_STRIP.value(), SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, stripped));
            stack.hurtAndBreak(1, player, hand);
        }
        return EventResult.interruptTrue();
    }
}
