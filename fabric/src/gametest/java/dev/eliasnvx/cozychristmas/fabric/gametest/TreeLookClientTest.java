package dev.eliasnvx.cozychristmas.fabric.gametest;

import dev.eliasnvx.cozychristmas.karma.KarmaManager;
import dev.eliasnvx.cozychristmas.registry.CozyBlocks;
import dev.eliasnvx.cozychristmas.season.ChristmasSeason;
import dev.eliasnvx.cozychristmas.stocking.StockingBlockEntity;
import dev.eliasnvx.cozychristmas.tree.OrnamentBlock;
import dev.eliasnvx.cozychristmas.tree.OrnamentColor;
import dev.eliasnvx.cozychristmas.tree.TreeStarBlock;
import dev.eliasnvx.cozychristmas.tree.TreeStarBlockEntity;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotOptions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Visual check of the Christmas tree: a spruce built in tiers, ornaments on and under its branches, the star on top,
 * and the presents it brings on Christmas mornings (by the real rule, the morning moved back between them), by day,
 * at night, up close; the status the star gives its owner, and the items in the hotbar. Screenshots land in
 * {@code fabric/build/run/clientGameTest/screenshots}.
 */
public final class TreeLookClientTest implements FabricClientGameTest {

    private static final int SETTLE = 20;
    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;
    private static final int Y = 100;
    private static final int TRUNK_HEIGHT = 9;
    /** (height above the ground, reach, diamond): the plaza tree's tiers. */
    private static final int[][] TIERS = {{1, 3, 4}, {2, 3, 4}, {3, 2, 3}, {4, 2, 3}, {5, 2, 2}, {6, 1, 2}, {7, 1, 1}, {8, 1, 1}};
    private static final double SIDE_CHANCE = 0.18;
    private static final double UNDER_CHANCE = 0.3;
    private static final long SEED = 1225L;
    private static final int PRESENTS = 3;
    private static final Direction[] SIDES = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};

    @Override
    public void runTest(ClientGameTestContext context) {
        ChristmasSeason.setClock(Clock.fixed(LocalDate.of(2026, 12, 20).atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC));
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            world.getConnection().waitForChunksRender();
            world.getServer().runCommand("gamerule spawn_mobs false");
            world.getServer().runCommand("weather clear");
            world.getServer().runCommand("time set noon");
            cmd(world, "fill -10 %d -10 10 %d 14 minecraft:snow_block", Y - 1, Y - 1);
            cmd(world, "fill -10 %d -10 10 %d 14 minecraft:air", Y, Y + 14);
            cmd(world, "tp @p 0.5 %d 12.5 180 -10", Y);
            context.waitTicks(SETTLE);
            world.getServer().runOnServer(server -> {
                ServerLevel level = server.overworld();
                ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
                BlockPos star = build(level);
                TreeStarBlockEntity entity = (TreeStarBlockEntity) level.getBlockEntity(star);
                long morning = StockingBlockEntity.morning(level);
                for (int i = 0; i < PRESENTS; i++) {
                    entity.putUp(player, morning - 1);
                    KarmaManager.takeTreeGift(player, morning - 1);
                    if (entity.tryGift(level) == null) {
                        throw new AssertionError("the tree should bring present " + (i + 1));
                    }
                }
            });
            context.waitTicks(SETTLE);
            world.getConnection().waitForChunksRender();
            look(world, "0.5 %d 13.5 facing 0.5 %d 0.5", Y + 4, Y + 5);
            context.waitTicks(SETTLE);
            shot(context, "tree_day", true);
            look(world, "3.5 %d 4.5 facing 0.5 %d 0.5", Y + 3, Y + 2);
            context.waitTicks(SETTLE / 2);
            shot(context, "tree_close", true);
            // low, from outside the lowest tier, under its edge
            look(world, "0.5 %d 6.5 facing 0.5 %.1f 1.0", Y, Y + 0.3);
            context.waitTicks(SETTLE / 2);
            shot(context, "tree_presents", true);
            world.getServer().runCommand("time set midnight");
            look(world, "0.5 %d 13.5 facing 0.5 %d 0.5", Y + 4, Y + 5);
            context.waitTicks(SETTLE);
            shot(context, "tree_night", true);
            world.getServer().runCommand("time set noon");

            world.getServer().runOnServer(server -> {
                ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
                player.sendOverlayMessage(TreeStarBlock.statusMessage(server.overworld(), new BlockPos(0, Y + TRUNK_HEIGHT + 1, 0)));
            });
            cmd(world, "item replace entity @p hotbar.0 with cozychristmas:tree_star");
            OrnamentColor[] colors = OrnamentColor.values();
            for (int i = 0; i < colors.length; i++) {
                cmd(world, "item replace entity @p hotbar.%d with cozychristmas:%s", i + 1, colors[i].blockName());
            }
            context.waitTicks(4);
            shot(context, "tree_hotbar", false);
        } finally {
            ChristmasSeason.setClock(Clock.systemDefaultZone());
        }
    }

    /** The tree at (0, Y, 0), ornaments hung at random but always the same; returns the star's position. */
    private static BlockPos build(ServerLevel level) {
        BlockState log = Blocks.SPRUCE_LOG.defaultBlockState();
        BlockState leaves = Blocks.SPRUCE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
        for (int y = 0; y < TRUNK_HEIGHT; y++) {
            level.setBlock(new BlockPos(0, Y + y, 0), log, Block.UPDATE_ALL);
        }
        List<BlockPos> leafSpots = new ArrayList<>();
        for (int[] tier : TIERS) {
            for (int dx = -tier[1]; dx <= tier[1]; dx++) {
                for (int dz = -tier[1]; dz <= tier[1]; dz++) {
                    if ((dx != 0 || dz != 0) && Math.abs(dx) + Math.abs(dz) <= tier[2]) {
                        BlockPos pos = new BlockPos(dx, Y + tier[0], dz);
                        level.setBlock(pos, leaves, Block.UPDATE_ALL);
                        leafSpots.add(pos);
                    }
                }
            }
        }
        BlockPos top = new BlockPos(0, Y + TRUNK_HEIGHT, 0);
        level.setBlock(top, leaves, Block.UPDATE_ALL);
        BlockPos star = top.above();
        level.setBlock(star, CozyBlocks.TREE_STAR.get().defaultBlockState(), Block.UPDATE_ALL);

        Random random = new Random(SEED);
        OrnamentColor[] colors = OrnamentColor.values();
        for (BlockPos leaf : leafSpots) {
            for (Direction side : SIDES) {
                BlockPos spot = leaf.relative(side);
                if (level.getBlockState(spot).isAir() && random.nextDouble() < SIDE_CHANCE) {
                    hang(level, spot, colors[random.nextInt(colors.length)], AttachFace.WALL, side);
                }
            }
            BlockPos under = leaf.below();
            if (under.getY() > Y && level.getBlockState(under).isAir() && random.nextDouble() < UNDER_CHANCE) {
                hang(level, under, colors[random.nextInt(colors.length)], AttachFace.CEILING, Direction.NORTH);
            }
        }
        return star;
    }

    private static void hang(ServerLevel level, BlockPos pos, OrnamentColor color, AttachFace face, Direction facing) {
        BlockState state = CozyBlocks.ORNAMENTS.get(color).get().defaultBlockState()
                .setValue(OrnamentBlock.FACE, face).setValue(OrnamentBlock.FACING, facing);
        if (state.canSurvive(level, pos)) {
            level.setBlock(pos, state, Block.UPDATE_ALL);
        }
    }

    private static void look(TestSingleplayerContext world, String format, Object fromY, Object atY) {
        cmd(world, "tp @p " + format, fromY, atY);
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
        // with the HUD at the window's own size: the HUD is laid out for the window, not a resized shot
        TestScreenshotOptions options = TestScreenshotOptions.of(name);
        context.takeScreenshot(hideHud ? options.withSize(WIDTH, HEIGHT) : options);
        context.runOnClient(mc -> {
            if (mc.gui.hud.isHidden()) {
                mc.gui.hud.toggle();
            }
        });
    }
}
