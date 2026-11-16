package dev.eliasnvx.cozychristmas.fabric.gametest;

import dev.eliasnvx.cozychristmas.gametest.TreeGameTests;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

import java.util.Arrays;

/**
 * Fabric glue for the shared {@link TreeGameTests} (server GameTests, part of {@code build}). One method per entry of
 * {@link TreeGameTests#ALL}; {@link #everySharedTestRegistered} catches a missing one.
 */
public final class TreeGameTestsFabric {

    @GameTest
    public void bringsAPresentOnceAMorning(GameTestHelper helper) {
        TreeGameTests.bringsAPresentOnceAMorning(helper);
    }

    @GameTest
    public void needsOrnamentsSeasonAndOwner(GameTestHelper helper) {
        TreeGameTests.needsOrnamentsSeasonAndOwner(helper);
    }

    @GameTest
    public void oneTreeAMorningPerPlayer(GameTestHelper helper) {
        TreeGameTests.oneTreeAMorningPerPlayer(helper);
    }

    @GameTest
    public void presentsStopPilingUp(GameTestHelper helper) {
        TreeGameTests.presentsStopPilingUp(helper);
    }

    @GameTest
    public void ornamentsAndStarHoldOn(GameTestHelper helper) {
        TreeGameTests.ornamentsAndStarHoldOn(helper);
    }

    @GameTest
    public void onlyTheOwnerTakesTheStarDown(GameTestHelper helper) {
        TreeGameTests.onlyTheOwnerTakesTheStarDown(helper);
    }

    @GameTest
    public void everySharedTestRegistered(GameTestHelper helper) {
        long wrappers = Arrays.stream(getClass().getMethods())
                .filter(method -> method.isAnnotationPresent(GameTest.class))
                .count() - 1;
        helper.assertValueEqual(wrappers, (long) TreeGameTests.ALL.size(), "Fabric wrappers for the shared tests");
        helper.succeed();
    }
}
