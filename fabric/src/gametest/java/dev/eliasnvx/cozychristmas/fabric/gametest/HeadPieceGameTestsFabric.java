package dev.eliasnvx.cozychristmas.fabric.gametest;

import dev.eliasnvx.cozychristmas.gametest.HeadPieceGameTests;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

import java.util.Arrays;

/**
 * Fabric glue for the shared {@link HeadPieceGameTests} (server GameTests, part of {@code build}). One method per entry
 * of {@link HeadPieceGameTests#ALL}; {@link #everySharedTestRegistered} catches a missing one.
 */
public final class HeadPieceGameTestsFabric {

    @GameTest
    public void headPiecesCraftAndTrade(GameTestHelper helper) {
        HeadPieceGameTests.headPiecesCraftAndTrade(helper);
    }

    @GameTest
    public void earWarmersCompleteTheToastySet(GameTestHelper helper) {
        HeadPieceGameTests.earWarmersCompleteTheToastySet(helper);
    }

    @GameTest
    public void everySharedTestRegistered(GameTestHelper helper) {
        long wrappers = Arrays.stream(getClass().getMethods())
                .filter(method -> method.isAnnotationPresent(GameTest.class))
                .count() - 1;
        helper.assertValueEqual(wrappers, (long) HeadPieceGameTests.ALL.size(), "Fabric wrappers for the shared tests");
        helper.succeed();
    }
}
