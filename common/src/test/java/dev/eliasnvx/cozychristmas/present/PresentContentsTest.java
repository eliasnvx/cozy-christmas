package dev.eliasnvx.cozychristmas.present;

import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Gifts hold item stacks (registries needed): they're covered by the GameTests. */
class PresentContentsTest {

    private static final PresentContents.Giver STEVE = new PresentContents.Giver(new UUID(1, 2), "Steve");

    @Test
    void roundTrips() {
        for (PresentContents contents : new PresentContents[]{PresentContents.SURPRISE,
                new PresentContents.Surprise(PresentOutcome.COAL), new PresentContents.Prank(Optional.of(STEVE))}) {
            var json = PresentContents.CODEC.encodeStart(JsonOps.INSTANCE, contents).getOrThrow();
            assertEquals(contents, PresentContents.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow());
        }
    }

    @Test
    void onlyBoomsTick() {
        assertTrue(new PresentContents.Prank(Optional.empty()).isBoom());
        assertTrue(new PresentContents.Surprise(PresentOutcome.BOOM).isBoom());
        assertFalse(new PresentContents.Surprise(PresentOutcome.LOOT).isBoom());
        assertFalse(PresentContents.SURPRISE.isBoom());
    }
}
