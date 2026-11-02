package dev.eliasnvx.cozychristmas.cosmetic.menu;

import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.cosmetic.CosmeticSlot;
import dev.eliasnvx.cozychristmas.cosmetic.CosmeticsManager;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/** A cosmetic slot in the player's inventory menu: takes one item of its {@link CosmeticSlot}. */
public final class CosmeticMenuSlot extends Slot {

    private static final CosmeticSlot[] SLOTS = CosmeticSlot.values();
    /** Outline icons of empty slots, like vanilla's armor slots: {@code textures/gui/sprites/container/slot/<slot>.png}. */
    private static final Identifier[] ICONS = new Identifier[SLOTS.length];

    static {
        for (CosmeticSlot slot : SLOTS) {
            ICONS[slot.ordinal()] = CozyChristmas.id("container/slot/" + slot.getSerializedName());
        }
    }

    public CosmeticMenuSlot(CosmeticsContainer container, int index, int x, int y) {
        super(container, index, x, y);
    }

    public CosmeticSlot cosmeticSlot() {
        return CosmeticsContainer.slot(getContainerSlot());
    }

    /**
     * The cosmetic slot behind an empty-slot icon. The creative inventory wraps slots and only passes the icon
     * through, so this is how its tooltips find out which slot is hovered.
     */
    public static @Nullable CosmeticSlot slotOfIcon(@Nullable Identifier icon) {
        for (int i = 0; i < ICONS.length; i++) {
            if (ICONS[i].equals(icon)) {
                return SLOTS[i];
            }
        }
        return null;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return CosmeticsManager.canEquip(cosmeticSlot(), stack);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public Identifier getNoItemIcon() {
        return ICONS[getContainerSlot()];
    }
}
