package dev.eliasnvx.cozychristmas.fabric.gametest;

import dev.eliasnvx.cozychristmas.registry.CozyEffects;
import dev.eliasnvx.cozychristmas.registry.CozyItems;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;

import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

/**
 * Visual check of the food: every item in a frame on a wall, chestnuts roasting on a campfire,, the hotbar, and the
 * warmth effect in the inventory. Screenshots land in {@code fabric/build/run/clientGameTest/screenshots}.
 */
public final class FoodLookClientTest implements FabricClientGameTest {

    private static final int SETTLE = 20;
    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;
    private static final int Y = 100;
    private static final int ROASTING = 3;
    /** Off to the side, so its smoke doesn't cover the frames. */
    private static final BlockPos CAMPFIRE = new BlockPos(5, Y, 0);
    private static final List<Supplier<Item>> FOOD = List.of(CozyItems.MUG::get, CozyItems.HOT_COCOA::get,
            CozyItems.MULLED_WINE::get, CozyItems.CHESTNUTS::get, CozyItems.ROASTED_CHESTNUTS::get,
            CozyItems.TANGERINE::get, CozyItems.GINGERBREAD_COOKIE::get);

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            world.getConnection().waitForChunksRender();
            world.getServer().runCommand("gamerule spawn_mobs false");
            world.getServer().runCommand("weather clear");
            world.getServer().runCommand("time set noon");
            room(world);
            cmd(world, "tp @p 0.5 %d 3.5 180 10", Y);
            context.waitTicks(SETTLE);
            world.getServer().runOnServer(server -> {
                ServerLevel level = server.overworld();
                for (int i = 0; i < FOOD.size(); i++) {
                    ItemFrame frame = new ItemFrame(level, new BlockPos(i - FOOD.size() / 2, Y + 1, -1), Direction.SOUTH);
                    frame.setItem(new ItemStack(FOOD.get(i).get()));
                    level.addFreshEntity(frame);
                }
                ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
                if (level.getBlockEntity(CAMPFIRE) instanceof CampfireBlockEntity campfire) {
                    for (int i = 0; i < ROASTING; i++) {
                        campfire.placeFood(level, player, new ItemStack(CozyItems.CHESTNUTS.get()));
                    }
                }
            });
            context.waitTicks(SETTLE);
            world.getConnection().waitForChunksRender();
            shot(context, "food_wall", true);
            cmd(world, "tp @p 5.5 %d 2.2 180 45", Y);
            context.waitTicks(SETTLE / 2);
            shot(context, "food_campfire", true);
            for (int i = 0; i < FOOD.size(); i++) {
                cmd(world, "item replace entity @p hotbar.%d with %s", i, FOOD.get(i).get().builtInRegistryHolder().getRegisteredName());
            }
            world.getServer().runCommand("effect give @p cozychristmas:warm 60 0 true");
            cmd(world, "tp @p 0.5 %d 3.5 180 10", Y);
            world.getServer().runCommand("gamemode survival @p");
            cmd(world, "item replace entity @p hotbar.%d with minecraft:air", FOOD.size());
            context.waitTicks(SETTLE / 2);
            shot(context, "food_hotbar", false);
            context.runOnClient(mc -> {
                if (!mc.player.hasEffect(CozyEffects.WARM.asHolder())) {
                    throw new AssertionError("the player should be warm");
                }
            });
            context.setScreen(() -> new InventoryScreen(Minecraft.getInstance().player));
            context.waitTicks(2);
            shot(context, "food_warm_effect", false);
            context.setScreen(() -> null);
            world.getServer().runCommand("gamemode creative @p");
        }
    }

    /** A spruce floor and back wall, a campfire to one side. */
    private static void room(TestSingleplayerContext world) {
        cmd(world, "fill -6 %d -3 6 %d 6 minecraft:spruce_planks", Y - 1, Y - 1);
        cmd(world, "fill -6 %d -3 6 %d 6 minecraft:air", Y, Y + 5);
        cmd(world, "fill -6 %d -2 6 %d -2 minecraft:stripped_spruce_log", Y, Y + 4);
        cmd(world, "setblock %d %d %d minecraft:campfire[facing=south]", CAMPFIRE.getX(), CAMPFIRE.getY(), CAMPFIRE.getZ());
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
