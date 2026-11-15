package dev.eliasnvx.cozychristmas.fabric.gametest;

import dev.eliasnvx.cozychristmas.goblin.GingerbreadMan;
import dev.eliasnvx.cozychristmas.goblin.PresentGoblin;
import dev.eliasnvx.cozychristmas.registry.CozyEntities;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotOptions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Visual check of the present goblins and gingerbread men (a goblin running off with a sword held up, a pack of four)
 * and of the goblin camp placed by command. Screenshots land in {@code fabric/build/run/clientGameTest/screenshots}.
 */
public final class GoblinsLookClientTest implements FabricClientGameTest {

    private static final int SETTLE = 20;
    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;
    private static final int STAGE_Y = 100;
    private static final BlockPos CAMP = new BlockPos(-160, 0, 160);
    private static final double SEARCH = 32.0;

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            world.getConnection().waitForChunksRender();
            world.getServer().runCommand("time set noon");
            world.getServer().runCommand("weather clear");
            world.getServer().runCommand("gamerule spawn_mobs false");
            lineup(context, world);
            camp(context, world);
        }
    }

    private static void lineup(ClientGameTestContext context, TestSingleplayerContext world) {
        world.getServer().runCommand("fill -8 " + (STAGE_Y - 1) + " -4 8 " + (STAGE_Y - 1) + " 8 minecraft:snow_block");
        world.getServer().runCommand("fill -8 " + STAGE_Y + " -4 8 " + (STAGE_Y + 5) + " 8 minecraft:air");
        world.getServer().runCommand("tp @p 0 " + STAGE_Y + " -1 0 20");
        context.waitTicks(SETTLE);
        world.getServer().runOnServer(server -> {
            ServerLevel level = server.overworld();
            posed(level, CozyEntities.PRESENT_GOBLIN.get(), -2.0, 3.0, goblin -> { });
            posed(level, CozyEntities.PRESENT_GOBLIN.get(), -0.6, 3.2, goblin ->
                    goblin.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD)));
            for (int i = 0; i < 4; i++) {
                posed(level, CozyEntities.GINGERBREAD_MAN.get(), 0.9 + (i % 2) * 0.9, 2.6 + (i / 2) * 1.1, man -> { });
            }
        });
        context.waitTicks(SETTLE);
        context.runOnClient(mc -> check(mc.level.getEntities(mc.player, mc.player.getBoundingBox().inflate(8),
                entity -> entity instanceof PresentGoblin goblin && goblin.hasLoot()).size() == 1, "one goblin holds loot"));
        shot(context, "goblins_lineup");
        world.getServer().runCommand("kill @e[type=!player]");
    }

    private static void camp(ClientGameTestContext context, TestSingleplayerContext world) {
        world.getServer().runCommand("gamemode spectator @a");
        world.getServer().runCommand(String.format(Locale.ROOT, "tp @p %d 120 %d", CAMP.getX(), CAMP.getZ()));
        context.waitTicks(SETTLE);
        world.getConnection().waitForChunksRender();
        world.getServer().runCommand(String.format(Locale.ROOT, "place structure cozychristmas:goblin_camp %d 0 %d",
                CAMP.getX(), CAMP.getZ()));
        context.waitTicks(SETTLE);
        List<Vec3> goblins = new ArrayList<>();
        List<Vec3> gingerbread = new ArrayList<>();
        world.getServer().runOnServer(server -> {
            ServerLevel level = server.overworld();
            AABB area = AABB.ofSize(Vec3.atCenterOf(CAMP.atY(level.getSeaLevel())), SEARCH * 2, 512, SEARCH * 2);
            level.getEntitiesOfClass(PresentGoblin.class, area, Entity::isAlive).forEach(e -> goblins.add(e.position()));
            level.getEntitiesOfClass(GingerbreadMan.class, area, Entity::isAlive).forEach(e -> gingerbread.add(e.position()));
        });
        check(goblins.size() == 3, "the camp should have three goblins, found " + goblins.size());
        check(gingerbread.size() == 4, "the camp should have four gingerbread men, found " + gingerbread.size());
        Vec3 center = goblins.getFirst();
        look(world, center.add(-11, 9, -11), center);
        context.waitTicks(SETTLE * 2);
        world.getConnection().waitForChunksRender();
        shot(context, "goblin_camp_overview");
        look(world, center.add(6, 3, -7), center);
        context.waitTicks(SETTLE);
        shot(context, "goblin_camp_low");
        world.getServer().runCommand("gamemode creative @a");
    }

    private static void look(TestSingleplayerContext world, Vec3 from, Vec3 at) {
        world.getServer().runCommand(String.format(Locale.ROOT, "tp @p %.2f %.2f %.2f facing %.2f %.2f %.2f",
                from.x, from.y, from.z, at.x, at.y, at.z));
    }

    /** A mob with no AI at (x, STAGE_Y, z) facing the camera (north). */
    private static <T extends Mob> void posed(ServerLevel level, EntityType<T> type, double x, double z, Consumer<T> setup) {
        T mob = type.create(level, EntitySpawnReason.COMMAND);
        check(mob != null, "couldn't create " + type);
        mob.snapTo(x, STAGE_Y, z, 180.0F, 0.0F);
        mob.setYHeadRot(180.0F);
        mob.setYBodyRot(180.0F);
        mob.setNoAi(true);
        setup.accept(mob);
        level.addFreshEntity(mob);
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

    private static void check(boolean ok, String message) {
        if (!ok) {
            throw new AssertionError(message);
        }
    }
}
