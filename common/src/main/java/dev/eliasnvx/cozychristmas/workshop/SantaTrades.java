package dev.eliasnvx.cozychristmas.workshop;

import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.registry.CozyBlocks;
import dev.eliasnvx.cozychristmas.registry.CozyItems;
import dev.eliasnvx.cozychristmas.tree.OrnamentColor;
import dev.eliasnvx.cozychristmas.wood.CozyWoods;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Santa's trades. 26.3 reads them from data ({@code villager_trade/santa/**}, picked by trade sets
 * {@code cozychristmas:santa/goods} and {@code santa/buying}); 1.20.1 has no data-driven villager trades, so the same
 * trades live here: same items, counts, prices, max uses and xp, no reputation discount. A cost without NBT takes the
 * item whatever its tag, like 26.3's {@code ItemCost} without components. Each set offers {@code amount}
 * different trades, drawn from the world's random sequence of the same name, like 26.3's trade sets.
 */
public final class SantaTrades {

    /** 26.3's {@code reputation_discount}: Santa ignores gossip, the nice discount is his own. */
    private static final float PRICE_MULTIPLIER = 0.0F;

    /** Six of his goods for emeralds. */
    public static final TradeSet GOODS = new TradeSet(CozyChristmas.id("trade_set/santa/goods"), 6, List.of(
            sells("present", 3, CozyBlocks.PRESENT_ITEM, 1, 4, 5),
            sells("saddle", 6, () -> Items.SADDLE, 1, 2, 10),
            sells("golden_carrots", 3, () -> Items.GOLDEN_CARROT, 3, 6, 5),
            sells("cookies", 1, () -> Items.COOKIE, 8, 8, 2),
            sells("pompom_hat", 4, CozyItems.POMPOM_HAT, 1, 3, 5),
            sells("scarf", 4, CozyItems.SCARF, 1, 3, 5),
            sells("mittens", 3, CozyItems.MITTENS, 1, 3, 5),
            sells("ear_warmers", 4, CozyItems.EAR_WARMERS, 1, 3, 5),
            sells("elf_hat", 4, CozyItems.ELF_HAT, 1, 3, 5),
            sells("antler_headband", 5, CozyItems.ANTLER_HEADBAND, 1, 3, 5),
            sells("snowflake_sweater", 7, CozyItems.SNOWFLAKE_SWEATER, 1, 2, 10),
            sells("reindeer_sweater", 7, CozyItems.REINDEER_SWEATER, 1, 2, 10),
            sells("christmas_tree_sweater", 7, CozyItems.CHRISTMAS_TREE_SWEATER, 1, 2, 10),
            sells("name_tag", 5, () -> Items.NAME_TAG, 1, 2, 10),
            sells("spruce_saplings", 1, () -> Items.SPRUCE_SAPLING, 4, 6, 2),
            sells("tangerines", 1, CozyItems.TANGERINE, 6, 8, 2),
            sells("oranges", 1, CozyItems.ORANGE, 6, 8, 2),
            sells("tangerine_saplings", 3, () -> CozyWoods.TANGERINE.sapling.get(), 1, 4, 2),
            sells("orange_saplings", 3, () -> CozyWoods.ORANGE.sapling.get(), 1, 4, 2),
            sells("chestnuts", 1, CozyItems.CHESTNUTS, 10, 8, 2),
            sells("hot_cocoa", 2, CozyItems.HOT_COCOA, 2, 6, 2),
            sells("tree_star", 4, CozyBlocks.TREE_STAR_ITEM, 1, 4, 2),
            sells("red_ornaments", 1, () -> CozyBlocks.ORNAMENT_ITEMS.get(OrnamentColor.RED).get(), 4, 8, 2),
            sells("gold_ornaments", 1, () -> CozyBlocks.ORNAMENT_ITEMS.get(OrnamentColor.GOLD).get(), 4, 8, 2)
    ));

    /** One thing he buys for an emerald. */
    public static final TradeSet BUYING = new TradeSet(CozyChristmas.id("trade_set/santa/buying"), 1, List.of(
            buys("coal", () -> Items.COAL, 16, 12, 2),
            buys("carrots", () -> Items.CARROT, 16, 12, 2)
    ));

    private SantaTrades() {
    }

    /**
     * One trade, as 26.3's {@code villager_trade/santa/<set>/<name>} file: Santa {@code wants} something and
     * {@code gives} something, {@code maxUses} times until the next restock, for {@code xp}.
     */
    public record Trade(String name, Supplier<? extends ItemLike> wants, int wantsCount,
                        Supplier<? extends ItemLike> gives, int givesCount, int maxUses, int xp) {

        public MerchantOffer offer() {
            return new MerchantOffer(new ItemStack(wants.get(), wantsCount), new ItemStack(gives.get(), givesCount),
                    maxUses, xp, PRICE_MULTIPLIER);
        }
    }

    /** {@code amount} different trades out of {@code trades}, drawn from {@code randomSequence}. */
    public record TradeSet(ResourceLocation randomSequence, int amount, List<Trade> trades) {

        /** Whether the set has a trade named {@code name} (the file name on 26.3). */
        public boolean has(String name) {
            return trades.stream().anyMatch(trade -> trade.name().equals(name));
        }

        /** Adds up to {@code amount} different trades of the set, at random, to {@code offers}. */
        public void addOffers(ServerLevel level, MerchantOffers offers) {
            RandomSource random = level.getRandomSequence(randomSequence);
            List<Trade> left = new ArrayList<>(trades);
            for (int added = 0; added < amount && !left.isEmpty(); added++) {
                offers.add(left.remove(random.nextInt(left.size())).offer());
            }
        }
    }

    private static Trade sells(String name, int emeralds, Supplier<? extends ItemLike> item, int count, int maxUses, int xp) {
        return new Trade(name, () -> Items.EMERALD, emeralds, item, count, maxUses, xp);
    }

    private static Trade buys(String name, Supplier<? extends ItemLike> item, int count, int maxUses, int xp) {
        return new Trade(name, item, count, () -> Items.EMERALD, 1, maxUses, xp);
    }
}
