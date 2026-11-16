package dev.eliasnvx.cozychristmas.fabric.gametest;

import dev.eliasnvx.cozychristmas.gametest.WorkshopGameTests;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

import java.util.Arrays;

/**
 * Fabric glue for the shared {@link WorkshopGameTests} (server GameTests, part of {@code build}). One method per entry
 * of {@link WorkshopGameTests#ALL}; {@link #everySharedTestRegistered} catches a missing one.
 */
public final class WorkshopGameTestsFabric implements FabricGameTest {

    @GameTest(template = EMPTY_STRUCTURE)
    public void hurtingAnElfBringsSanta(GameTestHelper helper) {
        WorkshopGameTests.hurtingAnElfBringsSanta(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void stealingAngersSanta(GameTestHelper helper) {
        WorkshopGameTests.stealingAngersSanta(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void santaTurnsAwayTheNaughty(GameTestHelper helper) {
        WorkshopGameTests.santaTurnsAwayTheNaughty(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void santaGivesTheNice(GameTestHelper helper) {
        WorkshopGameTests.santaGivesTheNice(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void reindeerTameWithCarrots(GameTestHelper helper) {
        WorkshopGameTests.reindeerTameWithCarrots(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void reindeerFlyInSeason(GameTestHelper helper) {
        WorkshopGameTests.reindeerFlyInSeason(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void workshopDataLoads(GameTestHelper helper) {
        WorkshopGameTests.workshopDataLoads(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void flyingReindeerIsAFlyingVehicle(GameTestHelper helper) {
        WorkshopGameTests.flyingReindeerIsAFlyingVehicle(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void santaStaysAngryAcrossARelog(GameTestHelper helper) {
        WorkshopGameTests.santaStaysAngryAcrossARelog(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void stockWaitsForItsOpener(GameTestHelper helper) {
        WorkshopGameTests.stockWaitsForItsOpener(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void blowingUpTheWarehouseIsTheft(GameTestHelper helper) {
        WorkshopGameTests.blowingUpTheWarehouseIsTheft(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void everySharedTestRegistered(GameTestHelper helper) {
        long wrappers = Arrays.stream(getClass().getMethods())
                .filter(method -> method.isAnnotationPresent(GameTest.class))
                .count() - 1;
        helper.assertValueEqual(wrappers, (long) WorkshopGameTests.ALL.size(), "Fabric wrappers for the shared tests");
        helper.succeed();
    }
}
