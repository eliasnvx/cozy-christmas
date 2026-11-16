package dev.eliasnvx.cozychristmas.present;

import dev.eliasnvx.cozychristmas.registry.CozyComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * The present as an item. It keeps its contents in its NBT ({@code cozychristmas:present_contents}), so like a
 * shulker box it can't go into containers. The design is vanilla's {@code BlockStateTag}, which placing reads.
 */
public class PresentItem extends BlockItem {

    public PresentItem(Block block, Properties properties) {
        super(block, properties);
    }

    /** No presents in bundles or shulker boxes (and none in presents, see the wrapping table): bounded nesting. */
    @Override
    public boolean canFitInsideContainerItems() {
        return false;
    }

    /** Placing hands the contents to the block entity, so breaking and placing a present again keeps them. */
    @Override
    protected boolean updateCustomBlockEntityTag(BlockPos pos, Level level, @Nullable Player player, ItemStack stack,
                                                 BlockState state) {
        boolean changed = super.updateCustomBlockEntityTag(pos, level, player, stack, state);
        PresentContents contents = CozyComponents.PRESENT_CONTENTS.get(stack);
        // a plain surprise is the block entity's own default (and keeps a creative pick-block copy's contents)
        if (!level.isClientSide() && contents != null && !contents.equals(PresentContents.SURPRISE)
                && level.getBlockEntity(pos) instanceof PresentBlockEntity present) {
            present.setContents(contents);
            return true;
        }
        return changed;
    }

    /**
     * Decided surprises don't stack: stacking with a known one would tell loot from coal before opening. Vanilla
     * 1.20.1 reads the stack size from the item only: Forge asks this itself ({@code IForgeItem#getMaxStackSize(ItemStack)},
     * same signature), elsewhere a mixin on {@code ItemStack#getMaxStackSize} has to.
     */
    public int getMaxStackSize(ItemStack stack) {
        PresentContents contents = CozyComponents.PRESENT_CONTENTS.get(stack);
        return contents instanceof PresentContents.Surprise surprise && surprise.decided().isPresent() ? 1 : getMaxStackSize();
    }

    /** The design the present places with, from its {@code BlockStateTag}; red if it has none. */
    public static PresentDesign design(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(BLOCK_STATE_TAG)) {
            return PresentDesign.RED;
        }
        String name = tag.getCompound(BLOCK_STATE_TAG).getString(PresentBlock.DESIGN.getName());
        return PresentBlock.DESIGN.getValue(name).orElse(PresentDesign.RED);
    }

    /** Places as {@code design}: vanilla's {@code BlockStateTag}, as the block state component on later versions. */
    public static ItemStack withDesign(ItemStack stack, PresentDesign design) {
        stack.getOrCreateTagElement(BLOCK_STATE_TAG).putString(PresentBlock.DESIGN.getName(), design.getSerializedName());
        return stack;
    }
}
