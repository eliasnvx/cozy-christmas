package dev.eliasnvx.cozychristmas.present;

import dev.eliasnvx.cozychristmas.registry.CozyBlocks;
import dev.eliasnvx.cozychristmas.registry.CozyComponents;
import dev.eliasnvx.cozychristmas.registry.CozyMenus;
import dev.eliasnvx.cozychristmas.registry.CozySounds;
import dev.eliasnvx.cozychristmas.registry.CozyTags;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlockItemStateProperties;

import java.util.Optional;

/**
 * The wrapping table: a gift (any stack but {@code #cozychristmas:unwrappable}) and one wrapping paper make a present
 * in the chosen {@link PresentDesign} (menu buttons 0-3, like the stonecutter's recipe list). The whole stack goes in;
 * {@code #cozychristmas:wraps_as_boom} (TNT) makes a prank and uses one. The present remembers who wrapped it.
 */
public class WrappingTableMenu extends AbstractContainerMenu {

    public static final int GIFT_SLOT = 0;
    public static final int PAPER_SLOT = 1;
    public static final int RESULT_SLOT = 2;
    private static final int INVENTORY_START = 3;
    private static final int INVENTORY_END = INVENTORY_START + Inventory.INVENTORY_SIZE;
    public static final int GIFT_X = 20;
    public static final int GIFT_Y = 24;
    public static final int PAPER_X = 20;
    public static final int PAPER_Y = 48;
    public static final int RESULT_X = 138;
    public static final int RESULT_Y = 36;
    private static final int INVENTORY_X = 8;
    private static final int INVENTORY_Y = 84;
    /** Vanilla's inventory layout: 18 px slots, the hotbar 4 px below the three rows. */
    private static final int SLOT_SIZE = 18;
    private static final int ROW_LENGTH = 9;
    private static final int INVENTORY_ROWS = 3;
    private static final int HOTBAR_OFFSET_Y = 58;

    private final ContainerLevelAccess access;
    private final Player player;
    private final DataSlot design = DataSlot.standalone();
    private final Container inputs = new SimpleContainer(2) {
        @Override
        public void setChanged() {
            super.setChanged();
            slotsChanged(this);
        }
    };
    private final ResultContainer result = new ResultContainer();

    /** Client side: the server checks the distance and does the wrapping. */
    public WrappingTableMenu(int id, Inventory inventory) {
        this(id, inventory, ContainerLevelAccess.NULL);
    }

    public WrappingTableMenu(int id, Inventory inventory, ContainerLevelAccess access) {
        super(CozyMenus.WRAPPING_TABLE.get(), id);
        this.access = access;
        this.player = inventory.player;
        addSlot(new Slot(inputs, GIFT_SLOT, GIFT_X, GIFT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return canWrap(stack);
            }
        });
        addSlot(new Slot(inputs, PAPER_SLOT, PAPER_X, PAPER_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(CozyTags.WRAPPING_PAPER);
            }
        });
        addSlot(new Slot(result, 0, RESULT_X, RESULT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(Player taker, ItemStack present) {
                wrapped(taker);
                super.onTake(taker, present);
            }
        });
        addInventorySlots(inventory);
        addDataSlot(design);
    }

    /** The player's inventory, then the hotbar, as vanilla's container menus lay them out. */
    private void addInventorySlots(Inventory inventory) {
        for (int row = 0; row < INVENTORY_ROWS; row++) {
            for (int column = 0; column < ROW_LENGTH; column++) {
                addSlot(new Slot(inventory, ROW_LENGTH + row * ROW_LENGTH + column,
                        INVENTORY_X + column * SLOT_SIZE, INVENTORY_Y + row * SLOT_SIZE));
            }
        }
        for (int column = 0; column < ROW_LENGTH; column++) {
            addSlot(new Slot(inventory, column, INVENTORY_X + column * SLOT_SIZE, INVENTORY_Y + HOTBAR_OFFSET_Y));
        }
    }

    public static boolean canWrap(ItemStack stack) {
        return !stack.isEmpty() && !stack.is(CozyTags.UNWRAPPABLE);
    }

    /** The present {@code wrapper} gets for {@code gift}; the gift stack isn't touched. */
    public static ItemStack wrap(ItemStack gift, PresentDesign design, Player wrapper) {
        Optional<PresentContents.Giver> from = Optional.of(PresentContents.Giver.of(wrapper));
        PresentContents contents = gift.is(CozyTags.WRAPS_AS_BOOM)
                ? new PresentContents.Prank(from)
                : new PresentContents.Gift(gift, from);
        ItemStack present = new ItemStack(CozyBlocks.PRESENT_ITEM.get());
        present.set(CozyComponents.PRESENT_CONTENTS.get(), contents);
        present.set(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY.with(PresentBlock.DESIGN, design));
        return present;
    }

    public PresentDesign design() {
        return PresentDesign.values()[design.get()];
    }

    /** Buttons 0-3 pick the design (sent by the screen through vanilla's container button packet). */
    @Override
    public boolean clickMenuButton(Player clicker, int id) {
        if (id < 0 || id >= PresentDesign.values().length) {
            return false;
        }
        design.set(id);
        updateResult();
        return true;
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        if (container == inputs) {
            updateResult();
        }
    }

    private void updateResult() {
        ItemStack gift = inputs.getItem(GIFT_SLOT);
        boolean ready = canWrap(gift) && inputs.getItem(PAPER_SLOT).is(CozyTags.WRAPPING_PAPER);
        result.setItem(0, ready ? wrap(gift, design(), player) : ItemStack.EMPTY);
        broadcastChanges();
    }

    /** Uses up what the present was made of: the gift (one if it's a prank) and one paper. */
    private void wrapped(Player taker) {
        ItemStack gift = inputs.getItem(GIFT_SLOT);
        inputs.removeItem(GIFT_SLOT, gift.is(CozyTags.WRAPS_AS_BOOM) ? 1 : gift.getCount());
        inputs.removeItem(PAPER_SLOT, 1);
        access.execute((level, pos) -> level.playSound(null, pos, CozySounds.WRAPPING_TABLE_WRAP.get(), SoundSource.BLOCKS,
                1.0F, 1.0F));
    }

    @Override
    public ItemStack quickMoveStack(Player clicker, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack before = stack.copy();
        if (index == RESULT_SLOT) {
            if (!moveItemStackTo(stack, INVENTORY_START, INVENTORY_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (index < INVENTORY_START) {
            if (!moveItemStackTo(stack, INVENTORY_START, INVENTORY_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.is(CozyTags.WRAPPING_PAPER) && moveItemStackTo(stack, PAPER_SLOT, PAPER_SLOT + 1, false)) {
            // paper fills the paper slot; once that's full, paper can be wrapped like anything else
        } else if (!canWrap(stack) || !moveItemStackTo(stack, GIFT_SLOT, GIFT_SLOT + 1, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        }
        slot.setChanged();
        if (stack.getCount() == before.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(clicker, stack);
        return before;
    }

    @Override
    public boolean stillValid(Player viewer) {
        return stillValid(access, viewer, CozyBlocks.WRAPPING_TABLE.get());
    }

    @Override
    public void removed(Player viewer) {
        super.removed(viewer);
        result.removeItemNoUpdate(0);
        access.execute((level, pos) -> clearContainer(viewer, inputs));
    }
}
