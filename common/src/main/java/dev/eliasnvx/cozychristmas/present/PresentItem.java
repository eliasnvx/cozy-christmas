package dev.eliasnvx.cozychristmas.present;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

/** The present as an item. It keeps its contents in a component, so like a shulker box it can't go into containers. */
public class PresentItem extends BlockItem {

    public PresentItem(Block block, Properties properties) {
        super(block, properties);
    }

    /** No presents in bundles or shulker boxes (and none in presents, see the wrapping table): bounded nesting. */
    @Override
    public boolean canFitInsideContainerItems() {
        return false;
    }
}
