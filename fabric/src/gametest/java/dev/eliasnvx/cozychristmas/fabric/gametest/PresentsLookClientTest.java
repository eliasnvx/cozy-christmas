package dev.eliasnvx.cozychristmas.fabric.gametest;

import dev.eliasnvx.cozychristmas.client.present.WrappingTableScreen;
import dev.eliasnvx.cozychristmas.mixin.client.AbstractContainerScreenAccessor;
import dev.eliasnvx.cozychristmas.present.PresentBlock;
import dev.eliasnvx.cozychristmas.present.PresentBlockEntity;
import dev.eliasnvx.cozychristmas.present.PresentContents;
import dev.eliasnvx.cozychristmas.present.PresentDesign;
import dev.eliasnvx.cozychristmas.present.WrappingTableMenu;
import dev.eliasnvx.cozychristmas.registry.CozyBlocks;
import dev.eliasnvx.cozychristmas.registry.CozyComponents;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

/**
 * Visual check of presents: the four designs, a ticking prank (drawn shaking), the confetti of a boom and the wrapping
 * table screen with a wrapped gift. Screenshots land in {@code fabric/build/run/clientGameTest/screenshots}.
 */
public class PresentsLookClientTest implements FabricClientGameTest {

    private static final int SETTLE = 20;
    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;
    /** The player stands here looking south (+z) and a little down; presents go in a row in front. */
    private static final BlockPos STAND = new BlockPos(0, 100, 0);

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            world.getConnection().waitForChunksRender();
            world.getServer().runCommand("time set noon");
            world.getServer().runCommand("weather clear");
            world.getServer().runCommand("gamerule spawn_mobs false");
            // a snowy stage in the air, so the shot doesn't depend on the terrain
            world.getServer().runCommand("fill -12 99 -12 12 99 12 minecraft:snow_block");
            world.getServer().runCommand("fill -12 100 -12 12 104 12 minecraft:air");
            world.getServer().runCommand("tp @p 0 100 0 0 30");
            context.waitTicks(SETTLE);

            // the four designs in a row
            world.getServer().runOnServer(server -> {
                PresentDesign[] designs = PresentDesign.values();
                for (int i = 0; i < designs.length; i++) {
                    BlockPos pos = STAND.offset(-3 + 2 * i, 0, 4);
                    server.overworld().setBlockAndUpdate(pos, CozyBlocks.PRESENT.get().defaultBlockState()
                            .setValue(PresentBlock.DESIGN, designs[i]));
                }
            });
            context.waitTicks(SETTLE);
            shot(context, "presents_designs");

            // a prank next to the player: it ticks, and the renderer shakes it
            BlockPos prank = STAND.offset(0, 0, 2);
            world.getServer().runOnServer(server -> {
                server.overworld().setBlockAndUpdate(prank, CozyBlocks.PRESENT.get().defaultBlockState()
                        .setValue(PresentBlock.DESIGN, PresentDesign.GOLD));
                ((PresentBlockEntity) server.overworld().getBlockEntity(prank)).setContents(new PresentContents.Prank(Optional.empty()));
            });
            context.waitTicks(SETTLE * 2);
            context.runOnClient(mc -> check(mc.level.getBlockState(prank).getValue(PresentBlock.TICKING),
                    "a prank next to the player should tick on the client"));
            shot(context, "present_ticking");

            // open it: confetti and snow
            world.getServer().runCommand("gamemode survival @a");
            world.getServer().runOnServer(server -> ((PresentBlockEntity) server.overworld().getBlockEntity(prank)).open(player(server)));
            context.waitTicks(3);
            shot(context, "present_boom");
            context.waitTicks(SETTLE * 2);
            shot(context, "present_boom_after");
            world.getServer().runCommand("gamemode creative @a");
            // the boom pushed the player back: return to the spot
            world.getServer().runCommand("tp @p 0 100 0 0 30");

            // the wrapping table: wrap diamonds in blue paper
            BlockPos table = STAND.offset(2, 0, 2);
            world.getServer().runOnServer(server -> {
                server.overworld().setBlockAndUpdate(table, CozyBlocks.WRAPPING_TABLE.get().defaultBlockState());
                ServerPlayer player = player(server);
                player.getInventory().clearContent();
                player.getInventory().add(new ItemStack(Items.DIAMOND, 5));
                player.getInventory().add(new ItemStack(Items.PAPER, 4));
                server.overworld().getBlockState(table).useWithoutItem(server.overworld(), player,
                        new BlockHitResult(Vec3.atCenterOf(table), Direction.UP, table, false));
            });
            context.waitTicks(SETTLE / 2);
            context.runOnClient(mc -> {
                check(mc.gui.screen() instanceof WrappingTableScreen, "the wrapping table screen should be open");
                WrappingTableMenu menu = ((WrappingTableScreen) mc.gui.screen()).getMenu();
                quickMove(mc, menu, Items.DIAMOND);
                quickMove(mc, menu, Items.PAPER);
                menu.clickMenuButton(mc.player, PresentDesign.BLUE.ordinal());
                mc.gameMode.handleInventoryButtonClick(menu.containerId, PresentDesign.BLUE.ordinal());
            });
            context.waitTicks(SETTLE / 2);
            context.runOnClient(mc -> mc.gui.toastManager().clear());
            context.takeScreenshot("wrapping_table");
            double[] result = context.computeOnClient(mc -> {
                AbstractContainerScreenAccessor screen = (AbstractContainerScreenAccessor) mc.gui.screen();
                int scale = mc.getWindow().getGuiScale();
                return new double[]{(screen.cozychristmas$leftPos() + WrappingTableMenu.RESULT_X + 8) * scale,
                        (screen.cozychristmas$topPos() + WrappingTableMenu.RESULT_Y + 8) * scale};
            });
            context.getInput().setCursorPos(result[0], result[1]);
            context.waitTicks(2);
            context.takeScreenshot("wrapping_table_tooltip");
            context.getInput().setCursorPos(0, 0);
            context.runOnClient(mc -> {
                WrappingTableMenu menu = ((WrappingTableScreen) mc.gui.screen()).getMenu();
                mc.gameMode.handleContainerInput(menu.containerId, WrappingTableMenu.RESULT_SLOT, 0, ContainerInput.QUICK_MOVE, mc.player);
            });
            context.waitTicks(SETTLE / 2);
            world.getServer().runOnServer(server -> {
                ServerPlayer player = player(server);
                boolean wrapped = false;
                for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
                    if (stack.get(CozyComponents.PRESENT_CONTENTS.get()) instanceof PresentContents.Gift gift
                            && gift.item().count() == 5) {
                        wrapped = true;
                    }
                }
                check(wrapped, "taking the result should give a present with the five diamonds");
                check(player.getInventory().countItem(Items.DIAMOND) == 0, "the diamonds should be wrapped");
            });
            context.setScreen(() -> null);
        }
    }

    private static void quickMove(Minecraft mc, WrappingTableMenu menu, Item item) {
        for (Slot slot : menu.slots) {
            if (slot.index > WrappingTableMenu.RESULT_SLOT && slot.getItem().is(item)) {
                mc.gameMode.handleContainerInput(menu.containerId, slot.index, 0, ContainerInput.QUICK_MOVE, mc.player);
                return;
            }
        }
        throw new AssertionError("no " + item + " in the inventory");
    }

    private static ServerPlayer player(MinecraftServer server) {
        return server.getPlayerList().getPlayers().getFirst();
    }

    private static void check(boolean ok, String message) {
        if (!ok) {
            throw new AssertionError(message);
        }
    }

    /** A screenshot without the HUD. */
    private static void shot(ClientGameTestContext context, String name) {
        context.runOnClient(mc -> {
            if (!mc.gui.hud.isHidden()) {
                mc.gui.hud.toggle();
            }
        });
        context.waitTicks(1);
        context.takeScreenshot(TestScreenshotOptions.of(name).withSize(WIDTH, HEIGHT));
        context.runOnClient(mc -> mc.gui.hud.toggle());
    }
}
