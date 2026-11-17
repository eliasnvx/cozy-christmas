package dev.eliasnvx.cozychristmas.fabric.gametest;

import dev.eliasnvx.cozychristmas.wood.CozyWoods;
import dev.eliasnvx.cozychristmas.wood.WoodSet;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotOptions;
import net.minecraft.world.item.Item;

import java.util.List;
import java.util.Locale;

/**
 * Visual check of the tangerine and orange woods: every block of each set in a row against a wall, the three stages
 * of the fruit leaves, a grown tree, and the items in the hotbar. Screenshots land in
 * {@code fabric/build/run/clientGameTest/screenshots}.
 */
public final class WoodLookClientTest implements FabricClientGameTest {

    private static final int SETTLE = 20;
    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;
    private static final int Y = 100;
    /** The row of blocks, as setblock suffixes ({@code %s} is the wood); x steps by one from the left. */
    private static final List<String> ROW = List.of("%s_log[axis=y]", "stripped_%s_log[axis=y]", "%s_wood[axis=y]",
            "stripped_%s_wood[axis=y]", "%s_planks", "%s_stairs[facing=south]", "%s_slab", "%s_fence", "%s_fence_gate[facing=south]",
            "%s_door[facing=south,half=lower]", "%s_trapdoor[facing=south,half=bottom,open=false]",
            "%s_button[face=wall,facing=south]", "%s_pressure_plate", "%s_sign[rotation=8]", "%s_wall_sign[facing=south]",
            "%s_hanging_sign[rotation=8,attached=false]", "%s_shelf[facing=south]", "%s_leaves[age=0,persistent=true]",
            "%s_leaves[age=1,persistent=true]", "%s_leaves[age=2,persistent=true]", "%s_sapling", "potted_%s_sapling");

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            world.getConnection().waitForChunksRender();
            world.getServer().runCommand("gamerule spawn_mobs false");
            world.getServer().runCommand("weather clear");
            world.getServer().runCommand("time set noon");
            for (WoodSet set : CozyWoods.ALL) {
                room(world);
                cmd(world, "tp @p 0.5 %d 6.5 180 10", Y);
                context.waitTicks(SETTLE);
                row(world, set.name);
                context.waitTicks(SETTLE);
                world.getConnection().waitForChunksRender();
                shot(context, set.name + "_row", true);
                cmd(world, "tp @p -3.5 %d 3.5 200 20", Y);
                context.waitTicks(SETTLE / 2);
                shot(context, set.name + "_row_left", true);
                cmd(world, "tp @p 7.5 %d 3.5 160 20", Y);
                context.waitTicks(SETTLE / 2);
                shot(context, set.name + "_row_right", true);
                room(world);
                cmd(world, "place feature cozychristmas:%s_tree_wild 0 %d 3", set.name, Y);
                cmd(world, "place feature cozychristmas:%s_tree_grand_wild -9 %d 3", set.name, Y);
                cmd(world, "place feature cozychristmas:%s_tree_wild 8 %d 4", set.name, Y);
                cmd(world, "summon cozychristmas:%s_boat -4 %d 8", set.name, Y);
                cmd(world, "summon cozychristmas:%s_chest_boat 4 %d 8", set.name, Y);
                cmd(world, "tp @p 0.5 %d 11.5 180 -14", Y);
                context.waitTicks(SETTLE);
                world.getConnection().waitForChunksRender();
                shot(context, set.name + "_tree", true);
                int slot = 0;
                for (var item : set.items()) {
                    if (slot < 9 && (item.getId().getPath().endsWith("sign") || item.getId().getPath().endsWith("door")
                            || item.getId().getPath().endsWith("leaves") || item.getId().getPath().endsWith("sapling"))) {
                        cmd(world, "item replace entity @p hotbar.%d with %s", slot++, item.getId());
                    }
                }
                cmd(world, "item replace entity @p hotbar.%d with %s", slot, set.name.equals("orange") ? "cozychristmas:orange" : "cozychristmas:tangerine");
                world.getServer().runCommand("gamemode survival @p");
                context.waitTicks(SETTLE / 2);
                shot(context, set.name + "_hotbar", false);
                world.getServer().runCommand("gamemode creative @p");
            }
        }
    }

    /** A spruce floor and a stone back wall, dirt under where the saplings go. */
    private static void room(TestSingleplayerContext world) {
        cmd(world, "fill -16 %d -12 16 %d 12 minecraft:spruce_planks", Y - 1, Y - 1);
        cmd(world, "fill -16 %d -12 16 %d 12 minecraft:air", Y, Y + 20);
        cmd(world, "fill -12 %d -2 12 %d -2 minecraft:smooth_stone", Y, Y + 4);
        cmd(world, "fill -12 %d -1 12 %d -1 minecraft:smooth_stone", Y + 3, Y + 3);
    }

    private static void row(TestSingleplayerContext world, String wood) {
        for (int i = 0; i < ROW.size(); i++) {
            int x = i - ROW.size() / 2;
            String block = "cozychristmas:" + ROW.get(i).replace("%s", wood);
            if (block.contains("_sapling")) {
                cmd(world, "setblock %d %d -1 minecraft:dirt", x, Y - 1);
            }
            cmd(world, "setblock %d %d -1 %s", x, Y, block);
            if (block.contains("_door")) {
                cmd(world, "setblock %d %d -1 %s", x, Y + 1, block.replace("half=lower", "half=upper"));
            }
        }
    }

    private static void cmd(TestSingleplayerContext world, String format, Object... args) {
        world.getServer().runCommand(String.format(Locale.ROOT, format, args));
    }

    private static void shot(ClientGameTestContext context, String name, boolean hideHud) {
        context.runOnClient(mc -> {
            if (mc.gui.hud.isHidden() != hideHud) {
                mc.gui.hud.toggle();
            }
        });
        context.runOnClient(mc -> mc.gui.toastManager().clear());
        context.waitTicks(2);
        context.takeScreenshot(TestScreenshotOptions.of(name).withSize(WIDTH, HEIGHT));
        context.runOnClient(mc -> {
            if (mc.gui.hud.isHidden()) {
                mc.gui.hud.toggle();
            }
        });
    }
}
