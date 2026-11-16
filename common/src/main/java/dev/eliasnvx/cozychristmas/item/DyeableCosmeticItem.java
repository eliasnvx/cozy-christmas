package dev.eliasnvx.cozychristmas.item;

import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Knitwear and felt dyed like leather armor: 1.20.1's dye recipe takes {@link DyeableLeatherItem}s and stores the color
 * in {@code display.color}. Undyed it is {@code undyed} (knit red, felt green) instead of leather brown.
 */
public class DyeableCosmeticItem extends Item implements DyeableLeatherItem {

    private final int undyed;

    public DyeableCosmeticItem(Properties properties, int undyed) {
        super(properties);
        this.undyed = undyed;
    }

    /** The color without a dye (RGB). */
    public int undyedColor() {
        return undyed;
    }

    @Override
    public int getColor(ItemStack stack) {
        return hasCustomColor(stack) ? DyeableLeatherItem.super.getColor(stack) : undyed;
    }
}
