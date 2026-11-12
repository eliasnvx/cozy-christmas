package dev.eliasnvx.cozychristmas.gametest;

import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.config.CozyConfig;
import dev.eliasnvx.cozychristmas.cosmetic.CosmeticSlot;
import dev.eliasnvx.cozychristmas.cosmetic.CosmeticsManager;
import dev.eliasnvx.cozychristmas.registry.CozyItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.trading.TradeSet;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

/**
 * Ear warmers, elf hat and antler headband on a real server, shared by both loaders (Fabric: {@code fabric/src/gametest},
 * NeoForge: {@code neoforge/.../dev/CozyGameTestsNeoForge}). Dev only: not in the released jars.
 */
public final class HeadPieceGameTests {

    public static final List<GameTestEntry> ALL = List.of(
            new GameTestEntry("head_pieces_craft_and_trade", HeadPieceGameTests::headPiecesCraftAndTrade),
            new GameTestEntry("ear_warmers_complete_the_toasty_set", HeadPieceGameTests::earWarmersCompleteTheToastySet));

    private static final Vec3 TEST_AREA_CENTER = new Vec3(1.5, 1.0, 1.5);

    private HeadPieceGameTests() {
    }

    /** Each piece is worn on the head, comes out of its crafting grid, and Santa may stock it; two of them dye. */
    public static void headPiecesCraftAndTrade(GameTestHelper helper) {
        ItemStack wool = new ItemStack(Items.WOOL.white());
        ItemStack string = new ItemStack(Items.STRING);
        ItemStack green = new ItemStack(Items.WOOL.green());
        ItemStack red = new ItemStack(Items.WOOL.red());
        ItemStack bone = new ItemStack(Items.BONE);
        ItemStack none = ItemStack.EMPTY;
        assertCrafts(helper, CozyItems.EAR_WARMERS.get(), CraftingInput.of(3, 2,
                List.of(string, string, string, wool, none, wool)));
        assertCrafts(helper, CozyItems.ELF_HAT.get(), CraftingInput.of(3, 3,
                List.of(none, none, new ItemStack(Items.GOLD_NUGGET), none, green, none, green, green, green)));
        assertCrafts(helper, CozyItems.ANTLER_HEADBAND.get(), CraftingInput.of(3, 2,
                List.of(bone, none, bone, red, red, red)));

        for (Item dyeable : List.of(CozyItems.EAR_WARMERS.get(), CozyItems.ELF_HAT.get())) {
            ItemStack dyed = craft(helper, CraftingInput.of(2, 1, List.of(new ItemStack(dyeable), new ItemStack(Items.DYE.blue()))));
            DyedItemColor color = dyed.get(DataComponents.DYED_COLOR);
            // both carry alpha, which crafting and the dye's color don't agree on
            helper.assertTrue(dyed.is(dyeable) && color != null
                            && (color.rgb() & 0xFFFFFF) == (DyeColor.BLUE.getTextureDiffuseColor() & 0xFFFFFF),
                    "blue " + dyeable + ", got " + dyed + " " + color);
        }

        TradeSet goods = helper.getLevel().registryAccess().lookupOrThrow(Registries.TRADE_SET)
                .getOrThrow(ResourceKey.create(Registries.TRADE_SET, CozyChristmas.id("santa/goods"))).value();
        for (String piece : List.of("ear_warmers", "elf_hat", "antler_headband")) {
            helper.assertTrue(goods.trades().stream().anyMatch(trade -> trade.is(CozyChristmas.id("santa/goods/" + piece))),
                    "Santa's goods offer " + piece);
        }
        helper.succeed();
    }

    /** Ear warmers count as the Toasty set's head piece, like the pompom hat. */
    public static void earWarmersCompleteTheToastySet(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.survival(helper, TEST_AREA_CENTER);
        try {
            CosmeticsManager.set(player, CosmeticSlot.NECK, CozyItems.SCARF.get().getDefaultInstance());
            CosmeticsManager.set(player, CosmeticSlot.BODY, CozyItems.SNOWFLAKE_SWEATER.get().getDefaultInstance());
            CosmeticsManager.set(player, CosmeticSlot.HANDS, CozyItems.MITTENS.get().getDefaultInstance());
            helper.assertTrue(player.canFreeze(), "three pieces are not the set");
            CosmeticsManager.set(player, CosmeticSlot.HEAD, CozyItems.dyed(CozyItems.EAR_WARMERS.get(), DyeColor.PINK));
            helper.assertValueEqual(player.canFreeze(), !CozyConfig.common().toastySet().keepsWarm(),
                    "freezing with ear warmers instead of the hat");
            CosmeticsManager.set(player, CosmeticSlot.HEAD, CozyItems.ELF_HAT.get().getDefaultInstance());
            helper.assertTrue(player.canFreeze(), "an elf hat is felt, not part of the set");
            helper.succeed();
        } finally {
            TestPlayers.remove(helper, player);
        }
    }

    private static void assertCrafts(GameTestHelper helper, Item piece, CraftingInput grid) {
        ItemStack result = craft(helper, grid);
        helper.assertTrue(result.is(piece), "crafting " + piece + ", got " + result);
        helper.assertValueEqual(CosmeticsManager.slotOf(result), CosmeticSlot.HEAD, "slot of " + piece);
    }

    private static ItemStack craft(GameTestHelper helper, CraftingInput grid) {
        Optional<ItemStack> result = helper.getLevel().getServer().getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, grid, helper.getLevel())
                .map(recipe -> recipe.value().assemble(grid));
        return result.orElse(ItemStack.EMPTY);
    }
}
