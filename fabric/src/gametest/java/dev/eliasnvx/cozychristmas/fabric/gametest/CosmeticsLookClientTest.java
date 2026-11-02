package dev.eliasnvx.cozychristmas.fabric.gametest;

import dev.architectury.registry.registries.RegistrySupplier;
import dev.eliasnvx.cozychristmas.cosmetic.CosmeticSlot;
import dev.eliasnvx.cozychristmas.cosmetic.CosmeticsManager;
import dev.eliasnvx.cozychristmas.cosmetic.menu.CosmeticsContainer;
import dev.eliasnvx.cozychristmas.cosmetic.menu.CosmeticsLayout;
import dev.eliasnvx.cozychristmas.cosmetic.menu.InventoryMenuCosmetics;
import dev.eliasnvx.cozychristmas.mixin.client.AbstractContainerScreenAccessor;
import dev.eliasnvx.cozychristmas.registry.CozyItems;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotOptions;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Visual check of the knitwear on the player (every sweater, dyed and undyed knitwear, over armor, first person) and
 * of the cosmetic slots in the survival and creative inventory. Screenshots land in
 * {@code fabric/build/run/clientGameTest/screenshots}. Run with {@code ./gradlew :fabric:runClientGameTest}.
 */
public final class CosmeticsLookClientTest implements FabricClientGameTest {

    private static final int SETTLE = 20;
    private static final int WALK_TICKS = 12;
    private static final int ZOOM_FOV = 45;
    private static final int CLOSE_UP_FOV = 30;
    private static final int DEFAULT_FOV = 70;
    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;
    /** The knitwear dye worn with each sweater, in {@link CozyItems#sweaters()} order; null = undyed red. */
    private static final DyeColor[] KNIT_DYES = {null, DyeColor.WHITE, DyeColor.LIGHT_BLUE, DyeColor.BLACK, DyeColor.GREEN};

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            world.getConnection().waitForChunksRender();
            world.getServer().runCommand("time set noon");
            world.getServer().runCommand("weather clear");
            context.runOnClient(mc -> mc.options.fov().set(ZOOM_FOV));

            // every sweater with a hat, scarf and mittens, front and back
            List<RegistrySupplier<Item>> sweaters = CozyItems.sweaters();
            for (int i = 0; i < sweaters.size(); i++) {
                Item sweater = sweaters.get(i).get();
                outfit(world, sweater, KNIT_DYES[i]);
                String name = sweaters.get(i).getId().getPath();
                camera(context, CameraType.THIRD_PERSON_FRONT);
                context.waitTicks(SETTLE);
                shot(context, name + "_front", ZOOM_FOV);
                camera(context, CameraType.THIRD_PERSON_BACK);
                world.getServer().runCommand("tp @p ~ ~ ~ -35 0");
                context.waitTicks(SETTLE / 2);
                shot(context, name + "_back", ZOOM_FOV);
                world.getServer().runCommand("tp @p ~ ~ ~ 0 0");
            }

            // the scarf's back end from behind, standing and walking
            outfit(world, CozyItems.SNOWFLAKE_SWEATER.get(), null);
            camera(context, CameraType.THIRD_PERSON_BACK);
            context.waitTicks(SETTLE);
            shot(context, "scarf_back_idle", ZOOM_FOV);
            // shot while still walking: the scarf's back end lifts with the walking speed
            context.getInput().holdKey(options -> options.keyUp);
            context.waitTicks(WALK_TICKS);
            shot(context, "scarf_back_walking", ZOOM_FOV);
            context.getInput().releaseKey(options -> options.keyUp);
            world.getServer().runCommand("tp @p ~ ~ ~ 0 0");

            // over armor: the larger size wraps helmet and chestplate
            world.getServer().runCommand("item replace entity @p armor.head with minecraft:iron_helmet");
            world.getServer().runCommand("item replace entity @p armor.chest with minecraft:diamond_chestplate");
            camera(context, CameraType.THIRD_PERSON_FRONT);
            context.waitTicks(SETTLE);
            shot(context, "over_armor_front", ZOOM_FOV);
            camera(context, CameraType.THIRD_PERSON_BACK);
            context.waitTicks(SETTLE / 2);
            shot(context, "over_armor_back", ZOOM_FOV);
            world.getServer().runCommand("item replace entity @p armor.head with minecraft:air");
            world.getServer().runCommand("item replace entity @p armor.chest with minecraft:air");

            // sleeve and mitten in first person, with the HUD on: hiding it (F1) also hides the hand
            camera(context, CameraType.FIRST_PERSON);
            context.runOnClient(mc -> mc.options.fov().set(DEFAULT_FOV));
            context.waitTicks(SETTLE / 2);
            context.takeScreenshot(TestScreenshotOptions.of("first_person").withSize(WIDTH, HEIGHT));

            // the cosmetic slots in the survival inventory's panel; shift-click a hat in and out again
            world.getServer().runOnServer(server -> {
                ServerPlayer player = player(server);
                CosmeticsManager.set(player, CosmeticSlot.HEAD, ItemStack.EMPTY);
                player.getInventory().add(CozyItems.dyed(CozyItems.POMPOM_HAT.get(), DyeColor.GREEN));
            });
            context.waitTicks(SETTLE / 2);
            context.setScreen(() -> new InventoryScreen(Minecraft.getInstance().player));
            context.waitTicks(SETTLE / 2);
            // advancement and recipe toasts sit on the inventory's top-right corner, next to the panel
            context.runOnClient(mc -> mc.gui.toastManager().clear());
            context.takeScreenshot("inventory_panel_empty_head");
            context.runOnClient(mc -> {
                InventoryMenu menu = mc.player.inventoryMenu;
                mc.gameMode.handleContainerInput(menu.containerId, firstSlotWith(menu, CozyItems.POMPOM_HAT.get()), 0,
                        ContainerInput.QUICK_MOVE, mc.player);
            });
            context.waitTicks(SETTLE / 2);
            world.getServer().runOnServer(server -> check(worn(server, CosmeticSlot.HEAD).is(CozyItems.POMPOM_HAT.get()),
                    "shift-click did not put the hat into its slot"));
            context.runOnClient(mc -> mc.gui.toastManager().clear());
            context.takeScreenshot("inventory_panel");
            // the whole outfit is on: the worn hat's tooltip counts the Toasty set complete
            context.runOnClient(mc -> {
                ItemStack hat = CosmeticsManager.get(mc.player).get(CosmeticSlot.HEAD);
                String tooltip = Screen.getTooltipFromItem(mc, hat).stream().map(Component::getString)
                        .collect(Collectors.joining("\n"));
                check(tooltip.contains("(4/4)"), "the Toasty set is not counted complete:\n" + tooltip);
            });
            double[] hatCenter = context.computeOnClient(mc -> {
                AbstractContainerScreenAccessor screen = (AbstractContainerScreenAccessor) mc.gui.screen();
                Slot slot = mc.player.inventoryMenu.getSlot(cosmeticSlot(mc.player.inventoryMenu, CosmeticSlot.HEAD));
                int scale = mc.getWindow().getGuiScale();
                return new double[]{(screen.cozychristmas$leftPos() + slot.x + 8) * scale,
                        (screen.cozychristmas$topPos() + slot.y + 8) * scale};
            });
            context.getInput().setCursorPos(hatCenter[0], hatCenter[1]);
            context.waitTicks(2);
            context.takeScreenshot("toasty_set_tooltip");
            context.getInput().setCursorPos(0, 0);
            context.runOnClient(mc -> mc.gameMode.handleContainerInput(mc.player.inventoryMenu.containerId,
                    cosmeticSlot(mc.player.inventoryMenu, CosmeticSlot.HEAD), 0, ContainerInput.QUICK_MOVE, mc.player));
            context.waitTicks(SETTLE / 2);
            world.getServer().runOnServer(server -> check(worn(server, CosmeticSlot.HEAD).isEmpty(),
                    "shift-click did not take the hat out of its slot"));
            context.setScreen(() -> null);

            // creative: the same slots on the inventory tab, and what is put there reaches the server
            world.getServer().runCommand("gamemode creative @a");
            context.waitTicks(SETTLE / 2);
            context.setScreen(() -> {
                Minecraft mc = Minecraft.getInstance();
                return new CreativeModeInventoryScreen(mc.player, mc.player.connection.enabledFeatures(), false);
            });
            context.waitTicks(2);
            context.runOnClient(mc -> {
                CreativeModeInventoryScreen screen = (CreativeModeInventoryScreen) mc.gui.screen();
                selectInventoryTab(screen);
                int found = 0;
                int panelX = CosmeticsLayout.panelX(((AbstractContainerScreenAccessor) screen).cozychristmas$imageWidth());
                for (Slot slot : screen.getMenu().slots) {
                    if (slot.container instanceof CosmeticsContainer) {
                        check(slot.x >= panelX, "a cosmetic slot sits inside the creative inventory: x=" + slot.x);
                        found++;
                    }
                }
                check(found == CosmeticSlot.values().length, "creative inventory tab shows " + found + " cosmetic slots");
                InventoryMenu menu = mc.player.inventoryMenu;
                menu.getSlot(cosmeticSlot(menu, CosmeticSlot.NECK)).set(CozyItems.dyed(CozyItems.SCARF.get(), DyeColor.YELLOW));
                menu.broadcastChanges();
            });
            context.waitTicks(SETTLE / 2);
            world.getServer().runOnServer(server -> check(ItemStack.matches(worn(server, CosmeticSlot.NECK),
                            CozyItems.dyed(CozyItems.SCARF.get(), DyeColor.YELLOW)),
                    "a scarf put into the creative inventory's cosmetic slot did not reach the server"));
            context.runOnClient(mc -> mc.gui.toastManager().clear());
            context.takeScreenshot("creative_panel");
            context.setScreen(() -> null);
            world.getServer().runCommand("gamemode survival @a");
        }
    }

    /** Wears the sweater and the hat, scarf and mittens in one dye (null = undyed). */
    private static void outfit(TestSingleplayerContext world, Item sweater, @Nullable DyeColor dye) {
        world.getServer().runOnServer(server -> {
            ServerPlayer player = player(server);
            CosmeticsManager.set(player, CosmeticSlot.BODY, new ItemStack(sweater));
            CosmeticsManager.set(player, CosmeticSlot.HEAD, knit(CozyItems.POMPOM_HAT.get(), dye));
            CosmeticsManager.set(player, CosmeticSlot.NECK, knit(CozyItems.SCARF.get(), dye));
            CosmeticsManager.set(player, CosmeticSlot.HANDS, knit(CozyItems.MITTENS.get(), dye));
        });
    }

    private static ItemStack knit(Item item, @Nullable DyeColor dye) {
        return dye == null ? new ItemStack(item) : CozyItems.dyed(item, dye);
    }

    private static ServerPlayer player(MinecraftServer server) {
        return server.getPlayerList().getPlayers().getFirst();
    }

    private static ItemStack worn(MinecraftServer server, CosmeticSlot slot) {
        return CosmeticsManager.get(player(server)).get(slot);
    }

    private static int firstSlotWith(InventoryMenu menu, Item item) {
        for (int i = InventoryMenu.INV_SLOT_START; i < InventoryMenu.USE_ROW_SLOT_END; i++) {
            if (menu.getSlot(i).getItem().is(item)) {
                return i;
            }
        }
        throw new AssertionError("no " + item + " in the inventory");
    }

    private static int cosmeticSlot(InventoryMenu menu, CosmeticSlot slot) {
        return ((InventoryMenuCosmetics) menu).cozychristmas$cosmeticStart() + slot.ordinal();
    }

    /** selectTab is private; the test runs unobfuscated, so reflection finds it by name. */
    private static void selectInventoryTab(CreativeModeInventoryScreen screen) {
        try {
            var selectTab = CreativeModeInventoryScreen.class.getDeclaredMethod("selectTab", CreativeModeTab.class);
            selectTab.setAccessible(true);
            for (CreativeModeTab tab : BuiltInRegistries.CREATIVE_MODE_TAB) {
                if (tab.getType() == CreativeModeTab.Type.INVENTORY) {
                    selectTab.invoke(screen, tab);
                    return;
                }
            }
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("couldn't open the creative inventory tab", e);
        }
        throw new AssertionError("no creative inventory tab");
    }

    private static void check(boolean ok, String message) {
        if (!ok) {
            throw new AssertionError(message);
        }
    }

    private static void camera(ClientGameTestContext context, CameraType type) {
        context.runOnClient(mc -> mc.options.setCameraType(type));
    }

    /** A screenshot without the HUD at the given field of view. */
    private static void shot(ClientGameTestContext context, String name, int fov) {
        context.runOnClient(mc -> {
            mc.options.fov().set(fov);
            if (!mc.gui.hud.isHidden()) {
                mc.gui.hud.toggle();
            }
        });
        context.waitTicks(SETTLE / 4);
        context.takeScreenshot(TestScreenshotOptions.of(name).withSize(WIDTH, HEIGHT));
        context.runOnClient(mc -> {
            mc.options.fov().set(ZOOM_FOV);
            mc.gui.hud.toggle();
        });
    }
}
