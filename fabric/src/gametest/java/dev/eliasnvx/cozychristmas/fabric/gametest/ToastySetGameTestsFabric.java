package dev.eliasnvx.cozychristmas.fabric.gametest;

import dev.eliasnvx.cozychristmas.gametest.ToastySetGameTests;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

import java.util.Arrays;

/**
 * Fabric glue for the shared {@link ToastySetGameTests} (server GameTests, part of {@code build}). One method per entry
 * of {@link ToastySetGameTests#ALL}; {@link #everySharedTestRegistered} catches a missing one.
 */
public final class ToastySetGameTestsFabric implements FabricGameTest {

    @GameTest(template = EMPTY_STRUCTURE)
    public void fullSetKeepsWarm(GameTestHelper helper) {
        ToastySetGameTests.fullSetKeepsWarm(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void fasterOnSnowOnly(GameTestHelper helper) {
        ToastySetGameTests.fasterOnSnowOnly(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void everySweaterCraftsAndCounts(GameTestHelper helper) {
        ToastySetGameTests.everySweaterCraftsAndCounts(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void everySharedTestRegistered(GameTestHelper helper) {
        long wrappers = Arrays.stream(getClass().getMethods())
                .filter(method -> method.isAnnotationPresent(GameTest.class))
                .count() - 1;
        helper.assertValueEqual(wrappers, (long) ToastySetGameTests.ALL.size(), "Fabric wrappers for the shared tests");
        helper.succeed();
    }
}
