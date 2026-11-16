package dev.eliasnvx.cozychristmas.fabric.gametest;

import dev.eliasnvx.cozychristmas.gametest.FoodGameTests;
import dev.eliasnvx.cozychristmas.gametest.GameTestAsserts;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

import java.util.Arrays;

/**
 * Fabric glue for the shared {@link FoodGameTests} (server GameTests, part of {@code build}). One method per entry of
 * {@link FoodGameTests#ALL}; {@link #everySharedTestRegistered} catches a missing one.
 */
public final class FoodGameTestsFabric implements FabricGameTest {

    @GameTest(template = EMPTY_STRUCTURE)
    public void cocoaThawsAndKeepsWarm(GameTestHelper helper) {
        FoodGameTests.cocoaThawsAndKeepsWarm(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void mulledWineRegenerates(GameTestHelper helper) {
        FoodGameTests.mulledWineRegenerates(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void tangerineRingsIn(GameTestHelper helper) {
        FoodGameTests.tangerineRingsIn(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void leavesDropChestnuts(GameTestHelper helper) {
        FoodGameTests.leavesDropChestnuts(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void chestnutsRoastOnCampfire(GameTestHelper helper) {
        FoodGameTests.chestnutsRoastOnCampfire(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void everySharedTestRegistered(GameTestHelper helper) {
        long wrappers = Arrays.stream(getClass().getMethods())
                .filter(method -> method.isAnnotationPresent(GameTest.class))
                .count() - 1;
        GameTestAsserts.assertValueEqual(helper, wrappers, (long) FoodGameTests.ALL.size(), "Fabric wrappers for the shared tests");
        helper.succeed();
    }
}
