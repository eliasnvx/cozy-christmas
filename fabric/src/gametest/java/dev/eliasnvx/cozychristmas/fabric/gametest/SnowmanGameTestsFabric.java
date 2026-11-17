package dev.eliasnvx.cozychristmas.fabric.gametest;

import dev.eliasnvx.cozychristmas.gametest.GameTestAsserts;
import dev.eliasnvx.cozychristmas.gametest.SnowmanGameTests;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

import java.util.Arrays;

/**
 * Fabric glue for the shared {@link SnowmanGameTests} (server GameTests, part of {@code build}). One method per entry
 * of {@link SnowmanGameTests#ALL}; {@link #everySharedTestRegistered} catches a missing one.
 */
public final class SnowmanGameTestsFabric implements FabricGameTest {

    private static final int SLOW = 100;

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = SLOW)
    public void iceSnowballsSlowAndChill(GameTestHelper helper) {
        SnowmanGameTests.iceSnowballsSlowAndChill(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = SLOW)
    public void snowmenDontHitEachOther(GameTestHelper helper) {
        SnowmanGameTests.snowmenDontHitEachOther(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void throwsIceSnowballs(GameTestHelper helper) {
        SnowmanGameTests.throwsIceSnowballs(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void fireHurtsMore(GameTestHelper helper) {
        SnowmanGameTests.fireHurtsMore(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = SLOW)
    public void litFireplaceMelts(GameTestHelper helper) {
        SnowmanGameTests.litFireplaceMelts(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = SLOW)
    public void dispensersThrow(GameTestHelper helper) {
        SnowmanGameTests.dispensersThrow(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void spawnInSnowyBiomes(GameTestHelper helper) {
        SnowmanGameTests.spawnInSnowyBiomes(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = SLOW)
    public void leavesSnow(GameTestHelper helper) {
        SnowmanGameTests.leavesSnow(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void everySharedTestRegistered(GameTestHelper helper) {
        long wrappers = Arrays.stream(getClass().getMethods())
                .filter(method -> method.isAnnotationPresent(GameTest.class))
                .count() - 1;
        GameTestAsserts.assertValueEqual(helper, wrappers, (long) SnowmanGameTests.ALL.size(), "Fabric wrappers for the shared tests");
        helper.succeed();
    }
}
