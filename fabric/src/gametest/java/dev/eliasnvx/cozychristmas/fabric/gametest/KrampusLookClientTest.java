package dev.eliasnvx.cozychristmas.fabric.gametest;

import dev.eliasnvx.cozychristmas.krampus.Krampus;
import dev.eliasnvx.cozychristmas.krampus.KrampusSack;
import dev.eliasnvx.cozychristmas.registry.CozyEffects;
import dev.eliasnvx.cozychristmas.registry.CozyEntities;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotOptions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Visual check of Krampus (standing, mid-lash, roaring, at night), a villager in his sack, and his lair placed by
 * template into a block of stone (the pit from above, the cave inside). Screenshots land in
 * {@code fabric/build/run/clientGameTest/screenshots}.
 */
public final class KrampusLookClientTest implements FabricClientGameTest {

    private static final int SETTLE = 20;
    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;
    private static final int Y = 100;
    private static final int LONG = 100000;
    private static final BlockPos LAIR = new BlockPos(-240, 0, -240);
    /** The lair template's size (tools/gen_krampus_lair.py) and the stone block it is set into. */
    private static final int LAIR_SIZE = 29;
    private static final int LAIR_HEIGHT = 21;
    private static final int MOUNTAIN_MARGIN = 6;
    private static final int MOUNTAIN_BOTTOM = -60;
    private static final int MOUNTAIN_TOP = -10;
    private static final int FILL_LAYERS = 12;
    private static final double SEARCH = 40.0;

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            world.getConnection().waitForChunksRender();
            world.getServer().runCommand("gamerule spawn_mobs false");
            world.getServer().runCommand("weather clear");
            world.getServer().runCommand("time set noon");
            lineup(context, world);
            lair(context, world);
        }
    }

    private static void lineup(ClientGameTestContext context, TestSingleplayerContext world) {
        cmd(world, "fill -8 %d -4 8 %d 12 minecraft:snow_block", Y - 1, Y - 1);
        cmd(world, "fill -8 %d -4 8 %d 12 minecraft:air", Y, Y + 8);
        cmd(world, "tp @p 0.5 %d -3.5 0 5", Y);
        context.waitTicks(SETTLE);
        world.getServer().runOnServer(server -> {
            ServerLevel level = server.overworld();
            posed(level, CozyEntities.KRAMPUS.get(), -1.5, 5.0, krampus -> { });
            posed(level, EntityTypes.VILLAGER, 3.0, 4.5, villager -> { });
        });
        context.waitTicks(SETTLE / 2);
        world.getServer().runOnServer(server -> {
            ServerLevel level = server.overworld();
            for (Entity entity : level.getEntities((Entity) null, new AABB(-8, Y, -4, 8, Y + 4, 12),
                    e -> e.getType() == EntityTypes.VILLAGER)) {
                LivingEntity villager = (LivingEntity) entity;
                villager.addEffect(new MobEffectInstance(CozyEffects.SACKED.asHolder(), LONG, 0, false, false, true));
                level.addFreshEntity(KrampusSack.over(villager, LONG));
            }
        });
        context.waitTicks(SETTLE);
        world.getConnection().waitForChunksRender();
        shot(context, "krampus_day");
        pose(context, EntityEvent.START_ATTACKING, Krampus.LASH_TICKS * 2 / 3);
        shot(context, "krampus_lash");
        pose(context, EntityEvent.EAT_GRASS, Krampus.ROAR_TICKS / 2);
        shot(context, "krampus_roar");
        cmd(world, "tp @p 0.5 %d 1.0 30 -12", Y);
        context.waitTicks(SETTLE);
        shot(context, "krampus_close");
        world.getServer().runCommand("time set midnight");
        cmd(world, "tp @p 0.5 %d -3.5 0 5", Y);
        context.waitTicks(SETTLE);
        shot(context, "krampus_night");
        world.getServer().runCommand("time set noon");
        world.getServer().runCommand("kill @e[type=!player]");
    }

    /**
     * The test world is flat, and the structure stands on the generator's surface, which there is the bottom of the
     * world: the cave would land below it. So this builds a block of "mountain" and places the lair's template in it
     * directly, its rim layer on the mountain's top, as the structure's start height does in real mountains.
     */
    private static void lair(ClientGameTestContext context, TestSingleplayerContext world) {
        world.getServer().runCommand("gamemode spectator @a");
        cmd(world, "tp @p %d 40 %d", LAIR.getX(), LAIR.getZ());
        context.waitTicks(SETTLE);
        world.getConnection().waitForChunksRender();
        int x0 = LAIR.getX() - MOUNTAIN_MARGIN;
        int z0 = LAIR.getZ() - MOUNTAIN_MARGIN;
        int x1 = LAIR.getX() + LAIR_SIZE + MOUNTAIN_MARGIN;
        int z1 = LAIR.getZ() + LAIR_SIZE + MOUNTAIN_MARGIN;
        for (int y = MOUNTAIN_BOTTOM; y < MOUNTAIN_TOP; y += FILL_LAYERS) {
            cmd(world, "fill %d %d %d %d %d %d minecraft:stone", x0, y, z0, x1, Math.min(MOUNTAIN_TOP - 1, y + FILL_LAYERS - 1), z1);
        }
        cmd(world, "fill %d %d %d %d %d %d minecraft:snow_block", x0, MOUNTAIN_TOP, z0, x1, MOUNTAIN_TOP, z1);
        // the template's rim layer (one below its top) on the mountain's top
        cmd(world, "place template cozychristmas:krampus_lair/lair %d %d %d", LAIR.getX(), MOUNTAIN_TOP - LAIR_HEIGHT + 2, LAIR.getZ());
        context.waitTicks(SETTLE);
        List<Vec3> found = new ArrayList<>();
        world.getServer().runOnServer(server -> {
            AABB area = AABB.ofSize(Vec3.atCenterOf(LAIR), SEARCH * 2, 400, SEARCH * 2);
            server.overworld().getEntitiesOfClass(Krampus.class, area, Entity::isAlive).forEach(k -> found.add(k.position()));
        });
        check(found.size() == 1, "the lair should hold Krampus, found " + found.size());
        Vec3 krampus = found.getFirst();
        Vec3 center = krampus.add(0, 0, -4);
        Vec3 pit = new Vec3(center.x, MOUNTAIN_TOP + 1, center.z);
        look(world, pit.add(9, 10, -9), pit);
        context.waitTicks(SETTLE * 2);
        world.getConnection().waitForChunksRender();
        shot(context, "krampus_lair_pit");
        look(world, center.add(-6, 5, -8), krampus.add(0, 1.5, 1));
        context.waitTicks(SETTLE);
        world.getConnection().waitForChunksRender();
        shot(context, "krampus_lair_cave");
        world.getServer().runCommand("effect give @p minecraft:night_vision 30 0 true");
        context.waitTicks(4);
        shot(context, "krampus_lair_cave_lit");
        look(world, center.add(7, 4, 7), center.add(-8, 1, -2));
        context.waitTicks(SETTLE);
        shot(context, "krampus_lair_cages");
        world.getServer().runCommand("effect clear @p");
        world.getServer().runCommand("gamemode creative @a");
    }

    /** The posed Krampus gets a move's event on the client, then ticks partway through it. */
    private static void pose(ClientGameTestContext context, byte event, int ticks) {
        context.runOnClient(mc -> mc.level.getEntitiesOfClass(Krampus.class, mc.player.getBoundingBox().inflate(16))
                .forEach(krampus -> {
                    krampus.handleEntityEvent(event);
                    for (int i = 0; i < ticks; i++) {
                        krampus.tick();
                    }
                }));
    }

    private static <T extends Mob> void posed(ServerLevel level, EntityType<T> type, double x, double z, Consumer<T> setup) {
        T mob = type.create(level, EntitySpawnReason.COMMAND);
        check(mob != null, "couldn't create " + type);
        mob.snapTo(x, Y, z, 180.0F, 0.0F);
        mob.setYHeadRot(180.0F);
        mob.setYBodyRot(180.0F);
        mob.setNoAi(true);
        setup.accept(mob);
        level.addFreshEntity(mob);
    }

    private static void look(TestSingleplayerContext world, Vec3 from, Vec3 at) {
        world.getServer().runCommand(String.format(Locale.ROOT, "tp @p %.2f %.2f %.2f facing %.2f %.2f %.2f",
                from.x, from.y, from.z, at.x, at.y, at.z));
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

    private static void check(boolean ok, String message) {
        if (!ok) {
            throw new AssertionError(message);
        }
    }
}
