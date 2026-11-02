package dev.eliasnvx.cozychristmas.cosmetic.menu;

import dev.eliasnvx.cozychristmas.cosmetic.CosmeticSlot;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CosmeticsLayoutTest {

    private static final int SLOTS = CosmeticSlot.values().length;

    @Test
    void slotsDoNotOverlap() {
        Set<Long> frames = new HashSet<>();
        for (int i = 0; i < SLOTS; i++) {
            int x = CosmeticsLayout.slotX(CosmeticsLayout.SURVIVAL_WIDTH, i);
            int y = CosmeticsLayout.slotY(i);
            assertTrue(frames.add((long) x << 32 | y), "slot " + i + " shares its position");
        }
        for (int i = 0; i < SLOTS; i++) {
            for (int j = i + 1; j < SLOTS; j++) {
                int dx = Math.abs(CosmeticsLayout.slotX(0, i) - CosmeticsLayout.slotX(0, j));
                int dy = Math.abs(CosmeticsLayout.slotY(i) - CosmeticsLayout.slotY(j));
                assertTrue(dx >= CosmeticsLayout.SLOT || dy >= CosmeticsLayout.SLOT, "slots " + i + " and " + j + " overlap");
            }
        }
    }

    @Test
    void slotsFitInsideThePanel() {
        int left = CosmeticsLayout.panelX(CosmeticsLayout.SURVIVAL_WIDTH);
        for (int i = 0; i < SLOTS; i++) {
            // the frame is one pixel up-left of the item and SLOT wide
            int x = CosmeticsLayout.slotX(CosmeticsLayout.SURVIVAL_WIDTH, i) - 1 - left;
            int y = CosmeticsLayout.slotY(i) - 1;
            assertTrue(x >= CosmeticsLayout.PADDING && x + CosmeticsLayout.SLOT <= CosmeticsLayout.WIDTH - CosmeticsLayout.PADDING,
                    "slot " + i + " leaves the panel sideways");
            assertTrue(y >= CosmeticsLayout.PADDING && y + CosmeticsLayout.SLOT <= CosmeticsLayout.HEIGHT - CosmeticsLayout.PADDING,
                    "slot " + i + " leaves the panel vertically");
        }
    }

    @Test
    void panelStartsRightOfTheInventory() {
        assertTrue(CosmeticsLayout.panelX(CosmeticsLayout.SURVIVAL_WIDTH) > CosmeticsLayout.SURVIVAL_WIDTH);
    }
}
