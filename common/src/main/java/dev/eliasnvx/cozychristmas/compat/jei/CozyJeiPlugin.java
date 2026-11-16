package dev.eliasnvx.cozychristmas.compat.jei;

import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.client.cosmetic.CosmeticsPanel;
import dev.eliasnvx.cozychristmas.compat.RecipeViewerPages;
import dev.eliasnvx.cozychristmas.registry.CozyBlocks;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * JEI (SPEC compat): information pages for what comes from mechanics rather than recipes, and the wrapping table's
 * uses as a category. Loaded only by JEI: the {@code jei_mod_plugin} entrypoint on Fabric, the annotation on NeoForge.
 */
@JeiPlugin
public final class CozyJeiPlugin implements IModPlugin {

    static final RecipeType<RecipeViewerPages.Wrapping> WRAPPING =
            new RecipeType<>(RecipeViewerPages.wrappingId(), RecipeViewerPages.Wrapping.class);

    @Override
    public ResourceLocation getPluginUid() {
        return CozyChristmas.id("jei");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new WrappingCategory(
                registration.getJeiHelpers().getGuiHelper().createDrawableItemLike(CozyBlocks.WRAPPING_TABLE_ITEM.get())));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(WRAPPING, RecipeViewerPages.wrappings());
        for (RecipeViewerPages.Page page : RecipeViewerPages.pages()) {
            registration.addItemStackInfo(page.items(), page.text());
        }
    }

    /** The cosmetics panel sits right of the inventory, where JEI puts its item list: JEI keeps clear of it. */
    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGuiContainerHandler(InventoryScreen.class, new PanelArea<>());
        registration.addGuiContainerHandler(CreativeModeInventoryScreen.class, new PanelArea<>());
    }

    private static final class PanelArea<T extends AbstractContainerScreen<?>> implements IGuiContainerHandler<T> {
        @Override
        public List<Rect2i> getGuiExtraAreas(T screen) {
            Rect2i area = CosmeticsPanel.area(screen);
            return area == null ? List.of() : List.of(area);
        }
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(CozyBlocks.WRAPPING_TABLE_ITEM.get(), WRAPPING);
    }

    /** Gift over paper, an arrow, the present: the table's own layout. */
    private static final class WrappingCategory implements IRecipeCategory<RecipeViewerPages.Wrapping> {

        private static final int WIDTH = 82;
        private static final int HEIGHT = 40;
        private static final int ARROW_X = 26;
        private static final int ARROW_Y = 12;
        private static final int RESULT_X = 60;
        private static final int RESULT_Y = 12;
        private static final int PAPER_Y = 22;

        private final IDrawable icon;

        WrappingCategory(IDrawable icon) {
            this.icon = icon;
        }

        @Override
        public RecipeType<RecipeViewerPages.Wrapping> getRecipeType() {
            return WRAPPING;
        }

        @Override
        public Component getTitle() {
            return RecipeViewerPages.title();
        }

        @Override
        public int getWidth() {
            return WIDTH;
        }

        @Override
        public int getHeight() {
            return HEIGHT;
        }

        @Override
        public IDrawable getIcon() {
            return icon;
        }

        @Override
        public void setRecipe(IRecipeLayoutBuilder builder, RecipeViewerPages.Wrapping recipe, IFocusGroup focuses) {
            builder.addInputSlot(1, 1).setStandardSlotBackground().addItemStacks(recipe.gifts());
            builder.addInputSlot(1, PAPER_Y + 1).setStandardSlotBackground().addItemStacks(recipe.paper());
            builder.addOutputSlot(RESULT_X + 1, RESULT_Y + 1).setOutputSlotBackground().addItemStacks(recipe.presents());
        }

        @Override
        public void createRecipeExtras(IRecipeExtrasBuilder builder, RecipeViewerPages.Wrapping recipe, IFocusGroup focuses) {
            builder.addRecipeArrowWidget().setPosition(ARROW_X, ARROW_Y);
        }

        @Override
        public ResourceLocation getRegistryName(RecipeViewerPages.Wrapping recipe) {
            return CozyChristmas.id("wrapping/" + recipe.key());
        }
    }
}
