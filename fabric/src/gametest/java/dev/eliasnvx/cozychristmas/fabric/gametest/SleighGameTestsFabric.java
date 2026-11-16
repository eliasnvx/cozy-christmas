package dev.eliasnvx.cozychristmas.fabric.gametest;

import dev.eliasnvx.cozychristmas.gametest.GameTestAsserts;
import dev.eliasnvx.cozychristmas.gametest.SleighGameTests;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

import java.util.Arrays;

/**
 * Fabric glue for the shared {@link SleighGameTests} (server GameTests, part of {@code build}). One method per entry of
 * {@link SleighGameTests#ALL}; {@link #everySharedTestRegistered} catches a missing one.
 */
public final class SleighGameTestsFabric implements FabricGameTest {

    @GameTest(template = EMPTY_STRUCTURE)
    public void takesPresentsOnDeliveryDays(GameTestHelper helper) {
        SleighGameTests.takesPresentsOnDeliveryDays(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void elvesLoadIt(GameTestHelper helper) {
        SleighGameTests.elvesLoadIt(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void santaFliesIt(GameTestHelper helper) {
        SleighGameTests.santaFliesIt(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void hidesAgainAfterAReload(GameTestHelper helper) {
        SleighGameTests.hidesAgainAfterAReload(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void removingItLetsSantaDown(GameTestHelper helper) {
        SleighGameTests.removingItLetsSantaDown(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void movedSleighComesBackThere(GameTestHelper helper) {
        SleighGameTests.movedSleighComesBackThere(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void santaAboardIsOutOfReach(GameTestHelper helper) {
        SleighGameTests.santaAboardIsOutOfReach(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void everySharedTestRegistered(GameTestHelper helper) {
        long wrappers = Arrays.stream(getClass().getMethods())
                .filter(method -> method.isAnnotationPresent(GameTest.class))
                .count() - 1;
        GameTestAsserts.assertValueEqual(helper, wrappers, (long) SleighGameTests.ALL.size(), "Fabric wrappers for the shared tests");
        helper.succeed();
    }
}
