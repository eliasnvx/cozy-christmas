package dev.eliasnvx.cozychristmas.cold;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ColdTest {

    private static final int SOLID = 140;

    @Test
    void chillAddsUpToFrozenSolid() {
        assertEquals(60, Cold.chilled(0, 60, SOLID));
        assertEquals(SOLID, Cold.chilled(100, 60, SOLID));
    }

    @Test
    void chillNeverThaws() {
        assertEquals(SOLID + 20, Cold.chilled(SOLID + 20, 60, SOLID));
    }
}
