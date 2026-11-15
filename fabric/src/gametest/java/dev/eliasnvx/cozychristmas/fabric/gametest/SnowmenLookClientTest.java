package dev.eliasnvx.cozychristmas.fabric.gametest;

import dev.eliasnvx.cozychristmas.registry.CozyEntities;
import dev.eliasnvx.cozychristmas.registry.CozyItems;
import dev.eliasnvx.cozychristmas.snowman.EvilSnowman;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;

/**
 * Visual check of the evil snowmen (one mid-throw, a snow golem for scale), by day and at night (the glowing eyes),
 * and an ice snowball lying in the snow. Screenshots land in {@code fabric/build/run/clientGameTest/screenshots}.
 */
public final class SnowmenLookClientTest implements FabricClientGameTest {

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
            world.getServer().runCommand("time set noon");
            cmd(world, "fill -8 %d -4 8 %d 10 minecraft:snow_block", Y - 1, Y - 1);
            cmd(world, "fill -8 %d -4 8 %d 10 minecraft:air", Y, Y + 6);
            cmd(world, "tp @p 0.5 %d -1.5 0 12", Y);
            context.waitTicks(SETTLE);
            world.getServer().runOnServer(server -> {
                ServerLevel level = server.overworld();
                posed(level, CozyEntities.EVIL_SNOWMAN.get(), -1.5, 4.5);
                posed(level, CozyEntities.EVIL_SNOWMAN.get(), 0.7, 5.0);
                posed(level, CozyEntities.EVIL_SNOWMAN.get(), 2.8, 4.2);
                posed(level, EntityTypes.SNOW_GOLEM, 4.8, 5.5);
                ItemEntity ball = new ItemEntity(level, 1.3, Y + 0.1, 3.0, new ItemStack(CozyItems.ICE_SNOWBALL.get()));
                ball.setNeverPickUp();
                        level.addFreshEntity(ball);
            });
            context.waitTicks(SETTLE);
            world.getConnection().waitForChunksRender();
            throwPose(context);
            shot(context, "snowmen_day");
            world.getServer().runCommand("time set midnight");
            context.waitTicks(SETTLE);
            throwPose(context);
            shot(context, "snowmen_night");
            cmd(world, "tp @p 2.0 %d 1.2 25 22", Y);
            world.getServer().runCommand("time set noon");
            context.waitTicks(SETTLE);
            shot(context, "snowmen_close");
        }
    }

    /** The middle snowman just threw: its client entity gets the throw event. */
    private static void throwPose(ClientGameTestContext context) {
        context.runOnClient(mc -> mc.level.getEntitiesOfClass(EvilSnowman.class, mc.player.getBoundingBox().inflate(10))
                .stream().filter(snowman -> Math.abs(snowman.getX() - 0.7) < 0.2).findFirst()
                .ifPresent(snowman -> {
                    snowman.handleEntityEvent(EntityEvent.START_ATTACKING);
                    // freeze it at the top of the swing: tick() counts down, the render reads the rest
                    for (int i = 0; i < EvilSnowman.THROW_TICKS / 2; i++) {
                        snowman.tick();
                    }
                }));
    }

    private static <T extends Mob> void posed(ServerLevel level, EntityType<T> type, double x, double z) {
        T mob = type.create(level, EntitySpawnReason.COMMAND);
        if (mob == null) {
            throw new AssertionError("couldn't create " + type);
        }
        mob.snapTo(x, Y, z, 180.0F, 0.0F);
        mob.setYHeadRot(180.0F);
        mob.setYBodyRot(180.0F);
        mob.setNoAi(true);
        level.addFreshEntity(mob);
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
        context.takeScreenshot(TestScreenshotOptions.of(name).withSize(WIDTH, HEIGHT));
        context.runOnClient(mc -> mc.gui.hud.toggle());
    }
}
