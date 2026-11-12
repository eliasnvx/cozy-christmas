package dev.eliasnvx.cozychristmas.fabric.gametest;

import dev.eliasnvx.cozychristmas.compat.catgirl.CatgirlEars;
import dev.eliasnvx.cozychristmas.cosmetic.CosmeticSlot;
import dev.eliasnvx.cozychristmas.cosmetic.CosmeticsManager;
import dev.eliasnvx.cozychristmas.registry.CozyItems;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotOptions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.EnumMap;
import java.util.Map;

/**
 * Close-ups of the head pieces (ear warmers, elf hat, antler headband) from the front, the side, three quarters and
 * behind, plain and over a helmet. With Catgirl &amp; Catboy Mod in the dev run ({@code -Pcatgirl_jar=<its Fabric jar>})
 * the ear warmers are also shot over its cat ears, standing and just hurt (the ears flatten, the muffs with them).
 * Screenshots land in {@code fabric/build/run/clientGameTest/screenshots}.
 */
public final class HeadPiecesLookClientTest implements FabricClientGameTest {

    private static final int SETTLE = 20;
    private static final int WIDTH = 960;
    private static final int HEIGHT = 720;
    private static final int CLOSE_UP_FOV = 30;
    private static final int DEFAULT_FOV = 70;
    private static final int STAGE_Y = 100;
    /** How far the cameras stand from the head, in blocks. */
    private static final double DISTANCE = 1.9;
    private static final double ARMOR_STAND_EYE = 1.7875;
    /** The middle of the wearer's head (eye height 1.62 plus a little). */
    private static final double HEAD_Y = STAGE_Y + 1 + 1.75;

    private enum View {
        FRONT(0.0, 1.0), THREE_QUARTERS(0.7071, 0.7071), SIDE(1.0, 0.0), BACK(0.0, -1.0);

        final double dx;
        final double dz;

        View(double dx, double dz) {
            this.dx = dx;
            this.dz = dz;
        }
    }

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            world.getConnection().waitForChunksRender();
            world.getServer().runCommand("time set noon");
            world.getServer().runCommand("weather clear");
            world.getServer().runCommand("gamerule spawn_mobs false");
            world.getServer().runCommand(String.format("fill -3 %d -3 3 %d 3 minecraft:snow_block", STAGE_Y, STAGE_Y));
            world.getServer().runCommand(String.format("tp @p 0.5 %d 0.5 0 0", STAGE_Y + 1));
            context.waitTicks(SETTLE);
            world.getConnection().waitForChunksRender();
            Map<View, Integer> cameras = cameras(world);
            context.waitTicks(SETTLE / 2); // until the client has the armor stands
            context.runOnClient(mc -> mc.options.setCameraType(CameraType.FIRST_PERSON));
            TestCamera.showPlayer = true;

            head(world, CozyItems.EAR_WARMERS.get(), null);
            shots(context, cameras, "ear_warmers");
            head(world, CozyItems.EAR_WARMERS.get(), DyeColor.LIGHT_BLUE);
            shot(context, cameras, View.THREE_QUARTERS, "ear_warmers_light_blue");
            head(world, CozyItems.ELF_HAT.get(), null);
            shots(context, cameras, "elf_hat");
            head(world, CozyItems.ELF_HAT.get(), DyeColor.RED);
            shot(context, cameras, View.THREE_QUARTERS, "elf_hat_red");
            head(world, CozyItems.ANTLER_HEADBAND.get(), null);
            shots(context, cameras, "antler_headband");

            world.getServer().runCommand("item replace entity @p armor.head with minecraft:iron_helmet");
            for (Item piece : new Item[]{CozyItems.EAR_WARMERS.get(), CozyItems.ELF_HAT.get(), CozyItems.ANTLER_HEADBAND.get()}) {
                head(world, piece, null);
                shot(context, cameras, View.THREE_QUARTERS, name(piece) + "_helmet");
            }
            world.getServer().runCommand("item replace entity @p armor.head with minecraft:air");

            if (FabricLoader.getInstance().isModLoaded(CatgirlEars.MOD_ID)) {
                catEars(context, world, cameras);
            }

            // status effects right of every cosmetics panel (ours, and Catgirl's when it's loaded); at gui scale 1
            // there is room for them in the test window
            context.runOnClient(mc -> {
                TestCamera.showPlayer = false;
                mc.setCameraEntity(mc.player);
            });
            world.getServer().runCommand("effect give @p minecraft:speed 60 0 true");
            context.runOnClient(mc -> {
                mc.options.guiScale().set(1);
                mc.resizeGui();
            });
            context.setScreen(() -> new InventoryScreen(Minecraft.getInstance().player));
            context.waitTicks(SETTLE / 2);
            context.runOnClient(mc -> mc.gui.toastManager().clear());
            context.takeScreenshot("effects_beside_panels");
            context.setScreen(() -> null);
            context.runOnClient(mc -> {
                mc.options.guiScale().set(0);
                mc.resizeGui();
            });

            context.runOnClient(mc -> {
                TestCamera.showPlayer = false;
                mc.setCameraEntity(mc.player);
                mc.options.fov().set(DEFAULT_FOV);
            });
            world.getServer().runCommand("kill @e[type=minecraft:armor_stand]");
        }
    }

    /** Ear warmers over Catgirl's cat ears; the other head pieces with them too. */
    private static void catEars(ClientGameTestContext context, TestSingleplayerContext world, Map<View, Integer> cameras) {
        world.getServer().runOnServer(server -> CatgirlCosmetics.wearEars(player(server)));
        head(world, CozyItems.EAR_WARMERS.get(), null);
        context.waitTicks(SETTLE / 2);
        context.runOnClient(mc -> check(CatgirlEars.wears(mc.player), "the client should see the cat ears"));
        shots(context, cameras, "cat_ears_ear_warmers");
        // just hurt: the ears fold back and out, the muffs go with them
        world.getServer().runCommand("damage @p 1 minecraft:generic");
        context.waitTicks(4);
        shot(context, cameras, View.SIDE, "cat_ears_ear_warmers_hurt_side");
        shot(context, cameras, View.FRONT, "cat_ears_ear_warmers_hurt_front");
        world.getServer().runCommand("effect give @p minecraft:instant_health 1 1 true");
        world.getServer().runCommand("item replace entity @p armor.head with minecraft:iron_helmet");
        context.waitTicks(SETTLE);
        shot(context, cameras, View.THREE_QUARTERS, "cat_ears_ear_warmers_helmet");
        world.getServer().runCommand("item replace entity @p armor.head with minecraft:air");
        head(world, CozyItems.ANTLER_HEADBAND.get(), null);
        shot(context, cameras, View.THREE_QUARTERS, "cat_ears_antler_headband");
        shot(context, cameras, View.SIDE, "cat_ears_antler_headband_side");
        head(world, CozyItems.ELF_HAT.get(), null);
        shot(context, cameras, View.THREE_QUARTERS, "cat_ears_elf_hat");
    }

    private static void head(TestSingleplayerContext world, Item piece, DyeColor dye) {
        ItemStack stack = dye == null ? new ItemStack(piece) : CozyItems.dyed(piece, dye);
        world.getServer().runOnServer(server -> CosmeticsManager.set(player(server), CosmeticSlot.HEAD, stack));
    }

    private static void shots(ClientGameTestContext context, Map<View, Integer> cameras, String name) {
        for (View view : View.values()) {
            shot(context, cameras, view, name + "_" + view.name().toLowerCase(java.util.Locale.ROOT));
        }
    }

    private static void shot(ClientGameTestContext context, Map<View, Integer> cameras, View view, String name) {
        int camera = cameras.get(view);
        context.runOnClient(mc -> {
            var stand = mc.level.getEntity(camera);
            check(stand != null, "the client has no camera stand for " + view);
            mc.setCameraEntity(stand);
            mc.options.fov().set(CLOSE_UP_FOV);
            if (!mc.gui.hud.isHidden()) {
                mc.gui.hud.toggle();
            }
        });
        context.waitTicks(SETTLE / 4);
        context.takeScreenshot(TestScreenshotOptions.of(name).withSize(WIDTH, HEIGHT));
    }

    /** Invisible armor stands around the wearer's head, each looking at it: the client borrows their eyes. */
    private static Map<View, Integer> cameras(TestSingleplayerContext world) {
        return world.getServer().computeOnServer(server -> {
            ServerLevel level = server.overworld();
            ServerPlayer player = player(server);
            Map<View, Integer> ids = new EnumMap<>(View.class);
            for (View view : View.values()) {
                ArmorStand stand = EntityTypes.ARMOR_STAND.create(level, EntitySpawnReason.COMMAND);
                check(stand != null, "no armor stand");
                double x = player.getX() + view.dx * DISTANCE;
                double z = player.getZ() + view.dz * DISTANCE;
                float yaw = (float) Math.toDegrees(Math.atan2(view.dx, -view.dz)); // facing back towards the head
                // a little above the head, looking down at it
                float pitch = 10.0F;
                double eyeY = HEAD_Y + Math.tan(Math.toRadians(pitch)) * DISTANCE;
                stand.snapTo(x, eyeY - ARMOR_STAND_EYE, z, yaw, pitch);
                stand.setYHeadRot(yaw);
                stand.setYBodyRot(yaw);
                stand.setInvisible(true);
                stand.setNoGravity(true);
                level.addFreshEntity(stand);
                ids.put(view, stand.getId());
            }
            return ids;
        });
    }

    private static String name(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).getPath();
    }

    private static ServerPlayer player(MinecraftServer server) {
        return server.getPlayerList().getPlayers().getFirst();
    }

    private static void check(boolean ok, String message) {
        if (!ok) {
            throw new AssertionError(message);
        }
    }

    /** Catgirl & Catboy Mod's own manager, by name: the mod is in the dev run only when asked for, never compiled against. */
    private static final class CatgirlCosmetics {
        static void wearEars(Player player) {
            try {
                Class<?> slot = Class.forName("dev.eliasnvx.catgirl.cosmetic.CatSlot");
                Object ears = slot.getField("EARS").get(null);
                Item item = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath(CatgirlEars.MOD_ID, "cat_ears"));
                check(item != Items.AIR, "no catgirl:cat_ears");
                Class.forName("dev.eliasnvx.catgirl.cosmetic.CosmeticsManager")
                        .getMethod("set", Player.class, slot, ItemStack.class)
                        .invoke(null, player, ears, new ItemStack(item));
            } catch (ReflectiveOperationException e) {
                throw new AssertionError("couldn't put Catgirl's cat ears on", e);
            }
        }
    }
}
