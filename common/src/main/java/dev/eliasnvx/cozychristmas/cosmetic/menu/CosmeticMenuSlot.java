package dev.eliasnvx.cozychristmas.cosmetic.menu;

import com.mojang.datafixers.util.Pair;
import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.cosmetic.CosmeticSlot;
import dev.eliasnvx.cozychristmas.cosmetic.CosmeticsManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/** A cosmetic slot in the player's inventory menu: takes one item of its {@link CosmeticSlot}. */
public final class CosmeticMenuSlot extends Slot {

    private static final CosmeticSlot[] SLOTS = CosmeticSlot.values();
    /** Outline icons of empty slots, like vanilla's armor slots: {@code textures/gui/sprites/container/slot/<slot>.png}. */
    private static final ResourceLocation[] ICONS = new ResourceLocation[SLOTS.length];

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
    public static @Nullable CosmeticSlot slotOfIcon(@Nullable ResourceLocation icon) {
        for (int i = 0; i < ICONS.length; i++) {
            if (ICONS[i].equals(icon)) {
                return SLOTS[i];
            }
        }
        return null;
    }

    /** {@link #slotOfIcon(ResourceLocation)} for a 1.20.1 icon: an atlas and a sprite in it. */
    public static @Nullable CosmeticSlot slotOfIcon(@Nullable Pair<ResourceLocation, ResourceLocation> icon) {
        return icon == null ? null : slotOfIcon(icon.getSecond());
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return CosmeticsManager.canEquip(cosmeticSlot(), stack);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    /** 1.20.1 draws empty-slot icons from the block atlas: {@code assets/minecraft/atlases/blocks.json} adds them there. */
    @Override
    public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
        return Pair.of(InventoryMenu.BLOCK_ATLAS, ICONS[getContainerSlot()]);
    }
}
