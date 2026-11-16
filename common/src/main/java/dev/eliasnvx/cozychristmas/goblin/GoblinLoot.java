package dev.eliasnvx.cozychristmas.goblin;

import dev.eliasnvx.cozychristmas.registry.CozyTags;
import net.minecraft.util.RandomSource;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * What a present goblin takes: one whole stack from a random hotbar slot that holds something, skipping
 * {@code #cozychristmas:goblin_proof} and anything that saves its holder from death (a totem in the goblin's hand would
 * save the goblin, and be used up). The held item counts, so a goblin can run off with your sword.
 */
public final class GoblinLoot {

    private GoblinLoot() {
    }

    /** Hotbar slot to rob, or -1 if nothing there can be taken. */
    public static int pickSlot(Inventory inventory, RandomSource random) {
        boolean[] stealable = new boolean[Inventory.getSelectionSize()];
        for (int slot = 0; slot < stealable.length; slot++) {
            stealable[slot] = canTake(inventory.getItem(slot));
        }
        return pickSlot(stealable, random.nextInt(Math.max(1, count(stealable))));
    }

    public static boolean canTake(ItemStack stack) {
        return !stack.isEmpty() && !stack.is(CozyTags.GOBLIN_PROOF) && !stack.has(DataComponents.DEATH_PROTECTION);
    }

    /** The {@code roll}-th stealable slot ({@code 0 <= roll < count}), or -1 if none is. */
    public static int pickSlot(boolean[] stealable, int roll) {
        int seen = 0;
        for (int slot = 0; slot < stealable.length; slot++) {
            if (stealable[slot] && seen++ == roll) {
                return slot;
            }
        }
        return -1;
    }

    private static int count(boolean[] stealable) {
        int count = 0;
        for (boolean value : stealable) {
            if (value) {
                count++;
            }
        }
        return count;
    }
}
