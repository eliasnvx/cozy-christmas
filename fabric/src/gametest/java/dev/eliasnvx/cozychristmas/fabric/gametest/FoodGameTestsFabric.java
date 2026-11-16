package dev.eliasnvx.cozychristmas.fabric.gametest;

import dev.eliasnvx.cozychristmas.gametest.FoodGameTests;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

import java.util.Arrays;

/**
 * Fabric glue for the shared {@link FoodGameTests} (server GameTests, part of {@code build}). One method per entry of
 * {@link FoodGameTests#ALL}; {@link #everySharedTestRegistered} catches a missing one.
 */
public final class FoodGameTestsFabric {

    @GameTest
    public void cocoaThawsAndKeepsWarm(GameTestHelper helper) {
        FoodGameTests.cocoaThawsAndKeepsWarm(helper);
    }

    @GameTest
    public void mulledWineRegenerates(GameTestHelper helper) {
        FoodGameTests.mulledWineRegenerates(helper);
    }

    @GameTest
    public void tangerineRingsIn(GameTestHelper helper) {
        FoodGameTests.tangerineRingsIn(helper);
    }

    @GameTest
    public void leavesDropChestnuts(GameTestHelper helper) {
        FoodGameTests.leavesDropChestnuts(helper);
    }

    @GameTest
    public void chestnutsRoastOnCampfire(GameTestHelper helper) {
        FoodGameTests.chestnutsRoastOnCampfire(helper);
    }

    @GameTest
    public void everySharedTestRegistered(GameTestHelper helper) {
        long wrappers = Arrays.stream(getClass().getMethods())
                .filter(method -> method.isAnnotationPresent(GameTest.class))
                .count() - 1;
        helper.assertValueEqual(wrappers, (long) FoodGameTests.ALL.size(), "Fabric wrappers for the shared tests");
        helper.succeed();
    }
}
