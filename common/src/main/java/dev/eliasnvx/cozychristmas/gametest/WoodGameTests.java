package dev.eliasnvx.cozychristmas.gametest;

import dev.architectury.event.events.common.InteractionEvent;
import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.config.CozyConfig;
import dev.eliasnvx.cozychristmas.wood.CozyWoods;
import dev.eliasnvx.cozychristmas.wood.FruitLeavesBlock;
import dev.eliasnvx.cozychristmas.wood.WoodSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.entity.HangingSignBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.ChestBoat;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The tangerine and orange woods on a real server, shared by both loaders (Fabric: {@code fabric/src/gametest},
 * NeoForge: {@code neoforge/.../dev/CozyGameTestsNeoForge}). Dev only: not in the released jars.
 */
public final class WoodGameTests {

    public static final List<GameTestEntry> ALL = List.of(
            new GameTestEntry("wood_sets_are_complete", WoodGameTests::setsAreComplete),
            new GameTestEntry("wood_recipes_craft", WoodGameTests::recipesCraft),
            new GameTestEntry("wood_blocks_are_in_the_vanilla_tags", WoodGameTests::tags),
            new GameTestEntry("wood_signs_are_sign_block_entities", WoodGameTests::signsAreBlockEntities),
            new GameTestEntry("wood_boats_are_entities", WoodGameTests::boatsFloat),
            new GameTestEntry("an_axe_strips_our_logs", WoodGameTests::axeStrips),
            new GameTestEntry("saplings_grow_fruit_trees", WoodGameTests::saplingsGrow),
            new GameTestEntry("fruit_ripens_and_is_picked", WoodGameTests::fruitRipensAndIsPicked),
            new GameTestEntry("wild_groves_are_in_their_biomes", WoodGameTests::grovesAreInTheirBiomes),
            new GameTestEntry("bone_meal_ripens_the_fruit", WoodGameTests::boneMealRipens));

    private static final BlockPos AT = new BlockPos(2, 1, 2);
    private static final Vec3 PLAYER = new Vec3(4.5, 1.0, 4.5);
    private static final int RIPENING_TRIES = 400;
    private static final int TREE_SEARCH = 8;
    private static final int GROWTH_TRIES = 40;

    private WoodGameTests() {
    }

    /** Every block has its item, its loot table and a translation; the leaves bear fruit. */
    public static void setsAreComplete(GameTestHelper helper) {
        for (WoodSet set : CozyWoods.ALL) {
            for (var block : set.all()) {
                String id = block.getId().getPath();
                boolean wall = id.contains("wall_") || id.startsWith("potted_");
                if (!wall) {
                    helper.assertTrue(BuiltInRegistries.ITEM.containsKey(block.getId()), "an item for " + id);
                }
                if (!id.contains("wall_")) {
                    LootTable loot = helper.getLevel().getServer().reloadableRegistries()
                            .getLootTable(ResourceKey.create(Registries.LOOT_TABLE, CozyChristmas.id("blocks/" + id)));
                    helper.assertTrue(loot != LootTable.EMPTY, "a loot table for " + id);
                }
            }
            helper.assertTrue(set.leaves.get().defaultBlockState().hasProperty(FruitLeavesBlock.AGE), set.name + " leaves age");
        }
        helper.succeed();
    }

    /** Planks from a log, a door and slabs from planks, a hanging sign from stripped logs and chains. */
    public static void recipesCraft(GameTestHelper helper) {
        for (WoodSet set : CozyWoods.ALL) {
            ItemStack log = new ItemStack(set.log.get());
            ItemStack planks = new ItemStack(set.planks.get());
            ItemStack stripped = new ItemStack(set.strippedLog.get());
            assertCrafts(helper, set.planks.get().asItem(), 4, CraftingInput.of(1, 1, List.of(log)));
            assertCrafts(helper, set.door.get().asItem(), 3, CraftingInput.of(2, 3, repeat(planks, 6)));
            assertCrafts(helper, set.slab.get().asItem(), 6, CraftingInput.of(3, 1, repeat(planks, 3)));
            assertCrafts(helper, set.wood.get().asItem(), 3, CraftingInput.of(2, 2, repeat(log, 4)));
            ItemStack boat = new ItemStack(CozyItemsHolder.item(set.name + "_boat"));
            assertCrafts(helper, boat.getItem(), 1, CraftingInput.of(3, 2,
                    List.of(planks, ItemStack.EMPTY, planks, planks, planks, planks)));
            assertCrafts(helper, CozyItemsHolder.item(set.name + "_chest_boat"), 1, CraftingInput.of(2, 1,
                    List.of(new ItemStack(Items.CHEST), boat)));
            ItemStack chain = new ItemStack(Items.CHAIN);
            assertCrafts(helper, set.hangingSign.get().asItem(), 6, CraftingInput.of(3, 3,
                    List.of(chain, ItemStack.EMPTY, chain, stripped, stripped, stripped, stripped, stripped, stripped)));
        }
        helper.succeed();
    }

    /** The blocks and items are in the tags the rest of the game goes by. */
    public static void tags(GameTestHelper helper) {
        for (WoodSet set : CozyWoods.ALL) {
            BlockState planks = set.planks.get().defaultBlockState();
            helper.assertTrue(planks.is(BlockTags.PLANKS) && planks.is(BlockTags.MINEABLE_WITH_AXE), "planks");
            helper.assertTrue(new ItemStack(set.planks.get()).is(ItemTags.PLANKS), "planks item");
            helper.assertTrue(set.log.get().defaultBlockState().is(BlockTags.LOGS), "log is a log");
            helper.assertTrue(set.log.get().defaultBlockState().is(BlockTags.LOGS_THAT_BURN), "natural log");
            helper.assertTrue(set.strippedWood.get().defaultBlockState().is(BlockTags.LOGS), "stripped wood");
            helper.assertTrue(set.leaves.get().defaultBlockState().is(BlockTags.LEAVES), "leaves");
            helper.assertTrue(new ItemStack(set.leaves.get()).is(ItemTags.LEAVES), "leaves item");
            helper.assertTrue(set.sapling.get().defaultBlockState().is(BlockTags.SAPLINGS), "sapling");
            helper.assertTrue(set.stairs.get().defaultBlockState().is(BlockTags.WOODEN_STAIRS), "stairs");
            helper.assertTrue(set.slab.get().defaultBlockState().is(BlockTags.WOODEN_SLABS), "slab");
            helper.assertTrue(set.fence.get().defaultBlockState().is(BlockTags.WOODEN_FENCES), "fence");
            helper.assertTrue(set.fenceGate.get().defaultBlockState().is(BlockTags.FENCE_GATES), "gate");
            helper.assertTrue(set.door.get().defaultBlockState().is(BlockTags.WOODEN_DOORS), "door");
            helper.assertTrue(set.trapdoor.get().defaultBlockState().is(BlockTags.WOODEN_TRAPDOORS), "trapdoor");
            helper.assertTrue(set.button.get().defaultBlockState().is(BlockTags.WOODEN_BUTTONS), "button");
            helper.assertTrue(set.pressurePlate.get().defaultBlockState().is(BlockTags.WOODEN_PRESSURE_PLATES), "plate");
            helper.assertTrue(set.sign.get().defaultBlockState().is(BlockTags.STANDING_SIGNS), "sign");
            helper.assertTrue(set.wallSign.get().defaultBlockState().is(BlockTags.WALL_SIGNS), "wall sign");
            helper.assertTrue(set.hangingSign.get().defaultBlockState().is(BlockTags.CEILING_HANGING_SIGNS), "hanging sign");
            helper.assertTrue(set.wallHangingSign.get().defaultBlockState().is(BlockTags.WALL_HANGING_SIGNS), "wall hanging sign");
        }
        helper.succeed();
    }

    /** Vanilla's sign block entity types list their blocks once; ours are let in. */
    public static void signsAreBlockEntities(GameTestHelper helper) {
        for (WoodSet set : CozyWoods.ALL) {
            helper.setBlock(AT, set.sign.get());
            helper.assertTrue(helper.getLevel().getBlockEntity(helper.absolutePos(AT)) instanceof SignBlockEntity,
                    set.name + " sign has a sign block entity");
            helper.setBlock(AT, set.hangingSign.get());
            helper.assertTrue(helper.getLevel().getBlockEntity(helper.absolutePos(AT)) instanceof HangingSignBlockEntity,
                    set.name + " hanging sign has its block entity");
            helper.setBlock(AT, Blocks.AIR);
        }
        helper.succeed();
    }

    /** The boats are entities of their own that come back as their item. */
    public static void boatsFloat(GameTestHelper helper) {
        for (WoodSet set : CozyWoods.ALL) {
            Boat boat = helper.spawn(set.boat.get(), new Vec3(2.5, 1.0, 2.5));
            helper.assertTrue(boat.isAlive(), set.name + " boat exists");
            helper.assertTrue(boat.getPickResult().is(CozyItemsHolder.item(set.name + "_boat")), "it picks as its item");
            boat.discard();
            ChestBoat chest = helper.spawn(set.chestBoat.get(), new Vec3(2.5, 1.0, 2.5));
            helper.assertTrue(chest.getPickResult().is(CozyItemsHolder.item(set.name + "_chest_boat")), "the chest boat too");
            chest.discard();
        }
        helper.succeed();
    }

    /** Vanilla's axe only strips what its transformer lists; a click with an axe on our log strips it, keeping the axis. */
    public static void axeStrips(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.survival(helper, PLAYER);
        try {
            for (WoodSet set : CozyWoods.ALL) {
                for (var pair : List.of(List.of(set.log, set.strippedLog), List.of(set.wood, set.strippedWood))) {
                    helper.setBlock(AT, pair.get(0).get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X));
                    ItemStack axe = new ItemStack(Items.IRON_AXE);
                    player.setItemInHand(InteractionHand.MAIN_HAND, axe);
                    InteractionEvent.RIGHT_CLICK_BLOCK.invoker().click(player, InteractionHand.MAIN_HAND,
                            helper.absolutePos(AT), Direction.UP);
                    BlockState now = helper.getBlockState(AT);
                    helper.assertTrue(now.is(pair.get(1).get()), set.name + ": stripped");
                    helper.assertValueEqual(now.getValue(RotatedPillarBlock.AXIS), Direction.Axis.X, "the axis stays");
                    helper.assertValueEqual(axe.getDamageValue(), 1, "the axe wears");
                }
                helper.setBlock(AT, set.planks.get());
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_AXE));
                InteractionEvent.RIGHT_CLICK_BLOCK.invoker().click(player, InteractionHand.MAIN_HAND, helper.absolutePos(AT),
                        Direction.UP);
                helper.assertTrue(helper.getBlockState(AT).is(set.planks.get()), "planks are left as they are");
            }
            helper.succeed();
        } finally {
            TestPlayers.remove(helper, player);
        }
    }

    /**
     * A sapling grows into a tree: a trunk of its logs and leaves that can bear fruit. Like vanilla's, a growth now and
     * then finds no room and leaves the sapling be, so most of many tries must succeed.
     */
    public static void saplingsGrow(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (WoodSet set : CozyWoods.ALL) {
            BlockPos pos = helper.absolutePos(AT);
            level.setBlock(pos.below(), Blocks.DIRT.defaultBlockState(), Block.UPDATE_ALL);
            SaplingBlock block = (SaplingBlock) set.sapling.get();
            int grown = 0;
            for (int i = 0; i < GROWTH_TRIES; i++) {
                level.setBlock(pos, block.defaultBlockState().setValue(SaplingBlock.STAGE, 1), Block.UPDATE_ALL);
                block.advanceTree(level, pos, level.getBlockState(pos), level.getRandom());
                if (level.getBlockState(pos).is(set.log.get())) {
                    grown++;
                    if (grown == 1) {
                        checkTree(helper, set, pos);
                    }
                }
                clearAbove(level, pos);
            }
            helper.assertTrue(grown >= GROWTH_TRIES * 9 / 10, set.name + " saplings grew " + grown + " of " + GROWTH_TRIES);
        }
        helper.succeed();
    }

    private static void checkTree(GameTestHelper helper, WoodSet set, BlockPos pos) {
        ServerLevel level = helper.getLevel();
        int logs = 0;
        int leaves = 0;
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-TREE_SEARCH, 0, -TREE_SEARCH), pos.offset(TREE_SEARCH, TREE_SEARCH * 2, TREE_SEARCH))) {
            BlockState state = level.getBlockState(p);
            if (state.is(set.log.get())) {
                logs++;
            } else if (state.is(set.leaves.get())) {
                leaves++;
                helper.assertValueEqual(state.getValue(FruitLeavesBlock.AGE), 0, "new leaves are bare");
            }
        }
        helper.assertTrue(logs >= 4, set.name + " trunk: " + logs);
        helper.assertTrue(leaves >= 12, set.name + " crown: " + leaves);
    }

    private static void clearAbove(ServerLevel level, BlockPos pos) {
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-TREE_SEARCH, 0, -TREE_SEARCH), pos.offset(TREE_SEARCH, TREE_SEARCH * 2, TREE_SEARCH))) {
            level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    /** Leaves move on from bare to blossom to fruit; a click picks the fruit and they start over. */
    public static void fruitRipensAndIsPicked(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.survival(helper, PLAYER);
        try {
            ServerLevel level = helper.getLevel();
            for (WoodSet set : CozyWoods.ALL) {
                helper.setBlock(AT, set.leaves.get().defaultBlockState().setValue(net.minecraft.world.level.block.LeavesBlock.PERSISTENT, true));
                BlockPos abs = helper.absolutePos(AT);
                int tries = 0;
                while (helper.getBlockState(AT).getValue(FruitLeavesBlock.AGE) < FruitLeavesBlock.RIPE && tries++ < RIPENING_TRIES) {
                    helper.getBlockState(AT).randomTick(level, abs, level.getRandom());
                }
                helper.assertValueEqual(helper.getBlockState(AT).getValue(FruitLeavesBlock.AGE), FruitLeavesBlock.RIPE,
                        set.name + " leaves ripen in time");
                BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(abs), Direction.UP, abs, false);
                helper.getBlockState(AT).useWithoutItem(level, player, hit);
                helper.assertValueEqual(helper.getBlockState(AT).getValue(FruitLeavesBlock.AGE), 0, "picked, they start over");
                var fruit = level.getEntitiesOfClass(ItemEntity.class, new net.minecraft.world.phys.AABB(abs).inflate(2.0));
                helper.assertTrue(!fruit.isEmpty(), set.name + " fruit dropped");
                fruit.forEach(ItemEntity::discard);
                helper.assertValueEqual(helper.getBlockState(AT).useWithoutItem(level, player, hit),
                        net.minecraft.world.InteractionResult.PASS, "bare leaves give nothing");
            }
            helper.succeed();
        } finally {
            TestPlayers.remove(helper, player);
        }
    }

    /** Bone meal moves the fruit leaves a stage on, always, and stops at ripe. */
    public static void boneMealRipens(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (WoodSet set : CozyWoods.ALL) {
            FruitLeavesBlock block = (FruitLeavesBlock) set.leaves.get();
            helper.setBlock(AT, block.defaultBlockState().setValue(net.minecraft.world.level.block.LeavesBlock.PERSISTENT, true));
            BlockPos abs = helper.absolutePos(AT);
            for (int age = 0; age < FruitLeavesBlock.RIPE; age++) {
                BlockState now = helper.getBlockState(AT);
                helper.assertTrue(block.isValidBonemealTarget(level, abs, now), "stage " + age + " takes bone meal");
                helper.assertTrue(block.isBonemealSuccess(level, level.getRandom(), abs, now), "always works");
                block.performBonemeal(level, level.getRandom(), abs, now);
                helper.assertValueEqual(helper.getBlockState(AT).getValue(FruitLeavesBlock.AGE), age + 1, "one stage on");
            }
            helper.assertFalse(block.isValidBonemealTarget(level, abs, helper.getBlockState(AT)),
                    "ripe fruit takes no more");
        }
        helper.succeed();
    }

    /** The groves are added to the biomes of their tags, and their placed features load. */
    public static void grovesAreInTheirBiomes(GameTestHelper helper) {
        var registries = helper.getLevel().registryAccess();
        var biomes = registries.lookupOrThrow(Registries.BIOME);
        helper.assertTrue(growsIn(biomes.getOrThrow(Biomes.PLAINS).value(), "tangerine_grove") == CozyConfig.common().citrus().wildTrees(),
                "tangerine groves in the plains");
        helper.assertTrue(growsIn(biomes.getOrThrow(Biomes.SAVANNA).value(), "orange_grove") == CozyConfig.common().citrus().wildTrees(),
                "orange groves in the savanna");
        helper.assertFalse(growsIn(biomes.getOrThrow(Biomes.DESERT).value(), "tangerine_grove"), "none in the desert");
        for (String grove : List.of("tangerine_grove", "orange_grove")) {
            helper.assertTrue(registries.lookupOrThrow(Registries.PLACED_FEATURE)
                    .get(ResourceKey.create(Registries.PLACED_FEATURE, CozyChristmas.id(grove))).isPresent(), grove + " loads");
        }
        helper.succeed();
    }

    private static boolean growsIn(Biome biome, String grove) {
        ResourceKey<PlacedFeature> key = ResourceKey.create(Registries.PLACED_FEATURE, CozyChristmas.id(grove));
        return biome.getGenerationSettings().features().stream()
                .anyMatch(step -> step.stream().anyMatch(holder -> holder.is(key)));
    }

    private static List<ItemStack> repeat(ItemStack stack, int count) {
        List<ItemStack> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(stack.copy());
        }
        return list;
    }

    private static void assertCrafts(GameTestHelper helper, net.minecraft.world.item.Item item, int count, CraftingInput grid) {
        Optional<ItemStack> result = helper.getLevel().getServer().getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, grid, helper.getLevel())
                .map(recipe -> recipe.value().assemble(grid, helper.getLevel().registryAccess()));
        helper.assertTrue(result.isPresent() && result.get().is(item) && result.get().getCount() == count,
                "crafting " + item + " x" + count + ", got " + result);
    }

    /** Items of the sets by id. */
    private static final class CozyItemsHolder {
        static net.minecraft.world.item.Item item(String name) {
            return BuiltInRegistries.ITEM.get(CozyChristmas.id(name));
        }
    }
}
