package dev.eliasnvx.cozychristmas.fabric.gametest;

import dev.eliasnvx.cozychristmas.gametest.StockingGameTests;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

import java.util.Arrays;

/**
 * Fabric glue for the shared {@link StockingGameTests} (server GameTests, part of {@code build}). One method per entry
 * of {@link StockingGameTests#ALL}; {@link #everySharedTestRegistered} catches a missing one.
 */
public final class StockingGameTestsFabric {

    @GameTest
    public void fillsByKarmaOnceAMorning(GameTestHelper helper) {
        StockingGameTests.fillsByKarmaOnceAMorning(helper);
    }

    @GameTest
    public void needsAFireplaceAndTheSeason(GameTestHelper helper) {
        StockingGameTests.needsAFireplaceAndTheSeason(helper);
    }

    @GameTest
    public void onlyTheOwnerEmptiesIt(GameTestHelper helper) {
        StockingGameTests.onlyTheOwnerEmptiesIt(helper);
    }

    @GameTest
    public void takingItDownDrops(GameTestHelper helper) {
        StockingGameTests.takingItDownDrops(helper);
    }

    @GameTest
    public void fireplaceLightsAndGoesOut(GameTestHelper helper) {
        StockingGameTests.fireplaceLightsAndGoesOut(helper);
    }

    @GameTest
    public void onlyTheOwnerTakesItDown(GameTestHelper helper) {
        StockingGameTests.onlyTheOwnerTakesItDown(helper);
    }

    @GameTest
    public void everySharedTestRegistered(GameTestHelper helper) {
        long wrappers = Arrays.stream(getClass().getMethods())
                .filter(method -> method.isAnnotationPresent(GameTest.class))
                .count() - 1;
        helper.assertValueEqual(wrappers, (long) StockingGameTests.ALL.size(), "Fabric wrappers for the shared tests");
        helper.succeed();
    }
}
