package dev.eliasnvx.cozychristmas.fabric.gametest;

import dev.eliasnvx.cozychristmas.gametest.SnowmanGameTests;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

import java.util.Arrays;

/**
 * Fabric glue for the shared {@link SnowmanGameTests} (server GameTests, part of {@code build}). One method per entry
 * of {@link SnowmanGameTests#ALL}; {@link #everySharedTestRegistered} catches a missing one.
 */
public final class SnowmanGameTestsFabric {

    private static final int SLOW = 100;

    @GameTest(maxTicks = SLOW)
    public void iceSnowballsSlowAndChill(GameTestHelper helper) {
        SnowmanGameTests.iceSnowballsSlowAndChill(helper);
    }

    @GameTest(maxTicks = SLOW)
    public void snowmenDontHitEachOther(GameTestHelper helper) {
        SnowmanGameTests.snowmenDontHitEachOther(helper);
    }

    @GameTest
    public void throwsIceSnowballs(GameTestHelper helper) {
        SnowmanGameTests.throwsIceSnowballs(helper);
    }

    @GameTest
    public void fireHurtsMore(GameTestHelper helper) {
        SnowmanGameTests.fireHurtsMore(helper);
    }

    @GameTest(maxTicks = SLOW)
    public void litFireplaceMelts(GameTestHelper helper) {
        SnowmanGameTests.litFireplaceMelts(helper);
    }

    @GameTest(maxTicks = SLOW)
    public void dispensersThrow(GameTestHelper helper) {
        SnowmanGameTests.dispensersThrow(helper);
    }

    @GameTest
    public void spawnInSnowyBiomes(GameTestHelper helper) {
        SnowmanGameTests.spawnInSnowyBiomes(helper);
    }

    @GameTest
    public void everySharedTestRegistered(GameTestHelper helper) {
        long wrappers = Arrays.stream(getClass().getMethods())
                .filter(method -> method.isAnnotationPresent(GameTest.class))
                .count() - 1;
        helper.assertValueEqual(wrappers, (long) SnowmanGameTests.ALL.size(), "Fabric wrappers for the shared tests");
        helper.succeed();
    }
}
