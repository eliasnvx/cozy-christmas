package dev.eliasnvx.cozychristmas.fabric.gametest;

import dev.eliasnvx.cozychristmas.present.PresentDesign;
import dev.eliasnvx.cozychristmas.registry.CozyEntities;
import dev.eliasnvx.cozychristmas.registry.CozyItems;
import dev.eliasnvx.cozychristmas.reindeer.Reindeer;
import dev.eliasnvx.cozychristmas.season.ChristmasSeason;
import dev.eliasnvx.cozychristmas.workshop.Elf;
import dev.eliasnvx.cozychristmas.workshop.Santa;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotOptions;
import net.minecraft.client.CameraType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Visual check of Santa's workshop: the three mobs side by side (an elf carrying a present, a fawn), a reindeer flying
 * with a rider, and the generated structure from above, at Santa's, in the hall, the warehouse and the stable.
 * Screenshots land in {@code fabric/build/run/clientGameTest/screenshots}.
 */
public class WorkshopLookClientTest implements FabricClientGameTest {

    private static final int SETTLE = 20;
    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;
    private static final int STAGE_Y = 100;
    /** The workshop is placed here, away from the stage. */
    private static final BlockPos WORKSHOP = new BlockPos(160, 0, 160);
    private static final double SEARCH = 64.0;

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            world.getConnection().waitForChunksRender();
            world.getServer().runCommand("time set noon");
            world.getServer().runCommand("weather clear");
            world.getServer().runCommand("gamerule spawn_mobs false");
            lineup(context, world);
            flying(context, world);
            workshop(context, world);
        }
    }

    /** The mobs on a snowy stage in the sky, facing the camera. */
    private static void lineup(ClientGameTestContext context, TestSingleplayerContext world) {
        world.getServer().runCommand("fill -10 " + (STAGE_Y - 1) + " -4 10 " + (STAGE_Y - 1) + " 10 minecraft:snow_block");
        world.getServer().runCommand("fill -10 " + STAGE_Y + " -4 10 " + (STAGE_Y + 5) + " 10 minecraft:air");
        world.getServer().runCommand("tp @p 0 " + STAGE_Y + " -2 0 12");
        context.waitTicks(SETTLE);
        world.getServer().runOnServer(server -> {
            ServerLevel level = server.overworld();
            posed(level, CozyEntities.ELF.get(), -4.5, 4.5, elf -> elf.setItemSlot(EquipmentSlot.MAINHAND,
                    CozyItems.present(PresentDesign.GREEN)));
            posed(level, CozyEntities.ELF.get(), -2.8, 4.0, elf -> { });
            posed(level, CozyEntities.SANTA.get(), -0.5, 5.0, santa -> { });
            posed(level, CozyEntities.REINDEER.get(), 2.8, 5.5, reindeer -> {
                reindeer.setTamed(true);
                reindeer.setItemSlot(EquipmentSlot.SADDLE, new ItemStack(Items.SADDLE));
            });
            posed(level, CozyEntities.REINDEER.get(), 5.5, 4.0, fawn -> fawn.setAge(-24000));
        });
        context.waitTicks(SETTLE);
        shot(context, "workshop_mobs");
        world.getServer().runCommand("tp @p 0 " + STAGE_Y + " 9 180 12");
        context.waitTicks(SETTLE / 2);
        shot(context, "workshop_mobs_back");
        world.getServer().runCommand("kill @e[type=!player]");
    }

    /** A reindeer in season, in the air with the player riding it: the flying pose. */
    private static void flying(ClientGameTestContext context, TestSingleplayerContext world) {
        world.getServer().runOnServer(server -> {
            ChristmasSeason.setClock(Clock.fixed(LocalDate.of(2026, 12, 10).atStartOfDay(ZoneOffset.UTC).toInstant(),
                    ZoneOffset.UTC));
            ServerLevel level = server.overworld();
            Reindeer reindeer = CozyEntities.REINDEER.get().create(level, EntitySpawnReason.COMMAND);
            check(reindeer != null, "no reindeer");
            reindeer.snapTo(0.5, STAGE_Y + 12, 0.5, 0.0F, 0.0F);
            reindeer.setTamed(true);
            reindeer.setItemSlot(EquipmentSlot.SADDLE, new ItemStack(Items.SADDLE));
            reindeer.refreshFlight(true);
            level.addFreshEntity(reindeer);
            ServerPlayer player = player(server);
            player.snapTo(0.5, STAGE_Y + 12, 0.5, 0.0F, 0.0F);
            player.startRiding(reindeer);
        });
        context.runOnClient(mc -> mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT));
        context.waitTicks(SETTLE / 2);
        context.runOnClient(mc -> check(mc.player.getVehicle() instanceof Reindeer r && r.isFlying(),
                "the ridden reindeer should be flying"));
        shot(context, "reindeer_flying");
        // from the side: the camera borrows an invisible armor stand five blocks east, looking west at the reindeer
        int standId = world.getServer().computeOnServer(server -> {
            ServerLevel level = server.overworld();
            ArmorStand stand = EntityTypes.ARMOR_STAND.create(level, EntitySpawnReason.COMMAND);
            check(stand != null, "no armor stand");
            Entity reindeer = player(server).getVehicle();
            check(reindeer != null, "the player should still ride");
            stand.snapTo(reindeer.getX() + 5.0, reindeer.getY() - 0.5, reindeer.getZ(), 90.0F, 5.0F);
            stand.setYHeadRot(90.0F); // the camera looks where the head looks
            stand.setYBodyRot(90.0F);
            stand.setInvisible(true);
            stand.setNoGravity(true);
            level.addFreshEntity(stand);
            return stand.getId();
        });
        context.waitTicks(2);
        context.runOnClient(mc -> {
            mc.options.setCameraType(CameraType.FIRST_PERSON);
            mc.setCameraEntity(mc.level.getEntity(standId));
        });
        context.waitTicks(2);
        shot(context, "reindeer_flying_side");
        context.runOnClient(mc -> mc.setCameraEntity(mc.player));
        context.runOnClient(mc -> mc.options.setCameraType(CameraType.FIRST_PERSON));
        world.getServer().runOnServer(server -> {
            player(server).stopRiding();
            ChristmasSeason.setClock(Clock.systemDefaultZone());
        });
        world.getServer().runCommand("kill @e[type=!player]");
    }

    /** The structure placed by command, then looked at from above and from inside its buildings. */
    private static void workshop(ClientGameTestContext context, TestSingleplayerContext world) {
        world.getServer().runCommand("gamemode spectator @a"); // a free camera: no falling, no walls in the way
        world.getServer().runCommand(String.format(Locale.ROOT, "tp @p %d 120 %d", WORKSHOP.getX(), WORKSHOP.getZ()));
        context.waitTicks(SETTLE);
        world.getConnection().waitForChunksRender();
        world.getServer().runCommand(String.format(Locale.ROOT, "place structure cozychristmas:santas_workshop %d 0 %d",
                WORKSHOP.getX(), WORKSHOP.getZ()));
        context.waitTicks(SETTLE);

        List<Vec3> santas = new ArrayList<>();
        List<Vec3> elves = new ArrayList<>();
        List<Vec3> reindeer = new ArrayList<>();
        world.getServer().runOnServer(server -> {
            ServerLevel level = server.overworld();
            AABB area = AABB.ofSize(Vec3.atCenterOf(WORKSHOP.atY(level.getSeaLevel())), SEARCH * 2, 512, SEARCH * 2);
            level.getEntitiesOfClass(Santa.class, area, Entity::isAlive).forEach(e -> santas.add(e.position()));
            level.getEntitiesOfClass(Elf.class, area, Entity::isAlive).forEach(e -> elves.add(e.position()));
            level.getEntitiesOfClass(Reindeer.class, area, Entity::isAlive).forEach(e -> reindeer.add(e.position()));
        });
        check(santas.size() == 1, "the workshop should have one Santa, found " + santas.size());
        check(elves.size() == 5, "the workshop should have five elves, found " + elves.size());
        check(reindeer.size() == 3, "the workshop should have three reindeer, found " + reindeer.size());

        List<Vec3> everyone = new ArrayList<>(santas);
        everyone.addAll(elves);
        everyone.addAll(reindeer);
        Vec3 center = centroid(everyone);
        look(world, center.add(-22, 26, -22), center);
        context.waitTicks(SETTLE * 2);
        world.getConnection().waitForChunksRender();
        shot(context, "workshop_overview");
        look(world, center.add(14, 9, -16), center);
        context.waitTicks(SETTLE);
        shot(context, "workshop_overview_low");

        Vec3 santa = santas.getFirst();
        inside(context, world, santa, "workshop_santa");
        // the hall: where most elves are; the warehouse: the elf farthest from them
        Vec3 hall = centroid(elves.stream().sorted(Comparator.comparingDouble(e -> -neighbours(e, elves))).limit(4).toList());
        Vec3 storeElf = elves.stream().max(Comparator.comparingDouble(e -> e.distanceToSqr(hall))).orElseThrow();
        inside(context, world, hall, "workshop_hall");
        inside(context, world, storeElf, "workshop_warehouse");
        inside(context, world, centroid(reindeer), "workshop_stable");
    }

    /**
     * Stands 4 to 2 blocks from {@code target} on a side with room to stand and a clear view at eye height (indoors),
     * and looks at it.
     */
    private static void inside(ClientGameTestContext context, TestSingleplayerContext world, Vec3 target, String name) {
        Vec3 spot = world.getServer().computeOnServer(server -> {
            ServerLevel level = server.overworld();
            BlockPos center = BlockPos.containing(target);
            for (int distance = 4; distance >= 2; distance--) {
                for (Direction side : Direction.Plane.HORIZONTAL) {
                    boolean clear = true;
                    for (int step = 1; step <= distance && clear; step++) {
                        BlockPos eye = center.relative(side, step).above();
                        clear = level.getBlockState(eye).isAir();
                    }
                    BlockPos feet = center.relative(side, distance);
                    if (clear && level.getBlockState(feet).getCollisionShape(level, feet).isEmpty()) {
                        return Vec3.atBottomCenterOf(feet);
                    }
                }
            }
            return target.add(0, 2, 0);
        });
        look(world, spot, target.add(0, 0.8, 0));
        context.waitTicks(SETTLE);
        shot(context, name);
    }

    private static void look(TestSingleplayerContext world, Vec3 from, Vec3 at) {
        world.getServer().runCommand(String.format(Locale.ROOT, "tp @p %.2f %.2f %.2f facing %.2f %.2f %.2f",
                from.x, from.y, from.z, at.x, at.y, at.z));
    }

    private static double neighbours(Vec3 elf, List<Vec3> elves) {
        return elves.stream().filter(other -> other.distanceToSqr(elf) < 100).count();
    }

    private static Vec3 centroid(List<Vec3> points) {
        Vec3 sum = Vec3.ZERO;
        for (Vec3 point : points) {
            sum = sum.add(point);
        }
        return sum.scale(1.0 / points.size());
    }

    /** A mob with no AI at (x, STAGE_Y, z) facing the camera (north). */
    private static <T extends Mob> void posed(ServerLevel level, EntityType<T> type, double x, double z,
                                              java.util.function.Consumer<T> setup) {
        T mob = type.create(level, EntitySpawnReason.COMMAND);
        check(mob != null, "couldn't create " + type);
        mob.snapTo(x, STAGE_Y, z, 180.0F, 0.0F);
        mob.setYHeadRot(180.0F);
        mob.setYBodyRot(180.0F);
        mob.setNoAi(true);
        setup.accept(mob);
        level.addFreshEntity(mob);
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
