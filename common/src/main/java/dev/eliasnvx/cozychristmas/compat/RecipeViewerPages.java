package dev.eliasnvx.cozychristmas.compat;

import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.present.PresentDesign;
import dev.eliasnvx.cozychristmas.registry.CozyBlocks;
import dev.eliasnvx.cozychristmas.registry.CozyItems;
import dev.eliasnvx.cozychristmas.registry.CozyTags;
import dev.eliasnvx.cozychristmas.tree.OrnamentColor;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * What JEI and REI show beyond crafting recipes (those they read themselves): information pages for things that
 * come from mechanics, not recipes, and the wrapping table's two uses. No viewer types here, so both plugins share it.
 * Built on the client once tags are loaded.
 */
public final class RecipeViewerPages {

    private RecipeViewerPages() {
    }

    /** An information page: these items, this text ({@code info.cozychristmas.<key>}). */
    public record Page(List<ItemStack> items, Component text) {
    }

    /** The wrapping table: one of {@code gifts} and one of {@code paper} make one of {@code presents}. */
    public record Wrapping(String key, List<ItemStack> gifts, List<ItemStack> paper, List<ItemStack> presents) {

        public Component title() {
            return Component.translatable("recipe.cozychristmas.wrapping." + key);
        }
    }

    public static List<Page> pages() {
        List<Page> pages = new ArrayList<>();
        pages.add(page("present", presents()));
        pages.add(page("wrapping_table", stacks(CozyBlocks.WRAPPING_TABLE_ITEM.get())));
        pages.add(page("stocking", stacks(CozyBlocks.STOCKING_ITEM.get())));
        pages.add(page("fireplace", stacks(CozyBlocks.FIREPLACE_ITEM.get())));
        pages.add(page("tree_star", stacks(CozyBlocks.TREE_STAR_ITEM.get())));
        pages.add(page("ornament", Arrays.stream(OrnamentColor.values())
                .map(color -> new ItemStack(CozyBlocks.ORNAMENT_ITEMS.get(color).get())).toList()));
        pages.add(page("toasty_set", tagged(CozyTags.TOASTY_SET)));
        pages.add(page("chestnuts", stacks(CozyItems.CHESTNUTS.get())));
        pages.add(page("hot_cocoa", stacks(CozyItems.HOT_COCOA.get())));
        pages.add(page("gingerbread_cookie", stacks(CozyItems.GINGERBREAD_COOKIE.get())));
        pages.add(page("ice_snowball", stacks(CozyItems.ICE_SNOWBALL.get())));
        pages.add(page("krampus_trophies", stacks(CozyItems.KRAMPUS_HORNS.get(), CozyItems.KRAMPUS_CHAIN.get())));
        pages.removeIf(page -> page.items().isEmpty()); // a data pack may empty a tag: the viewers want items
        return pages;
    }

    public static List<Wrapping> wrappings() {
        List<ItemStack> paper = tagged(CozyTags.WRAPPING_PAPER);
        List<ItemStack> gifts = stacks(Items.DIAMOND, CozyItems.GINGERBREAD_COOKIE.get(), CozyItems.POMPOM_HAT.get(),
                Items.GOLDEN_APPLE, CozyItems.TANGERINE.get(), Items.MUSIC_DISC_CAT);
        List<Wrapping> wrappings = new ArrayList<>(List.of(
                new Wrapping("gift", gifts, paper, presents()),
                new Wrapping("prank", tagged(CozyTags.WRAPS_AS_BOOM), paper, presents())));
        wrappings.removeIf(wrapping -> wrapping.gifts().isEmpty() || wrapping.paper().isEmpty());
        return wrappings;
    }

    public static Component title() {
        return Component.translatable("recipe.cozychristmas.wrapping");
    }

    public static Identifier wrappingId() {
        return CozyChristmas.id("wrapping");
    }

    private static Page page(String key, List<ItemStack> items) {
        return new Page(items, Component.translatable("info.cozychristmas." + key));
    }

    private static List<ItemStack> presents() {
        return Arrays.stream(PresentDesign.values()).map(CozyItems::present).toList();
    }

    private static List<ItemStack> stacks(ItemLike... items) {
        return Arrays.stream(items).map(ItemStack::new).toList();
    }

    private static List<ItemStack> tagged(TagKey<Item> tag) {
        List<ItemStack> stacks = new ArrayList<>();
        for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(tag)) {
            stacks.add(new ItemStack(holder));
        }
        return stacks;
    }
}
