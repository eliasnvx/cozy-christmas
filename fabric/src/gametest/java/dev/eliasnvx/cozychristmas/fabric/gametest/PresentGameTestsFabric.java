package dev.eliasnvx.cozychristmas.fabric.gametest;

import dev.eliasnvx.cozychristmas.gametest.PresentGameTests;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

import java.util.Arrays;

/**
 * Fabric glue for the shared {@link PresentGameTests} (server GameTests, part of {@code build}). One method per entry
 * of {@link PresentGameTests#ALL}; {@link #everySharedTestRegistered} catches a missing one.
 */
public final class PresentGameTestsFabric {

    @GameTest
    public void keepsContentsWhenBroken(GameTestHelper helper) {
        PresentGameTests.keepsContentsWhenBroken(helper);
    }

    @GameTest
    public void giftThanksGiver(GameTestHelper helper) {
        PresentGameTests.giftThanksGiver(helper);
    }

    @GameTest
    public void coalAndLoot(GameTestHelper helper) {
        PresentGameTests.coalAndLoot(helper);
    }

    @GameTest
    public void boomSparesBlocks(GameTestHelper helper) {
        PresentGameTests.boomSparesBlocks(helper);
    }

    /** Waits for the present's player checks twice; NeoForge gives every test 100 ticks too. */
    @GameTest(maxTicks = 100)
    public void boomTicksNearPlayers(GameTestHelper helper) {
        PresentGameTests.boomTicksNearPlayers(helper);
    }

    @GameTest
    public void wrappingTable(GameTestHelper helper) {
        PresentGameTests.wrappingTable(helper);
    }

    @GameTest
    public void snowPresents(GameTestHelper helper) {
        PresentGameTests.snowPresents(helper);
    }

    @GameTest
    public void everySharedTestRegistered(GameTestHelper helper) {
        long wrappers = Arrays.stream(getClass().getMethods())
                .filter(method -> method.isAnnotationPresent(GameTest.class))
                .count() - 1;
        helper.assertValueEqual(wrappers, (long) PresentGameTests.ALL.size(), "Fabric wrappers for the shared tests");
        helper.succeed();
    }
}
