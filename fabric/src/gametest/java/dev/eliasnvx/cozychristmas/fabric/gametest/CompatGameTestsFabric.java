package dev.eliasnvx.cozychristmas.fabric.gametest;

import dev.eliasnvx.cozychristmas.gametest.CompatGameTests;
import dev.eliasnvx.cozychristmas.gametest.GameTestAsserts;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

import java.util.Arrays;

/**
 * Fabric glue for the shared {@link CompatGameTests} (server GameTests, part of {@code build}). One method per entry of
 * {@link CompatGameTests#ALL}; {@link #everySharedTestRegistered} catches a missing one.
 */
public final class CompatGameTestsFabric implements FabricGameTest {

    @GameTest(template = EMPTY_STRUCTURE)
    public void otherModsSlotsCount(GameTestHelper helper) {
        CompatGameTests.otherModsSlotsCount(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void sereneSeasonsCalendar(GameTestHelper helper) {
        CompatGameTests.sereneSeasonsCalendar(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void hiddenAndLookOnlySlots(GameTestHelper helper) {
        CompatGameTests.hiddenAndLookOnlySlots(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void reindeerPullSleds(GameTestHelper helper) {
        CompatGameTests.reindeerPullSleds(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void santaCallsGuards(GameTestHelper helper) {
        CompatGameTests.santaCallsGuards(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void everySharedTestRegistered(GameTestHelper helper) {
        long wrappers = Arrays.stream(getClass().getMethods())
                .filter(method -> method.isAnnotationPresent(GameTest.class))
                .count() - 1;
        GameTestAsserts.assertValueEqual(helper, wrappers, (long) CompatGameTests.ALL.size(), "Fabric wrappers for the shared tests");
        helper.succeed();
    }
}
