package dev.eliasnvx.cozychristmas.cosmetic.menu;

/** Implemented by the player's {@code InventoryMenu} (mixin): where its cosmetic slots start. */
public interface InventoryMenuCosmetics {

    /** Menu index of the first cosmetic slot; the others follow in {@code CosmeticSlot} order. */
    int cozychristmas$cosmeticStart();
}
