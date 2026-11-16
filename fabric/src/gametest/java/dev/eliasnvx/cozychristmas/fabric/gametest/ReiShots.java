package dev.eliasnvx.cozychristmas.fabric.gametest;

import dev.eliasnvx.cozychristmas.compat.RecipeViewerPages;
import dev.eliasnvx.cozychristmas.registry.CozyBlocks;
import me.shedaniel.rei.api.client.view.ViewSearchBuilder;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;

/** Opens REI's views for {@link CompatLookClientTest}; loaded only when REI is (JEI has {@link JeiShots}). */
final class ReiShots {

    private ReiShots() {
    }

    static void wrapping() {
        ViewSearchBuilder.builder().addCategory(CategoryIdentifier.of(RecipeViewerPages.wrappingId())).open();
    }

    static void treeStar() {
        ViewSearchBuilder.builder().addUsagesFor(EntryStacks.of(CozyBlocks.TREE_STAR_ITEM.get())).open();
    }
}
