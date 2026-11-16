package dev.eliasnvx.cozychristmas.fabric.gametest;

import dev.eliasnvx.cozychristmas.gametest.SleighGameTests;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

import java.util.Arrays;

/**
 * Fabric glue for the shared {@link SleighGameTests} (server GameTests, part of {@code build}). One method per entry of
 * {@link SleighGameTests#ALL}; {@link #everySharedTestRegistered} catches a missing one.
 */
public final class SleighGameTestsFabric {

    @GameTest
    public void takesPresentsOnDeliveryDays(GameTestHelper helper) {
        SleighGameTests.takesPresentsOnDeliveryDays(helper);
    }

    @GameTest
    public void elvesLoadIt(GameTestHelper helper) {
        SleighGameTests.elvesLoadIt(helper);
    }

    @GameTest
    public void santaFliesIt(GameTestHelper helper) {
        SleighGameTests.santaFliesIt(helper);
    }

    @GameTest
    public void hidesAgainAfterAReload(GameTestHelper helper) {
        SleighGameTests.hidesAgainAfterAReload(helper);
    }

    @GameTest
    public void removingItLetsSantaDown(GameTestHelper helper) {
        SleighGameTests.removingItLetsSantaDown(helper);
    }

    @GameTest
    public void movedSleighComesBackThere(GameTestHelper helper) {
        SleighGameTests.movedSleighComesBackThere(helper);
    }

    @GameTest
    public void santaAboardIsOutOfReach(GameTestHelper helper) {
        SleighGameTests.santaAboardIsOutOfReach(helper);
    }

    @GameTest
    public void everySharedTestRegistered(GameTestHelper helper) {
        long wrappers = Arrays.stream(getClass().getMethods())
                .filter(method -> method.isAnnotationPresent(GameTest.class))
                .count() - 1;
        helper.assertValueEqual(wrappers, (long) SleighGameTests.ALL.size(), "Fabric wrappers for the shared tests");
        helper.succeed();
    }
}
