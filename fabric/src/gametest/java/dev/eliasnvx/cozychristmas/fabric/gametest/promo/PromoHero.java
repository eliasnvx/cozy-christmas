package dev.eliasnvx.cozychristmas.fabric.gametest.promo;

import dev.eliasnvx.cozychristmas.krampus.KrampusSack;
import dev.eliasnvx.cozychristmas.registry.CozyEffects;
import dev.eliasnvx.cozychristmas.registry.CozyEntities;
import dev.eliasnvx.cozychristmas.season.ChristmasSeason;
import dev.eliasnvx.cozychristmas.sleigh.SantasSleigh;
import dev.eliasnvx.cozychristmas.workshop.Santa;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Hero shots for the store pages: a snowy world made of the flat test world (grass turned to snow, spruces planted),
 * Santa's workshop placed in it on the evening of 24 December, Krampus at the tree line, the sleigh taking off.
 */
public class PromoHero implements FabricClientGameTest {

    private static final int W = 1920;
    private static final int H = 1080;
    private static final int SETTLE = 30;

    @Override
    public void runTest(ClientGameTestContext context) {
        ChristmasSeason.setClock(Clock.fixed(LocalDate.of(2026, 12, 24).atStartOfDay(ZoneOffset.UTC).toInstant(),
                ZoneOffset.UTC));
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            world.getConnection().waitForChunksRender();
            cmd(world, "gamerule spawn_mobs false");
            cmd(world, "weather clear");
            cmd(world, "time set 11500");
            cmd(world, "gamemode spectator @p");
            cmd(world, "tp @p 0 120 0");
            context.waitTicks(SETTLE * 3);
            world.getConnection().waitForChunksRender();
            int ground = world.getServer().computeOnServer(server ->
                    server.overworld().getHeight(Heightmap.Types.WORLD_SURFACE, 0, 0));
            for (int x = -96; x < 96; x += 48) {
                for (int z = -96; z < 96; z += 48) {
                    cmd(world, "fill %d %d %d %d %d %d minecraft:grass_block[snowy=true] replace minecraft:grass_block",
                            x, ground - 1, z, x + 47, ground - 1, z + 47);
                }
            }
            Random random = new Random(26);
            for (int i = 0; i < 70; i++) {
                double angle = random.nextDouble() * Math.PI * 2;
                double radius = 34 + random.nextDouble() * 52;
                int x = (int) (Math.cos(angle) * radius);
                int z = (int) (Math.sin(angle) * radius);
                cmd(world, "place feature minecraft:spruce %d %d %d", x, ground, z);
            }
            cmd(world, "place structure cozychristmas:santas_workshop 0 %d 0", ground);
            context.waitTicks(SETTLE * 2);
            world.getConnection().waitForChunksRender();

            AtomicReference<Vec3> santaPos = new AtomicReference<>(new Vec3(0, ground, 0));
            world.getServer().runOnServer(server -> {
                ServerLevel level = server.overworld();
                level.getEntitiesOfClass(Santa.class, new AABB(-80, 0, -80, 80, 320, 80)).stream().findFirst()
                        .ifPresent(s -> santaPos.set(s.position()));
            });
            Vec3 c = santaPos.get();
            // the workshop from the tree line, low and golden
            look(world, c.add(-30, 7, -34), c.add(0, 5, 0));
            context.waitTicks(SETTLE * 2);
            world.getConnection().waitForChunksRender();
            shot(context, "hero_workshop_wide");
            look(world, c.add(24, 4, -20), c.add(0, 4, 0));
            context.waitTicks(SETTLE);
            shot(context, "hero_workshop_east");
            look(world, c.add(-9, 3, -13), c.add(0, 3.5, 0));
            context.waitTicks(SETTLE);
            shot(context, "hero_workshop_close");
            look(world, c.add(-3, 2.5, -7), c.add(0, 2.5, 0));
            context.waitTicks(SETTLE);
            shot(context, "hero_workshop_santa");
            // dusk deepens: the windows light up
            cmd(world, "time set 13300");
            look(world, c.add(-30, 7, -34), c.add(0, 5, 0));
            context.waitTicks(SETTLE * 2);
            shot(context, "hero_workshop_dusk");
            look(world, c.add(-9, 3, -13), c.add(0, 3.5, 0));
            context.waitTicks(SETTLE);
            shot(context, "hero_workshop_dusk_close");

            // Krampus at the edge of the trees, a sacked villager at his feet
            cmd(world, "time set 14200");
            Vec3 k = c.add(52, 0, 22);
            world.getServer().runOnServer(server -> {
                ServerLevel level = server.overworld();
                int gy = level.getHeight(Heightmap.Types.WORLD_SURFACE, (int) k.x, (int) k.z);
                var krampus = CozyEntities.KRAMPUS.get().create(level, EntitySpawnReason.COMMAND);
                krampus.snapTo(k.x, gy, k.z, 90.0F, 0.0F);
                krampus.setYHeadRot(90.0F);
                krampus.setYBodyRot(90.0F);
                krampus.setNoAi(true);
                level.addFreshEntity(krampus);
                var villager = EntityTypes.VILLAGER.create(level, EntitySpawnReason.COMMAND);
                villager.snapTo(k.x - 2.2, gy, k.z + 1.5, 0.0F, 0.0F);
                villager.setNoAi(true);
                level.addFreshEntity(villager);
                villager.addEffect(new MobEffectInstance(CozyEffects.SACKED.asHolder(), 100000, 0, false, false, true));
                level.addFreshEntity(KrampusSack.over((LivingEntity) villager, 100000));
            });
            context.waitTicks(SETTLE);
            int kg = world.getServer().computeOnServer(server ->
                    server.overworld().getHeight(Heightmap.Types.WORLD_SURFACE, (int) k.x, (int) k.z));
            look(world, new Vec3(k.x + 9, kg + 0.5, k.z + 1), new Vec3(k.x, kg + 1.9, k.z));
            context.waitTicks(SETTLE);
            shot(context, "hero_krampus_low");
            look(world, new Vec3(k.x + 6, kg + 2, k.z - 5), new Vec3(k.x - 1, kg + 1.5, k.z + 0.5));
            context.waitTicks(SETTLE);
            shot(context, "hero_krampus_three_quarters");

            // the sleigh leaves the workshop
            cmd(world, "time set 13000");
            world.getServer().runOnServer(server -> {
                ServerLevel level = server.overworld();
                List<SantasSleigh> sleighs = level.getEntitiesOfClass(SantasSleigh.class,
                        new AABB(-80, 0, -80, 80, 320, 80));
                if (!sleighs.isEmpty()) {
                    SantasSleigh sleigh = sleighs.getFirst();
                    level.getEntitiesOfClass(Mob.class, sleigh.getBoundingBox().inflate(6.0))
                            .forEach(m -> m.setNoAi(false));
                    sleigh.tryDepart(level, true);
                }
            });
            for (int step = 0; step < 3; step++) {
                context.waitTicks(step == 0 ? 70 : 60);
                chase(context, world);
                shot(context, "hero_sleigh_" + step);
            }
        } finally {
            ChristmasSeason.setClock(Clock.systemDefaultZone());
        }
    }

    private static void chase(ClientGameTestContext context, TestSingleplayerContext world) {
        AtomicReference<Vec3> at = new AtomicReference<>();
        AtomicReference<Float> yaw = new AtomicReference<>();
        world.getServer().runOnServer(server -> {
            List<SantasSleigh> found = server.overworld().getEntitiesOfClass(SantasSleigh.class,
                    new AABB(-400, -100, -400, 400, 400, 400));
            if (!found.isEmpty()) {
                at.set(found.getFirst().position());
                yaw.set(found.getFirst().getYRot());
            }
        });
        if (at.get() == null) {
            return;
        }
        Vec3 side = Vec3.directionFromRotation(0.0F, yaw.get() + 120.0F).scale(16.0);
        look(world, at.get().add(side.x, -4.0, side.z), at.get().add(0, 0.5, 0));
        context.waitTicks(4);
    }

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
        context.takeScreenshot(TestScreenshotOptions.of(name).withSize(W, H));
        context.runOnClient(mc -> {
            if (mc.gui.hud.isHidden()) {
                mc.gui.hud.toggle();
            }
        });
    }
}
