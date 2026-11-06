package dev.eliasnvx.cozychristmas.fabric.gametest;

import dev.eliasnvx.cozychristmas.gametest.WorkshopGameTests;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

import java.util.Arrays;

/**
 * Fabric glue for the shared {@link WorkshopGameTests} (server GameTests, part of {@code build}). One method per entry
 * of {@link WorkshopGameTests#ALL}; {@link #everySharedTestRegistered} catches a missing one.
 */
public final class WorkshopGameTestsFabric {

    @GameTest
    public void hurtingAnElfBringsSanta(GameTestHelper helper) {
        WorkshopGameTests.hurtingAnElfBringsSanta(helper);
    }

    @GameTest
    public void stealingAngersSanta(GameTestHelper helper) {
        WorkshopGameTests.stealingAngersSanta(helper);
    }

    @GameTest
    public void santaTurnsAwayTheNaughty(GameTestHelper helper) {
        WorkshopGameTests.santaTurnsAwayTheNaughty(helper);
    }

    @GameTest
    public void santaGivesTheNice(GameTestHelper helper) {
        WorkshopGameTests.santaGivesTheNice(helper);
    }

    @GameTest
    public void reindeerTameWithCarrots(GameTestHelper helper) {
        WorkshopGameTests.reindeerTameWithCarrots(helper);
    }

    @GameTest
    public void reindeerFlyInSeason(GameTestHelper helper) {
        WorkshopGameTests.reindeerFlyInSeason(helper);
    }

    @GameTest
    public void workshopDataLoads(GameTestHelper helper) {
        WorkshopGameTests.workshopDataLoads(helper);
    }

    @GameTest
    public void everySharedTestRegistered(GameTestHelper helper) {
        long wrappers = Arrays.stream(getClass().getMethods())
                .filter(method -> method.isAnnotationPresent(GameTest.class))
                .count() - 1;
        helper.assertValueEqual(wrappers, (long) WorkshopGameTests.ALL.size(), "Fabric wrappers for the shared tests");
        helper.succeed();
    }
}
