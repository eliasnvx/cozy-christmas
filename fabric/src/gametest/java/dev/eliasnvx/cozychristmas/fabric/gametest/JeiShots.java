package dev.eliasnvx.cozychristmas.fabric.gametest;

import dev.eliasnvx.cozychristmas.compat.RecipeViewerPages;
import dev.eliasnvx.cozychristmas.registry.CozyItems;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** Opens JEI's views for {@link CompatLookClientTest}; loaded only when JEI is (REI has {@link ReiShots}). */
final class JeiShots {

    private JeiShots() {
    }

    static void wrapping() {
        jei().getRecipesGui().showTypes(List.of(IRecipeType.create(RecipeViewerPages.wrappingId(), RecipeViewerPages.Wrapping.class)));
    }

    static void chestnuts() {
        IJeiRuntime runtime = jei();
        runtime.getRecipesGui().show(runtime.getJeiHelpers().getFocusFactory()
                .createFocus(RecipeIngredientRole.OUTPUT, VanillaTypes.ITEM_STACK, new ItemStack(CozyItems.CHESTNUTS.get())));
    }

    private static IJeiRuntime jei() {
        IJeiRuntime runtime = TestJeiPlugin.runtime;
        if (runtime == null) {
            throw new AssertionError("JEI's runtime isn't available");
        }
        return runtime;
    }
}
