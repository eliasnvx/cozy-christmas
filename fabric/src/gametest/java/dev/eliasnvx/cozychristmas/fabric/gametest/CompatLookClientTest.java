package dev.eliasnvx.cozychristmas.fabric.gametest;

import dev.eliasnvx.cozychristmas.present.PresentBlockEntity;
import dev.eliasnvx.cozychristmas.present.PresentContents;
import dev.eliasnvx.cozychristmas.registry.CozyBlocks;
import dev.eliasnvx.cozychristmas.stocking.StockingBlock;
import dev.eliasnvx.cozychristmas.stocking.StockingBlockEntity;
import dev.eliasnvx.cozychristmas.tree.OrnamentBlock;
import dev.eliasnvx.cozychristmas.tree.OrnamentColor;
import dev.eliasnvx.cozychristmas.tree.TreeStarBlockEntity;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotOptions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.Vec3;

import java.util.Locale;
import java.util.Optional;

/**
 * Visual check of the compat plugins, only in a compat run ({@code ./gradlew :fabric:runClientGameTest
 * -Pcompat=jei,jade} or {@code -Pcompat=rei}); without those mods it does nothing. Jade's tooltip on a stocking, a
 * tree star and a wrapped present; JEI's or REI's wrapping category and an information page. Screenshots land in
 * {@code fabric/build/run/clientGameTest/screenshots}.
 */
public final class CompatLookClientTest implements FabricClientGameTest {

    private static final int SETTLE = 20;
    private static final int Y = 100;
    private static final BlockPos FIREPLACE = new BlockPos(-3, Y, -3);
    private static final BlockPos STOCKING = FIREPLACE.above().south();
    private static final BlockPos TRUNK = new BlockPos(0, Y, -3);
    private static final BlockPos STAR = TRUNK.above(3);
    private static final BlockPos PRESENT = new BlockPos(3, Y, -3);
    private static final int ORNAMENTS = 4;
    private static final double EYES = 1.62;

    @Override
    public void runTest(ClientGameTestContext context) {
        FabricLoader loader = FabricLoader.getInstance();
        boolean jade = loader.isModLoaded("jade");
        boolean jei = loader.isModLoaded("jei");
        boolean rei = loader.isModLoaded("roughlyenoughitems");
        if (!jade && !jei && !rei) {
            return; // not a compat run
        }
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            world.getConnection().waitForChunksRender();
            world.getServer().runCommand("gamerule spawn_mobs false");
            world.getServer().runCommand("weather clear");
            world.getServer().runCommand("time set noon");
            cmd(world, "fill -8 %d -8 8 %d 6 minecraft:snow_block", Y - 1, Y - 1);
            cmd(world, "fill -8 %d -8 8 %d 6 minecraft:air", Y, Y + 8);
            cmd(world, "tp @p 0.5 %d 2.5 180 0", Y);
            context.waitTicks(SETTLE);
            world.getServer().runOnServer(server -> build(server.overworld(), server.getPlayerList().getPlayers().getFirst()));
            context.waitTicks(SETTLE);
            world.getConnection().waitForChunksRender();
            if (jade) {
                look(context, world, new Vec3(-2.5, Y, 0.5), new Vec3(-2.5, Y + 1.5, -1.8), "compat_jade_stocking");
                look(context, world, new Vec3(0.5, Y + 1, 0.5), new Vec3(0.5, Y + 3.4, -2.5), "compat_jade_tree");
                look(context, world, new Vec3(3.5, Y, -0.5), new Vec3(3.5, Y + 0.4, -2.5), "compat_jade_present");
            }
            if (jei || rei) {
                // the item list keeps clear of the cosmetics panel right of the inventory
                context.setScreen(() -> new InventoryScreen(Minecraft.getInstance().player));
                context.waitTicks(SETTLE / 2);
                shot(context, jei ? "compat_jei_inventory" : "compat_rei_inventory");
                context.setScreen(() -> null);
                context.waitTicks(2);
            }
            if (jei) {
                viewer(context, JeiShots::wrapping, "compat_jei_wrapping");
                viewer(context, JeiShots::chestnuts, "compat_jei_chestnuts");
            }
            if (rei) {
                viewer(context, ReiShots::wrapping, "compat_rei_wrapping");
                viewer(context, ReiShots::treeStar, "compat_rei_tree_star");
            }
        }
    }

    /** A stocking over a fireplace, a little tree with a star and too few ornaments, a present wrapped by the player. */
    private static void build(ServerLevel level, ServerPlayer player) {
        level.setBlock(FIREPLACE, CozyBlocks.FIREPLACE.get().defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(FIREPLACE.above(), Blocks.BRICKS.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(STOCKING, CozyBlocks.STOCKING.get().defaultBlockState()
                .setValue(StockingBlock.FACING, Direction.SOUTH), Block.UPDATE_ALL);
        if (level.getBlockEntity(STOCKING) instanceof StockingBlockEntity stocking) {
            stocking.hang(player, new ItemStack(CozyBlocks.STOCKING_ITEM.get()), StockingBlockEntity.morning(level));
        }
        for (int y = 0; y < 2; y++) {
            level.setBlock(TRUNK.above(y), Blocks.SPRUCE_LOG.defaultBlockState(), Block.UPDATE_ALL);
        }
        level.setBlock(TRUNK.above(2), Blocks.SPRUCE_LEAVES.defaultBlockState()
                .setValue(LeavesBlock.PERSISTENT, true), Block.UPDATE_ALL);
        for (Direction side : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
            level.setBlock(TRUNK.above().relative(side), Blocks.SPRUCE_LEAVES.defaultBlockState()
                    .setValue(LeavesBlock.PERSISTENT, true), Block.UPDATE_ALL);
        }
        OrnamentColor[] colors = OrnamentColor.values();
        Direction[] sides = {Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.NORTH};
        for (int i = 0; i < ORNAMENTS; i++) {
            level.setBlock(TRUNK.above().relative(sides[i]).relative(sides[i]), CozyBlocks.ORNAMENTS.get(colors[i]).get()
                    .defaultBlockState().setValue(OrnamentBlock.FACE, AttachFace.WALL).setValue(OrnamentBlock.FACING, sides[i]),
                    Block.UPDATE_ALL);
        }
        level.setBlock(STAR, CozyBlocks.TREE_STAR.get().defaultBlockState(), Block.UPDATE_ALL);
        if (level.getBlockEntity(STAR) instanceof TreeStarBlockEntity star) {
            star.putUp(player, StockingBlockEntity.morning(level));
        }
        level.setBlock(PRESENT, CozyBlocks.PRESENT.get().defaultBlockState(), Block.UPDATE_ALL);
        if (level.getBlockEntity(PRESENT) instanceof PresentBlockEntity present) {
            present.setContents(new PresentContents.Gift(new ItemStackTemplate(Items.DIAMOND),
                    Optional.of(PresentContents.Giver.of(player))));
        }
    }

    /** Turns the player's eyes (not feet) at {@code at}: yaw and pitch worked out here, from eye height. */
    private static void look(ClientGameTestContext context, TestSingleplayerContext world, Vec3 from, Vec3 at, String name) {
        Vec3 eyes = from.add(0, EYES, 0);
        Vec3 delta = at.subtract(eyes);
        double yaw = Math.toDegrees(Math.atan2(-delta.x, delta.z));
        double pitch = Math.toDegrees(-Math.atan2(delta.y, Math.sqrt(delta.x * delta.x + delta.z * delta.z)));
        cmd(world, "tp @p %.2f %.2f %.2f %.1f %.1f", from.x, from.y, from.z, yaw, pitch);
        context.waitTicks(SETTLE);
        shot(context, name);
    }

    private static void viewer(ClientGameTestContext context, Runnable open, String name) {
        context.runOnClient(mc -> open.run());
        context.waitTicks(SETTLE / 2);
        shot(context, name);
        context.setScreen(() -> null);
        context.waitTicks(2);
    }

    private static void cmd(TestSingleplayerContext world, String format, Object... args) {
        world.getServer().runCommand(String.format(Locale.ROOT, format, args));
    }

    /** At the window's own size: Jade's tooltip and the viewers are laid out for it. */
    private static void shot(ClientGameTestContext context, String name) {
        context.runOnClient(mc -> mc.gui.toastManager().clear());
        context.waitTicks(2);
        context.takeScreenshot(TestScreenshotOptions.of(name));
    }
}
