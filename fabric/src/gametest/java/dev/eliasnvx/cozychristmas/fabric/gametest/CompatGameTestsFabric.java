package dev.eliasnvx.cozychristmas.fabric.gametest;

import dev.eliasnvx.cozychristmas.gametest.CompatGameTests;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

import java.util.Arrays;

/**
 * Fabric glue for the shared {@link CompatGameTests} (server GameTests, part of {@code build}). One method per entry of
 * {@link CompatGameTests#ALL}; {@link #everySharedTestRegistered} catches a missing one.
 */
public final class CompatGameTestsFabric {

    @GameTest
    public void otherModsSlotsCount(GameTestHelper helper) {
        CompatGameTests.otherModsSlotsCount(helper);
    }

    @GameTest
    public void sereneSeasonsCalendar(GameTestHelper helper) {
        CompatGameTests.sereneSeasonsCalendar(helper);
    }

    @GameTest
    public void everySharedTestRegistered(GameTestHelper helper) {
        long wrappers = Arrays.stream(getClass().getMethods())
                .filter(method -> method.isAnnotationPresent(GameTest.class))
                .count() - 1;
        helper.assertValueEqual(wrappers, (long) CompatGameTests.ALL.size(), "Fabric wrappers for the shared tests");
        helper.succeed();
    }
}
