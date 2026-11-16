package dev.eliasnvx.cozychristmas.gametest;

import dev.eliasnvx.cozychristmas.config.CozyConfig;
import dev.eliasnvx.cozychristmas.food.Tangerine;
import dev.eliasnvx.cozychristmas.food.TangerineItem;
import dev.eliasnvx.cozychristmas.registry.CozyEffects;
import dev.eliasnvx.cozychristmas.registry.CozyItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * The SPEC food on a real server, shared by both loaders (Fabric: {@code fabric/src/gametest}, NeoForge:
 * {@code neoforge/.../dev/CozyGameTestsNeoForge}). Pigs drink and eat: the consume effects work on any living thing.
 * Dev only: not in the released jars.
 */
public final class FoodGameTests {

    public static final List<GameTestEntry> ALL = List.of(
            new GameTestEntry("hot_cocoa_thaws_and_keeps_warm", FoodGameTests::cocoaThawsAndKeepsWarm),
            new GameTestEntry("mulled_wine_regenerates", FoodGameTests::mulledWineRegenerates),
            new GameTestEntry("a_tangerine_at_midnight_rings_in_the_new_year", FoodGameTests::tangerineRingsIn),
            new GameTestEntry("dark_oak_leaves_drop_chestnuts", FoodGameTests::leavesDropChestnuts),
            new GameTestEntry("chestnuts_roast_on_a_campfire", FoodGameTests::chestnutsRoastOnCampfire));

    private static final BlockPos WARM_SNOW = new BlockPos(1, 1, 1);
    private static final BlockPos COLD_SNOW = new BlockPos(4, 1, 1);
    private static final Vec3 PIG = new Vec3(1.5, 1.0, 4.5);
    private static final BlockPos PIG_BLOCK = BlockPos.containing(PIG);
    private static final int FROZEN = 100;
    private static final int LEAF_BREAKS = 3000;
    private static final int RING_AGAIN_AFTER = 5;

    private FoodGameTests() {
    }

    /** Two pigs in powder snow, both half frozen: the one given cocoa thaws and stays warm, the other freezes on. */
    public static void cocoaThawsAndKeepsWarm(GameTestHelper helper) {
        helper.setBlock(WARM_SNOW, Blocks.POWDER_SNOW);
        helper.setBlock(COLD_SNOW, Blocks.POWDER_SNOW);
        Pig warm = pig(helper, Vec3.atBottomCenterOf(WARM_SNOW));
        Pig cold = pig(helper, Vec3.atBottomCenterOf(COLD_SNOW));
        warm.setTicksFrozen(FROZEN);
        cold.setTicksFrozen(FROZEN);
        ItemStack left = new ItemStack(CozyItems.HOT_COCOA.get()).finishUsingItem(helper.getLevel(), warm);
        helper.assertTrue(left.is(CozyItems.MUG.get()), "the mug is left, not " + left);
        helper.succeedWhen(() -> {
            helper.assertTrue(warm.hasEffect(CozyEffects.WARM.asHolder()), "the cocoa warms");
            helper.assertValueEqual(warm.getTicksFrozen(), 0, "the warm pig's freezing");
            helper.assertFalse(warm.canFreeze(), "the warm pig can't freeze");
            helper.assertTrue(cold.getTicksFrozen() > FROZEN, "the other pig freezes on");
        });
    }

    public static void mulledWineRegenerates(GameTestHelper helper) {
        Pig pig = pig(helper, PIG);
        ItemStack left = new ItemStack(CozyItems.MULLED_WINE.get()).finishUsingItem(helper.getLevel(), pig);
        helper.assertTrue(left.is(Items.GLASS_BOTTLE), "the bottle is left, not " + left);
        MobEffectInstance regeneration = pig.getEffect(MobEffects.REGENERATION);
        helper.assertTrue(regeneration != null, "mulled wine regenerates");
        helper.assertValueEqual(regeneration.getAmplifier(), 0, "Regeneration level (I)");
        helper.assertValueEqual(regeneration.getDuration(), 200, "Regeneration ticks (10 s)");
        helper.succeed();
    }

    /**
     * The clock can't be set to midnight under the other tests, so this rings the bell directly: hidden, twelve
     * strokes long, and a second tangerine doesn't start it over.
     */
    public static void tangerineRingsIn(GameTestHelper helper) {
        Pig pig = pig(helper, PIG);
        TangerineItem.ringIn(pig);
        MobEffectInstance chimes = pig.getEffect(CozyEffects.CHIMES.asHolder());
        helper.assertTrue(chimes != null, "the bell strikes");
        helper.assertFalse(chimes.isVisible() || chimes.showIcon(), "no particles, no icon: no explanation");
        helper.assertValueEqual(chimes.getDuration(), Tangerine.CHIMES_DURATION, "twelve strokes long");
        helper.runAfterDelay(RING_AGAIN_AFTER, () -> {
            TangerineItem.ringIn(pig);
            MobEffectInstance again = pig.getEffect(CozyEffects.CHIMES.asHolder());
            helper.assertTrue(again != null && again.getDuration() < Tangerine.CHIMES_DURATION, "still the first twelve");
            helper.succeed();
        });
    }

    /** Broken many times over: dark oak leaves sometimes give chestnuts, oak leaves never. */
    public static void leavesDropChestnuts(GameTestHelper helper) {
        if (CozyConfig.common().food().chestnutChance() <= 0.0F) {
            helper.succeed();
            return;
        }
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(PIG_BLOCK);
        helper.assertTrue(chestnuts(level, pos, Blocks.DARK_OAK_LEAVES.defaultBlockState()) > 0, "dark oak leaves drop chestnuts");
        helper.assertValueEqual(chestnuts(level, pos, Blocks.OAK_LEAVES.defaultBlockState()), 0, "chestnuts from oak leaves");
        helper.succeed();
    }

    /** SPEC: roasted on a campfire. Only there: not in a furnace. */
    public static void chestnutsRoastOnCampfire(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        RecipeManager recipes = level.getServer().getRecipeManager();
        SingleRecipeInput input = new SingleRecipeInput(new ItemStack(CozyItems.CHESTNUTS.get()));
        var roast = recipes.getRecipeFor(RecipeType.CAMPFIRE_COOKING, input, level);
        helper.assertTrue(roast.isPresent(), "a campfire recipe for chestnuts");
        helper.assertTrue(roast.get().value().assemble(input).is(CozyItems.ROASTED_CHESTNUTS.get()), "it roasts them");
        helper.assertFalse(recipes.getRecipeFor(RecipeType.SMELTING, input, level).isPresent(), "no furnace recipe");
        helper.succeed();
    }

    private static int chestnuts(ServerLevel level, BlockPos pos, BlockState leaves) {
        int count = 0;
        for (int i = 0; i < LEAF_BREAKS; i++) {
            for (ItemStack drop : Block.getDrops(leaves, level, pos, null)) {
                if (drop.is(CozyItems.CHESTNUTS.get())) {
                    count += drop.getCount();
                }
            }
        }
        return count;
    }

    private static Pig pig(GameTestHelper helper, Vec3 at) {
        Pig pig = helper.spawn(EntityTypes.PIG, at);
        pig.setNoAi(true);
        return pig;
    }
}
