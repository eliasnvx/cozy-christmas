package dev.eliasnvx.cozychristmas.fabric.gametest;

import dev.eliasnvx.cozychristmas.gametest.KrampusGameTests;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

import java.util.Arrays;

/**
 * Fabric glue for the shared {@link KrampusGameTests} (server GameTests, part of {@code build}). One method per entry
 * of {@link KrampusGameTests#ALL}; {@link #everySharedTestRegistered} catches a missing one.
 */
public final class KrampusGameTestsFabric {

    private static final int SLOW = 100;

    @GameTest
    public void lashHurtsAndPulls(GameTestHelper helper) {
        KrampusGameTests.lashHurtsAndPulls(helper);
    }

    @GameTest(maxTicks = SLOW)
    public void sackHoldsAPlayerStill(GameTestHelper helper) {
        KrampusGameTests.sackHoldsAPlayerStill(helper);
    }

    @GameTest
    public void callsGoblins(GameTestHelper helper) {
        KrampusGameTests.callsGoblins(helper);
    }

    @GameTest
    public void beatingHimClearsBadKarma(GameTestHelper helper) {
        KrampusGameTests.beatingHimClearsBadKarma(helper);
    }

    @GameTest
    public void visitsANaughtyPlayer(GameTestHelper helper) {
        KrampusGameTests.visitsANaughtyPlayer(helper);
    }

    @GameTest
    public void lairDataLoads(GameTestHelper helper) {
        KrampusGameTests.lairDataLoads(helper);
    }

    @GameTest
    public void hornsAndChain(GameTestHelper helper) {
        KrampusGameTests.hornsAndChain(helper);
    }

    @GameTest
    public void everySharedTestRegistered(GameTestHelper helper) {
        long wrappers = Arrays.stream(getClass().getMethods())
                .filter(method -> method.isAnnotationPresent(GameTest.class))
                .count() - 1;
        helper.assertValueEqual(wrappers, (long) KrampusGameTests.ALL.size(), "Fabric wrappers for the shared tests");
        helper.succeed();
    }
}
