package dev.eliasnvx.cozychristmas.client.cosmetic;

import dev.eliasnvx.cozychristmas.client.CozyGui;
import dev.eliasnvx.cozychristmas.cosmetic.CosmeticSlot;
import dev.eliasnvx.cozychristmas.cosmetic.menu.CosmeticMenuSlot;
import dev.eliasnvx.cozychristmas.cosmetic.menu.CosmeticsContainer;
import dev.eliasnvx.cozychristmas.cosmetic.menu.CosmeticsLayout;
import dev.eliasnvx.cozychristmas.mixin.client.AbstractContainerScreenAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * The cosmetic slots' panel on the right of the survival inventory and the creative inventory tab
 * ({@link CosmeticsLayout}): background, slot frames and the slot's name on empty slots. The slots themselves belong
 * to the inventory menu.
 */
public final class CosmeticsPanel {

    private CosmeticsPanel() {
    }

    public static boolean visible(AbstractContainerScreen<?> screen) {
        return screen instanceof InventoryScreen
                || screen instanceof CreativeModeInventoryScreen creative && creative.isInventoryOpen();
    }

    private static int left(AbstractContainerScreen<?> screen) {
        AbstractContainerScreenAccessor access = (AbstractContainerScreenAccessor) screen;
        return access.cozychristmas$leftPos() + CosmeticsLayout.panelX(access.cozychristmas$imageWidth());
    }

    private static int top(AbstractContainerScreen<?> screen) {
        return ((AbstractContainerScreenAccessor) screen).cozychristmas$topPos();
    }

    /** The panel's screen area, or null while it isn't shown (for item-list mods to keep out of it). */
    public static @Nullable Rect2i area(AbstractContainerScreen<?> screen) {
        return visible(screen) ? new Rect2i(left(screen), top(screen), CosmeticsLayout.WIDTH, CosmeticsLayout.HEIGHT) : null;
    }

    /** Clicks on the panel are not "outside the window" (they would drop the carried stack). */
    public static boolean contains(AbstractContainerScreen<?> screen, double mouseX, double mouseY) {
        if (!visible(screen)) {
            return false;
        }
        int x = left(screen);
        int y = top(screen);
        return mouseX >= x && mouseX < x + CosmeticsLayout.WIDTH && mouseY >= y && mouseY < y + CosmeticsLayout.HEIGHT;
    }

    public static void extractBackground(AbstractContainerScreen<?> screen, GuiGraphicsExtractor g) {
        if (!visible(screen)) {
            return;
        }
        int x = left(screen);
        int y = top(screen);
        CozyGui.panel(g, x, y, CosmeticsLayout.WIDTH, CosmeticsLayout.HEIGHT);
        int slotLeft = x - CosmeticsLayout.panelX(0);
        for (int i = 0; i < CosmeticSlot.values().length; i++) {
            CozyGui.slotFrame(g, slotLeft + CosmeticsLayout.slotX(0, i) - 1, y + CosmeticsLayout.slotY(i) - 1);
        }
    }

    /** The name of a hovered empty cosmetic slot; true when it was shown. */
    public static boolean tooltip(AbstractContainerScreen<?> screen, GuiGraphicsExtractor g, @Nullable Slot hovered,
                                  ItemStack carried, int mouseX, int mouseY) {
        if (!visible(screen) || !carried.isEmpty() || hovered == null || hovered.hasItem()
                || !(hovered.container instanceof CosmeticsContainer)) {
            return false;
        }
        CosmeticSlot slot = CosmeticMenuSlot.slotOfIcon(hovered.getNoItemIcon());
        if (slot == null) {
            return false;
        }
        g.setTooltipForNextFrame(Minecraft.getInstance().font, Component.translatable(slot.translationKey()), mouseX, mouseY);
        return true;
    }
}
