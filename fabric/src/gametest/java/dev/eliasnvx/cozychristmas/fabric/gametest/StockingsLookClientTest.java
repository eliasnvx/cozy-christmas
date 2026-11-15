package dev.eliasnvx.cozychristmas.fabric.gametest;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotOptions;

import java.util.Locale;

/**
 * Visual check of the fireplace (lit and out) and the stockings in every fill and style, hung on a brick chimney
 * breast, by day and at night. Screenshots land in {@code fabric/build/run/clientGameTest/screenshots}.
 */
public final class StockingsLookClientTest implements FabricClientGameTest {

    private static final int SETTLE = 20;
    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;
    private static final int Y = 100;

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            world.getConnection().waitForChunksRender();
            world.getServer().runCommand("gamerule spawn_mobs false");
            world.getServer().runCommand("weather clear");
            room(world);
            world.getServer().runCommand(String.format(Locale.ROOT, "tp @p 0.5 %d 5.5 180 12", Y));
            world.getServer().runCommand("time set noon");
            context.waitTicks(SETTLE * 2);
            world.getConnection().waitForChunksRender();
            shot(context, "stockings_day");
            world.getServer().runCommand("time set midnight");
            context.waitTicks(SETTLE);
            shot(context, "stockings_night");
            world.getServer().runCommand(String.format(Locale.ROOT, "tp @p 2.2 %d 2.6 155 25", Y));
            world.getServer().runCommand("time set noon");
            context.waitTicks(SETTLE);
            shot(context, "stockings_close");
        }
    }

    /** A floor, a back wall, three fireplaces (two lit) and a stocking over each, plus one more on the wall. */
    private static void room(TestSingleplayerContext world) {
        cmd(world, "fill -6 %d -3 6 %d 7 minecraft:spruce_planks", Y - 1, Y - 1);
        cmd(world, "fill -6 %d -3 6 %d 7 minecraft:air", Y, Y + 6);
        cmd(world, "fill -6 %d -2 6 %d -2 minecraft:bricks", Y, Y + 5);
        String[] fireplaces = {"lit=true", "lit=false", "lit=true"};
        String[] stockings = {"fill=present,style=stocking", "fill=empty,style=stocking", "fill=coal,style=sock"};
        for (int i = 0; i < 3; i++) {
            int x = -3 + i * 3;
            cmd(world, "setblock %d %d -1 cozychristmas:fireplace[facing=south," + fireplaces[i] + "]", x, Y);
            cmd(world, "setblock %d %d -1 minecraft:bricks", x, Y + 1);
            cmd(world, "setblock %d %d 0 cozychristmas:stocking[facing=south," + stockings[i] + "]", x, Y + 1);
        }
        cmd(world, "setblock 4 %d -1 cozychristmas:stocking[facing=south,fill=present,style=sock]", Y + 2);
    }

    private static void cmd(TestSingleplayerContext world, String format, Object... args) {
        world.getServer().runCommand(String.format(Locale.ROOT, format, args));
    }

    private static void shot(ClientGameTestContext context, String name) {
        context.runOnClient(mc -> {
            if (!mc.gui.hud.isHidden()) {
                mc.gui.hud.toggle();
            }
        });
        context.waitTicks(2);
        context.takeScreenshot(TestScreenshotOptions.of(name).withSize(WIDTH, HEIGHT));
        context.runOnClient(mc -> mc.gui.hud.toggle());
    }
}
