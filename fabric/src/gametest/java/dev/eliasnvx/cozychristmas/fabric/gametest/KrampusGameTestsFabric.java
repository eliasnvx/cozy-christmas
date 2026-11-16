package dev.eliasnvx.cozychristmas.fabric.gametest;

import dev.eliasnvx.cozychristmas.gametest.GameTestAsserts;
import dev.eliasnvx.cozychristmas.gametest.KrampusGameTests;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

import java.util.Arrays;

/**
 * Fabric glue for the shared {@link KrampusGameTests} (server GameTests, part of {@code build}). One method per entry
 * of {@link KrampusGameTests#ALL}; {@link #everySharedTestRegistered} catches a missing one.
 */
public final class KrampusGameTestsFabric implements FabricGameTest {

    private static final int SLOW = 100;

    @GameTest(template = EMPTY_STRUCTURE)
    public void lashHurtsAndPulls(GameTestHelper helper) {
        KrampusGameTests.lashHurtsAndPulls(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = SLOW)
    public void sackHoldsAPlayerStill(GameTestHelper helper) {
        KrampusGameTests.sackHoldsAPlayerStill(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void callsGoblins(GameTestHelper helper) {
        KrampusGameTests.callsGoblins(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void beatingHimClearsBadKarma(GameTestHelper helper) {
        KrampusGameTests.beatingHimClearsBadKarma(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void visitsANaughtyPlayer(GameTestHelper helper) {
        KrampusGameTests.visitsANaughtyPlayer(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void lairDataLoads(GameTestHelper helper) {
        KrampusGameTests.lairDataLoads(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void hornsAndChain(GameTestHelper helper) {
        KrampusGameTests.hornsAndChain(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void noRidingOffInTheSack(GameTestHelper helper) {
        KrampusGameTests.noRidingOffInTheSack(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void everySharedTestRegistered(GameTestHelper helper) {
        long wrappers = Arrays.stream(getClass().getMethods())
                .filter(method -> method.isAnnotationPresent(GameTest.class))
                .count() - 1;
        GameTestAsserts.assertValueEqual(helper, wrappers, (long) KrampusGameTests.ALL.size(), "Fabric wrappers for the shared tests");
        helper.succeed();
    }
}
