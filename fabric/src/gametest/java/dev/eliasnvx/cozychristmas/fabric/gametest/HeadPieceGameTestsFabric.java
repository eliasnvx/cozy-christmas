package dev.eliasnvx.cozychristmas.fabric.gametest;

import dev.eliasnvx.cozychristmas.gametest.GameTestAsserts;
import dev.eliasnvx.cozychristmas.gametest.HeadPieceGameTests;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

import java.util.Arrays;

/**
 * Fabric glue for the shared {@link HeadPieceGameTests} (server GameTests, part of {@code build}). One method per entry
 * of {@link HeadPieceGameTests#ALL}; {@link #everySharedTestRegistered} catches a missing one.
 */
public final class HeadPieceGameTestsFabric implements FabricGameTest {

    @GameTest(template = EMPTY_STRUCTURE)
    public void headPiecesCraftAndTrade(GameTestHelper helper) {
        HeadPieceGameTests.headPiecesCraftAndTrade(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void earWarmersCompleteTheToastySet(GameTestHelper helper) {
        HeadPieceGameTests.earWarmersCompleteTheToastySet(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void everySharedTestRegistered(GameTestHelper helper) {
        long wrappers = Arrays.stream(getClass().getMethods())
                .filter(method -> method.isAnnotationPresent(GameTest.class))
                .count() - 1;
        GameTestAsserts.assertValueEqual(helper, wrappers, (long) HeadPieceGameTests.ALL.size(), "Fabric wrappers for the shared tests");
        helper.succeed();
    }
}
