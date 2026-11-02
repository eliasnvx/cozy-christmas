package dev.eliasnvx.cozychristmas.gametest;

import net.minecraft.gametest.framework.GameTestHelper;

import java.util.function.Consumer;

/** A shared GameTest: its name and body. Each loader's glue registers every entry of the test classes' {@code ALL}. */
public record GameTestEntry(String name, Consumer<GameTestHelper> body) {
}
