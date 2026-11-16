package dev.eliasnvx.cozychristmas.fabric.gametest;

import dev.eliasnvx.cozychristmas.registry.CozyBlocks;
import dev.eliasnvx.cozychristmas.registry.CozyEntities;
import dev.eliasnvx.cozychristmas.season.ChristmasSeason;
import dev.eliasnvx.cozychristmas.sleigh.SantasSleigh;
import dev.eliasnvx.cozychristmas.workshop.Elf;
import dev.eliasnvx.cozychristmas.workshop.Santa;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Visual check of Santa's sleigh on Christmas Eve: parked with the team harnessed, the sack half full, Santa and elves
 * by it; then at night taking off and climbing its spiral with Santa aboard. Screenshots land in
 * {@code fabric/build/run/clientGameTest/screenshots}.
 */
public final class SleighLookClientTest implements FabricClientGameTest {

    private static final int SETTLE = 20;
    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;
    private static final int Y = 100;
    private static final int LOADED = 14;
    /** Ticks after takeoff for each flight shot: lifting off the runway, then partway up the spiral. */
    private static final int TAKEOFF_SHOT = 70;
    private static final int CLIMB_SHOT = 260;
    private static final double CHASE = 14.0;

    @Override
    public void runTest(ClientGameTestContext context) {
        ChristmasSeason.setClock(Clock.fixed(LocalDate.of(2026, 12, 24).atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC));
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            world.getConnection().waitForChunksRender();
            world.getServer().runCommand("gamerule spawn_mobs false");
            world.getServer().runCommand("weather clear");
            world.getServer().runCommand("time set noon");
            cmd(world, "fill -40 %d -60 40 %d 30 minecraft:snow_block", Y - 1, Y - 1);
            cmd(world, "fill -40 %d -60 40 %d 30 minecraft:air", Y, Y + 12);
            cmd(world, "tp @p 6.5 %d 7.5", Y);
            context.waitTicks(SETTLE);
            world.getServer().runOnServer(server -> {
                ServerLevel level = server.overworld();
                SantasSleigh sleigh = CozyEntities.SANTAS_SLEIGH.get().create(level, EntitySpawnReason.COMMAND);
                sleigh.snapTo(0.5, Y, 0.5, 180.0F, 0.0F);
                level.addFreshEntity(sleigh);
                for (int i = 0; i < LOADED; i++) {
                    sleigh.loadPresent(null);
                }
                posed(level, CozyEntities.SANTA.get().create(level, EntitySpawnReason.COMMAND), 2.3, 1.0, 120.0F, false);
                posed(level, CozyEntities.ELF.get().create(level, EntitySpawnReason.COMMAND), 2.0, -1.2, 90.0F, true);
                posed(level, CozyEntities.ELF.get().create(level, EntitySpawnReason.COMMAND), -1.9, 0.2, -90.0F, true);
            });
            context.waitTicks(SETTLE * 2); // the sleigh harnesses its team on its next check
            look(world, new Vec3(5.5, Y, 2.5), new Vec3(0.0, Y + 0.6, -1.0));
            context.waitTicks(SETTLE);
            world.getConnection().waitForChunksRender();
            shot(context, "sleigh_parked");
            look(world, new Vec3(-3.5, Y + 1, -7.5), new Vec3(0.5, Y + 0.5, -1.5));
            context.waitTicks(SETTLE / 2);
            shot(context, "sleigh_team");

            world.getServer().runOnServer(server -> {
                // night and the takeoff in one go: left alone, the sleigh would leave on its own next check
                server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "time set midnight");
                ServerLevel level = server.overworld();
                SantasSleigh sleigh = sleigh(level);
                level.getEntitiesOfClass(Mob.class, sleigh.getBoundingBox().inflate(6.0)).forEach(mob -> mob.setNoAi(false));
                if (!sleigh.tryDepart(level, true)) {
                    throw new AssertionError("the sleigh should take off");
                }
            });
            context.waitTicks(TAKEOFF_SHOT);
            chase(context, world);
            shot(context, "sleigh_takeoff");
            context.waitTicks(CLIMB_SHOT - TAKEOFF_SHOT);
            chase(context, world);
            shot(context, "sleigh_climb");
        } finally {
            ChristmasSeason.setClock(Clock.systemDefaultZone());
        }
    }

    private static void posed(ServerLevel level, Mob mob, double x, double z, float yaw, boolean carrying) {
        mob.snapTo(x, Y, z, yaw, 0.0F);
        mob.setYHeadRot(yaw);
        mob.setYBodyRot(yaw);
        mob.setNoAi(true);
        if (carrying) {
            mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(CozyBlocks.PRESENT_ITEM.get()));
        }
        level.addFreshEntity(mob);
    }

    private static SantasSleigh sleigh(ServerLevel level) {
        List<SantasSleigh> found = level.getEntitiesOfClass(SantasSleigh.class, new AABB(-200, 0, -200, 200, 320, 200));
        if (found.isEmpty()) {
            throw new AssertionError("no sleigh");
        }
        return found.getFirst();
    }

    /** Puts the camera beside the flying sleigh, a little below and behind, looking at it. */
    private static void chase(ClientGameTestContext context, TestSingleplayerContext world) {
        AtomicReference<Vec3> at = new AtomicReference<>();
        AtomicReference<Float> yaw = new AtomicReference<>();
        world.getServer().runOnServer(server -> {
            SantasSleigh sleigh = sleigh(server.overworld());
            at.set(sleigh.position());
            yaw.set(sleigh.getYRot());
        });
        Vec3 side = Vec3.directionFromRotation(0.0F, yaw.get() + 120.0F).scale(CHASE);
        Vec3 from = at.get().add(side.x, -3.0, side.z);
        world.getServer().runCommand("gamemode spectator @p");
        look(world, from, at.get().add(0.0, 0.5, 0.0));
        context.waitTicks(4);
    }

    /** Eyes (not feet) at {@code at}: yaw and pitch worked out here. */
    private static void look(TestSingleplayerContext world, Vec3 from, Vec3 at) {
        Vec3 delta = at.subtract(from.add(0.0, 1.62, 0.0));
        double yaw = Math.toDegrees(Math.atan2(-delta.x, delta.z));
        double pitch = Math.toDegrees(-Math.atan2(delta.y, Math.sqrt(delta.x * delta.x + delta.z * delta.z)));
        cmd(world, "tp @p %.2f %.2f %.2f %.1f %.1f", from.x, from.y, from.z, yaw, pitch);
    }

    private static void cmd(TestSingleplayerContext world, String format, Object... args) {
        world.getServer().runCommand(String.format(Locale.ROOT, format, args));
    }

    private static void shot(ClientGameTestContext context, String name) {
        context.runOnClient(mc -> {
            if (!mc.gui.hud.isHidden()) {
                mc.gui.hud.toggle();
            }
            mc.gui.toastManager().clear();
        });
        context.waitTicks(2);
        context.takeScreenshot(TestScreenshotOptions.of(name).withSize(WIDTH, HEIGHT));
        context.runOnClient(mc -> {
            if (mc.gui.hud.isHidden()) {
                mc.gui.hud.toggle();
            }
        });
    }
}
