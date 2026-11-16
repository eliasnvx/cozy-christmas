package dev.eliasnvx.cozychristmas.fabric.gametest;

import dev.eliasnvx.cozychristmas.gametest.CosmeticSlotGameTests;
import dev.eliasnvx.cozychristmas.gametest.GameTestAsserts;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

import java.util.Arrays;

/**
 * Fabric glue for the shared {@link CosmeticSlotGameTests} (server GameTests, part of {@code build}). One method per
 * entry of {@link CosmeticSlotGameTests#ALL}; {@link #everySharedTestRegistered} catches a missing one.
 */
public final class CosmeticSlotGameTestsFabric implements FabricGameTest {

    @GameTest(template = EMPTY_STRUCTURE)
    public void shiftClick(GameTestHelper helper) {
        CosmeticSlotGameTests.shiftClick(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void cursorClicks(GameTestHelper helper) {
        CosmeticSlotGameTests.cursorClicks(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void numberKeys(GameTestHelper helper) {
        CosmeticSlotGameTests.numberKeys(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void drag(GameTestHelper helper) {
        CosmeticSlotGameTests.drag(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void doubleClick(GameTestHelper helper) {
        CosmeticSlotGameTests.doubleClick(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void throwAndMiddleClick(GameTestHelper helper) {
        CosmeticSlotGameTests.throwAndMiddleClick(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void rightClickEquip(GameTestHelper helper) {
        CosmeticSlotGameTests.rightClickEquip(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void creativeSlotPacket(GameTestHelper helper) {
        CosmeticSlotGameTests.creativeSlotPacket(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void deathDropsOnce(GameTestHelper helper) {
        CosmeticSlotGameTests.deathDropsOnce(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void keepInventory(GameTestHelper helper) {
        CosmeticSlotGameTests.keepInventory(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void leavingTheEnd(GameTestHelper helper) {
        CosmeticSlotGameTests.leavingTheEnd(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void cursedOnesVanish(GameTestHelper helper) {
        CosmeticSlotGameTests.cursedOnesVanish(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void everySharedTestRegistered(GameTestHelper helper) {
        long wrappers = Arrays.stream(getClass().getMethods())
                .filter(method -> method.isAnnotationPresent(GameTest.class))
                .count() - 1;
        GameTestAsserts.assertValueEqual(helper, wrappers, (long) CosmeticSlotGameTests.ALL.size(), "Fabric wrappers for the shared tests");
        helper.succeed();
    }
}
