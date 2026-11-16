package dev.eliasnvx.cozychristmas.compat.rei;

import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.compat.RecipeViewerPages;
import dev.eliasnvx.cozychristmas.registry.CozyBlocks;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.plugin.client.BuiltinClientPlugin;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Optional;

/**
 * REI (SPEC compat): the same information pages and wrapping category as {@code CozyJeiPlugin}. Loaded only by REI:
 * the {@code rei_client} entrypoint on Fabric, the annotated subclass in the NeoForge module (the annotation ships
 * only in REI's NeoForge jar).
 */
public class CozyReiPlugin implements REIClientPlugin {

    static final CategoryIdentifier<WrappingDisplay> WRAPPING = CategoryIdentifier.of(RecipeViewerPages.wrappingId());

    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new WrappingCategory());
        registry.addWorkstations(WRAPPING, EntryStacks.of(CozyBlocks.WRAPPING_TABLE_ITEM.get()));
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        for (RecipeViewerPages.Wrapping wrapping : RecipeViewerPages.wrappings()) {
            registry.add(new WrappingDisplay(wrapping));
        }
        for (RecipeViewerPages.Page page : RecipeViewerPages.pages()) {
            BuiltinClientPlugin.getInstance().registerInformation(EntryIngredients.ofItemStacks(page.items()),
                    page.items().getFirst().getHoverName(), lines -> {
                        lines.add(page.text());
                        return lines;
                    });
        }
    }

    /** One use of the wrapping table: gifts and paper in, presents out. Client only: never synced, no serializer. */
    static final class WrappingDisplay extends BasicDisplay {

        WrappingDisplay(RecipeViewerPages.Wrapping wrapping) {
            super(List.of(EntryIngredients.ofItemStacks(wrapping.gifts()), EntryIngredients.ofItemStacks(wrapping.paper())),
                    List.of(EntryIngredients.ofItemStacks(wrapping.presents())),
                    Optional.of(CozyChristmas.id("wrapping/" + wrapping.key())));
        }

        @Override
        public CategoryIdentifier<?> getCategoryIdentifier() {
            return WRAPPING;
        }

        @Override
        public DisplaySerializer<? extends WrappingDisplay> getSerializer() {
            return null;
        }
    }

    /** Gift over paper, an arrow, the present: the table's own layout. */
    static final class WrappingCategory implements DisplayCategory<WrappingDisplay> {

        private static final int HEIGHT = 50;
        private static final int SLOT = 18;

        @Override
        public CategoryIdentifier<? extends WrappingDisplay> getCategoryIdentifier() {
            return WRAPPING;
        }

        @Override
        public Component getTitle() {
            return RecipeViewerPages.title();
        }

        @Override
        public Renderer getIcon() {
            return EntryStacks.of(CozyBlocks.WRAPPING_TABLE_ITEM.get());
        }

        @Override
        public int getDisplayHeight() {
            return HEIGHT;
        }

        @Override
        public List<Widget> setupDisplay(WrappingDisplay display, Rectangle bounds) {
            Point center = new Point(bounds.getCenterX(), bounds.getCenterY());
            int left = center.x - 41;
            return List.of(
                    Widgets.createRecipeBase(bounds),
                    Widgets.createSlot(new Point(left, center.y - SLOT)).entries(display.getInputEntries().get(0)).markInput(),
                    Widgets.createSlot(new Point(left, center.y + 2)).entries(display.getInputEntries().get(1)).markInput(),
                    Widgets.createArrow(new Point(left + 26, center.y - 8)),
                    Widgets.createResultSlotBackground(new Point(left + 60, center.y - 8)),
                    Widgets.createSlot(new Point(left + 60, center.y - 8)).entries(display.getOutputEntries().getFirst())
                            .disableBackground().markOutput());
        }
    }
}
