package dev.eliasnvx.cozychristmas.gametest;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** Recipe inputs for tests. 1.20.1 has no {@code CraftingInput}: recipes read containers. Adapted from Femboy Mod. */
final class TestContainers {

    private TestContainers() {
    }

    /** A {@code width} x {@code height} crafting grid filled row by row with {@code items}. */
    static CraftingContainer crafting(int width, int height, List<ItemStack> items) {
        NonNullList<ItemStack> grid = NonNullList.withSize(width * height, ItemStack.EMPTY);
        for (int i = 0; i < items.size(); i++) {
            grid.set(i, items.get(i));
        }
        return new TransientCraftingContainer(new NoMenu(), width, height, grid);
    }

    /** Menu that owns the grid; the grid only notifies it when a slot changes. */
    private static final class NoMenu extends AbstractContainerMenu {

        private NoMenu() {
            super(null, -1);
        }

        @Override
        public ItemStack quickMoveStack(Player player, int index) {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean stillValid(Player player) {
            return false;
        }
    }
}
