package dev.eliasnvx.cozychristmas.fabric.gametest;

import dev.eliasnvx.cozychristmas.gametest.ToastySetGameTests;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

import java.util.Arrays;

/**
 * Fabric glue for the shared {@link ToastySetGameTests} (server GameTests, part of {@code build}). One method per entry
 * of {@link ToastySetGameTests#ALL}; {@link #everySharedTestRegistered} catches a missing one.
 */
public final class ToastySetGameTestsFabric {

    @GameTest
    public void fullSetKeepsWarm(GameTestHelper helper) {
        ToastySetGameTests.fullSetKeepsWarm(helper);
    }

    @GameTest
    public void fasterOnSnowOnly(GameTestHelper helper) {
        ToastySetGameTests.fasterOnSnowOnly(helper);
    }

    @GameTest
    public void everySweaterCraftsAndCounts(GameTestHelper helper) {
        ToastySetGameTests.everySweaterCraftsAndCounts(helper);
    }

    @GameTest
    public void everySharedTestRegistered(GameTestHelper helper) {
        long wrappers = Arrays.stream(getClass().getMethods())
                .filter(method -> method.isAnnotationPresent(GameTest.class))
                .count() - 1;
        helper.assertValueEqual(wrappers, (long) ToastySetGameTests.ALL.size(), "Fabric wrappers for the shared tests");
        helper.succeed();
    }
}
