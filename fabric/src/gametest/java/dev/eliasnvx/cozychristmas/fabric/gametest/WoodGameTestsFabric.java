package dev.eliasnvx.cozychristmas.fabric.gametest;

import dev.eliasnvx.cozychristmas.gametest.WoodGameTests;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

import java.util.Arrays;

/** Fabric glue for {@link WoodGameTests}. */
public final class WoodGameTestsFabric implements FabricGameTest {

    @GameTest(template = EMPTY_STRUCTURE)
    public void setsAreComplete(GameTestHelper helper) {
        WoodGameTests.setsAreComplete(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void recipesCraft(GameTestHelper helper) {
        WoodGameTests.recipesCraft(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void tags(GameTestHelper helper) {
        WoodGameTests.tags(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void signsAreBlockEntities(GameTestHelper helper) {
        WoodGameTests.signsAreBlockEntities(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void boatsFloat(GameTestHelper helper) {
        WoodGameTests.boatsFloat(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void axeStrips(GameTestHelper helper) {
        WoodGameTests.axeStrips(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void saplingsGrow(GameTestHelper helper) {
        WoodGameTests.saplingsGrow(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void fruitRipensAndIsPicked(GameTestHelper helper) {
        WoodGameTests.fruitRipensAndIsPicked(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void boneMealRipens(GameTestHelper helper) {
        WoodGameTests.boneMealRipens(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void grovesAreInTheirBiomes(GameTestHelper helper) {
        WoodGameTests.grovesAreInTheirBiomes(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void everySharedTestRegistered(GameTestHelper helper) {
        long wrappers = Arrays.stream(getClass().getMethods())
                .filter(method -> method.isAnnotationPresent(GameTest.class))
                .count() - 1;
        helper.assertTrue(wrappers == WoodGameTests.ALL.size(), "Fabric wrappers for the shared tests: " + wrappers);
        helper.succeed();
    }
}
