package dev.eliasnvx.cozychristmas.fabric.gametest;

import dev.eliasnvx.cozychristmas.gametest.WoodGameTests;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

import java.util.Arrays;

/**
 * Fabric glue for the shared {@link WoodGameTests} (server GameTests, part of {@code build}). One method per entry of
 * {@link WoodGameTests#ALL}; {@link #everySharedTestRegistered} catches a missing one.
 */
public final class WoodGameTestsFabric {

    @GameTest
    public void setsAreComplete(GameTestHelper helper) {
        WoodGameTests.setsAreComplete(helper);
    }

    @GameTest
    public void recipesCraft(GameTestHelper helper) {
        WoodGameTests.recipesCraft(helper);
    }

    @GameTest
    public void tags(GameTestHelper helper) {
        WoodGameTests.tags(helper);
    }

    @GameTest
    public void signsAreBlockEntities(GameTestHelper helper) {
        WoodGameTests.signsAreBlockEntities(helper);
    }

    @GameTest
    public void axeStrips(GameTestHelper helper) {
        WoodGameTests.axeStrips(helper);
    }

    @GameTest
    public void saplingsGrow(GameTestHelper helper) {
        WoodGameTests.saplingsGrow(helper);
    }

    @GameTest
    public void fruitRipensAndIsPicked(GameTestHelper helper) {
        WoodGameTests.fruitRipensAndIsPicked(helper);
    }

    @GameTest
    public void grovesAreInTheirBiomes(GameTestHelper helper) {
        WoodGameTests.grovesAreInTheirBiomes(helper);
    }

    @GameTest
    public void everySharedTestRegistered(GameTestHelper helper) {
        long wrappers = Arrays.stream(getClass().getMethods())
                .filter(method -> method.isAnnotationPresent(GameTest.class))
                .count() - 1;
        helper.assertValueEqual(wrappers, (long) WoodGameTests.ALL.size(), "Fabric wrappers for the shared tests");
        helper.succeed();
    }
}
