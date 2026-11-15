package dev.eliasnvx.cozychristmas.goblin;

import dev.eliasnvx.cozychristmas.registry.CozyTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;

/**
 * What a present goblin takes: one whole stack from a random hotbar slot that holds something, skipping
 * {@code #cozychristmas:goblin_proof}. The held item counts, so a goblin can run off with your sword.
 */
public final class GoblinLoot {

    private GoblinLoot() {
    }

    /** Hotbar slot to rob, or -1 if nothing there can be taken. */
    public static int pickSlot(Inventory inventory, RandomSource random) {
        boolean[] stealable = new boolean[Inventory.getSelectionSize()];
        for (int slot = 0; slot < stealable.length; slot++) {
            stealable[slot] = !inventory.getItem(slot).isEmpty() && !inventory.getItem(slot).is(CozyTags.GOBLIN_PROOF);
        }
        return pickSlot(stealable, random.nextInt(Math.max(1, count(stealable))));
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
