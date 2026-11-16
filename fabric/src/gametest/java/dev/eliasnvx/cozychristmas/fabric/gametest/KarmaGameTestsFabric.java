package dev.eliasnvx.cozychristmas.fabric.gametest;

import dev.eliasnvx.cozychristmas.gametest.KarmaGameTests;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

import java.util.Arrays;

/**
 * Fabric glue for the shared {@link KarmaGameTests} (server GameTests, part of {@code build}). One method per entry of
 * {@link KarmaGameTests#ALL}; {@link #everySharedTestRegistered} catches a missing one.
 */
public final class KarmaGameTestsFabric implements FabricGameTest {

    @GameTest(template = EMPTY_STRUCTURE)
    public void karmaStaysInRange(GameTestHelper helper) {
        KarmaGameTests.karmaStaysInRange(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void hurtingAVillagerIsNaughty(GameTestHelper helper) {
        KarmaGameTests.hurtingAVillagerIsNaughty(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void hurtingAZombieIsFine(GameTestHelper helper) {
        KarmaGameTests.hurtingAZombieIsFine(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void killingAVillagerCostsMore(GameTestHelper helper) {
        KarmaGameTests.killingAVillagerCostsMore(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void karmaSurvivesDeath(GameTestHelper helper) {
        KarmaGameTests.karmaSurvivesDeath(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void twoPlayersBothPay(GameTestHelper helper) {
        KarmaGameTests.twoPlayersBothPay(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void everySharedTestRegistered(GameTestHelper helper) {
        long wrappers = Arrays.stream(getClass().getMethods())
                .filter(method -> method.isAnnotationPresent(GameTest.class))
                .count() - 1;
        helper.assertValueEqual(wrappers, (long) KarmaGameTests.ALL.size(), "Fabric wrappers for the shared tests");
        helper.succeed();
    }
}
