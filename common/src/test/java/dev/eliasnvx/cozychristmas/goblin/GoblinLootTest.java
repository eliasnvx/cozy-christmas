package dev.eliasnvx.cozychristmas.goblin;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GoblinLootTest {

    @Test
    void nothingToTake() {
        assertEquals(-1, GoblinLoot.pickSlot(new boolean[9], 0));
    }

    @Test
    void rollPicksAmongStealableSlotsOnly() {
        boolean[] stealable = {false, true, false, false, true, false, false, false, true};
        assertEquals(1, GoblinLoot.pickSlot(stealable, 0));
        assertEquals(4, GoblinLoot.pickSlot(stealable, 1));
        assertEquals(8, GoblinLoot.pickSlot(stealable, 2));
    }

    @Test
    void rollPastTheLastIsNothing() {
        assertEquals(-1, GoblinLoot.pickSlot(new boolean[]{true, false}, 1));
    }
}
