package dev.eliasnvx.cozychristmas.cosmetic.menu;

import dev.eliasnvx.cozychristmas.compat.catgirl.CatgirlPanel;
import dev.eliasnvx.cozychristmas.cosmetic.CosmeticSlot;

/**
 * The cosmetic slots live in the player's own inventory: a panel attached to the right of the survival inventory and
 * of the creative inventory tab, one column of {@link #ROWS} per column in {@link CosmeticSlot} order (a fifth slot
 * starts a second column). Positions are relative to the screen's top-left, so the server menu knows them too.
 */
public final class CosmeticsLayout {

    public static final int SLOT = 18;
    public static final int ROWS = 4;
    /** Between the inventory and the panel. */
    public static final int GAP = 2;
    public static final int PADDING = 7;
    public static final int COLUMNS = (CosmeticSlot.values().length + ROWS - 1) / ROWS;
    public static final int WIDTH = PADDING * 2 + SLOT * COLUMNS;
    public static final int HEIGHT = PADDING * 2 + SLOT * ROWS;
    /** Vanilla survival inventory width; the creative screen passes its own. */
    public static final int SURVIVAL_WIDTH = 176;

    private CosmeticsLayout() {
    }

    /** Right of the inventory, or right of Catgirl &amp; Catboy Mod's panel when that mod is loaded. */
    public static int panelX(int imageWidth) {
        return panelX(imageWidth, CatgirlPanel.offset());
    }

    /** {@link #panelX(int)} with the room another mod's panel takes ({@code shift}) given. */
    public static int panelX(int imageWidth, int shift) {
        return imageWidth + GAP + shift;
    }

    /** Item position of cosmetic slot {@code index} (the slot frame is one pixel up-left). */
    public static int slotX(int imageWidth, int index) {
        return slotX(imageWidth, CatgirlPanel.offset(), index);
    }

    public static int slotX(int imageWidth, int shift, int index) {
        return panelX(imageWidth, shift) + PADDING + 1 + index / ROWS * SLOT;
    }

    public static int slotY(int index) {
        return PADDING + 1 + index % ROWS * SLOT;
    }
}
