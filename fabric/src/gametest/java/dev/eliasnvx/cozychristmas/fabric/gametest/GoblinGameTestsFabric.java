package dev.eliasnvx.cozychristmas.fabric.gametest;

import dev.eliasnvx.cozychristmas.gametest.GoblinGameTests;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

import java.util.Arrays;

/**
 * Fabric glue for the shared {@link GoblinGameTests} (server GameTests, part of {@code build}). One method per entry of
 * {@link GoblinGameTests#ALL}; {@link #everySharedTestRegistered} catches a missing one.
 */
public final class GoblinGameTestsFabric implements FabricGameTest {

    @GameTest(template = EMPTY_STRUCTURE)
    public void snatchesAHotbarStack(GameTestHelper helper) {
        GoblinGameTests.snatchesAHotbarStack(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void leavesCreativeAlone(GameTestHelper helper) {
        GoblinGameTests.leavesCreativeAlone(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void killingAThiefReturnsTheLoot(GameTestHelper helper) {
        GoblinGameTests.killingAThiefReturnsTheLoot(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 100)
    public void gingerbreadMenGoSoggy(GameTestHelper helper) {
        GoblinGameTests.gingerbreadMenGoSoggy(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void goblinCampDataLoads(GameTestHelper helper) {
        GoblinGameTests.goblinCampDataLoads(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void spawnInSnowyBiomes(GameTestHelper helper) {
        GoblinGameTests.spawnInSnowyBiomes(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void neverTakeATotem(GameTestHelper helper) {
        GoblinGameTests.neverTakeATotem(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void lootComesBackWhateverTheRules(GameTestHelper helper) {
        GoblinGameTests.lootComesBackWhateverTheRules(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void returningLootRewardsOnceADay(GameTestHelper helper) {
        GoblinGameTests.returningLootRewardsOnceADay(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void everySharedTestRegistered(GameTestHelper helper) {
        long wrappers = Arrays.stream(getClass().getMethods())
                .filter(method -> method.isAnnotationPresent(GameTest.class))
                .count() - 1;
        helper.assertValueEqual(wrappers, (long) GoblinGameTests.ALL.size(), "Fabric wrappers for the shared tests");
        helper.succeed();
    }
}
