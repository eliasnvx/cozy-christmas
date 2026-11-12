package dev.eliasnvx.cozychristmas.cosmetic.menu;

import dev.eliasnvx.cozychristmas.cosmetic.CosmeticSlot;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Every case runs alone and right of Catgirl &amp; Catboy Mod's panel (its width 50 plus a gap). */
class CosmeticsLayoutTest {

    private static final int SLOTS = CosmeticSlot.values().length;
    private static final int CATGIRL_SHIFT = 52;

    @ParameterizedTest
    @ValueSource(ints = {0, CATGIRL_SHIFT})
    void slotsDoNotOverlap(int shift) {
        Set<Long> frames = new HashSet<>();
        for (int i = 0; i < SLOTS; i++) {
            int x = CosmeticsLayout.slotX(CosmeticsLayout.SURVIVAL_WIDTH, shift, i);
            int y = CosmeticsLayout.slotY(i);
            assertTrue(frames.add((long) x << 32 | y), "slot " + i + " shares its position");
        }
        for (int i = 0; i < SLOTS; i++) {
            for (int j = i + 1; j < SLOTS; j++) {
                int dx = Math.abs(CosmeticsLayout.slotX(0, shift, i) - CosmeticsLayout.slotX(0, shift, j));
                int dy = Math.abs(CosmeticsLayout.slotY(i) - CosmeticsLayout.slotY(j));
                assertTrue(dx >= CosmeticsLayout.SLOT || dy >= CosmeticsLayout.SLOT, "slots " + i + " and " + j + " overlap");
            }
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {0, CATGIRL_SHIFT})
    void slotsFitInsideThePanel(int shift) {
        int left = CosmeticsLayout.panelX(CosmeticsLayout.SURVIVAL_WIDTH, shift);
        for (int i = 0; i < SLOTS; i++) {
            // the frame is one pixel up-left of the item and SLOT wide
            int x = CosmeticsLayout.slotX(CosmeticsLayout.SURVIVAL_WIDTH, shift, i) - 1 - left;
            int y = CosmeticsLayout.slotY(i) - 1;
            assertTrue(x >= CosmeticsLayout.PADDING && x + CosmeticsLayout.SLOT <= CosmeticsLayout.WIDTH - CosmeticsLayout.PADDING,
                    "slot " + i + " leaves the panel sideways");
            assertTrue(y >= CosmeticsLayout.PADDING && y + CosmeticsLayout.SLOT <= CosmeticsLayout.HEIGHT - CosmeticsLayout.PADDING,
                    "slot " + i + " leaves the panel vertically");
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {0, CATGIRL_SHIFT})
    void panelStartsRightOfTheInventory(int shift) {
        assertTrue(CosmeticsLayout.panelX(CosmeticsLayout.SURVIVAL_WIDTH, shift) > CosmeticsLayout.SURVIVAL_WIDTH);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, CATGIRL_SHIFT})
    void shiftMovesThePanelAsAWhole(int shift) {
        assertEquals(CosmeticsLayout.panelX(CosmeticsLayout.SURVIVAL_WIDTH, 0) + shift,
                CosmeticsLayout.panelX(CosmeticsLayout.SURVIVAL_WIDTH, shift));
    }
}
