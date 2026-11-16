package dev.eliasnvx.cozychristmas.gametest;

import dev.eliasnvx.cozychristmas.cosmetic.CosmeticSlot;
import dev.eliasnvx.cozychristmas.cosmetic.CosmeticsManager;
import dev.eliasnvx.cozychristmas.cosmetic.menu.InventoryMenuCosmetics;
import dev.eliasnvx.cozychristmas.registry.CozyItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Consumer;

/**
 * Dupe checks for the cosmetic slots, shared by both loaders (Fabric: {@code fabric/src/gametest}, NeoForge:
 * {@code neoforge/.../dev/CozyGameTestsNeoForge}). Every way an item gets into or out of a slot goes through the
 * real server code (menu clicks, the creative slot packet, right-click use, death and respawn), and after each step
 * the number of pompom hats (menu, cursor, worn and dropped) must not change. Dev only: not in the released jars.
 */
public final class CosmeticSlotGameTests {

    public static final List<GameTestEntry> ALL = List.of(
            new GameTestEntry("shift_click", CosmeticSlotGameTests::shiftClick),
            new GameTestEntry("cursor_clicks", CosmeticSlotGameTests::cursorClicks),
            new GameTestEntry("number_keys", CosmeticSlotGameTests::numberKeys),
            new GameTestEntry("drag", CosmeticSlotGameTests::drag),
            new GameTestEntry("double_click", CosmeticSlotGameTests::doubleClick),
            new GameTestEntry("throw_and_middle_click", CosmeticSlotGameTests::throwAndMiddleClick),
            new GameTestEntry("right_click_equip", CosmeticSlotGameTests::rightClickEquip),
            new GameTestEntry("creative_slot_packet", CosmeticSlotGameTests::creativeSlotPacket),
            new GameTestEntry("death_drops_once", CosmeticSlotGameTests::deathDropsOnce),
            new GameTestEntry("cursed_cosmetics_vanish_on_death", CosmeticSlotGameTests::cursedOnesVanish),
            new GameTestEntry("keep_inventory", CosmeticSlotGameTests::keepInventory),
            new GameTestEntry("leaving_the_end", CosmeticSlotGameTests::leavingTheEnd));

    private static final Vec3 TEST_AREA_CENTER = new Vec3(1.5, 1.0, 1.5);
    /** Dropped items are looked for this far around the spot the player stood on. */
    private static final double DROP_RADIUS = 3.0;
    private static final int OUTSIDE = AbstractContainerMenu.SLOT_CLICKED_OUTSIDE;

    private CosmeticSlotGameTests() {
    }

    public static void shiftClick(GameTestHelper helper) {
        run(helper, rig -> {
            rig.inventory().setItem(0, hat(DyeColor.RED));
            rig.click(rig.hotbar(0), 0, ContainerInput.QUICK_MOVE);
            rig.expectWorn(DyeColor.RED, "shift-click from the hotbar");
            rig.expectTotal(1, "shift-click in");

            // slot taken: the hat move between hotbar and inventory like vanilla, the worn ones stay
            rig.inventory().setItem(1, hat(DyeColor.GREEN));
            rig.click(rig.hotbar(1), 0, ContainerInput.QUICK_MOVE);
            rig.expectWorn(DyeColor.RED, "shift-click onto a taken slot");
            rig.expectTotal(2, "shift-click onto a taken slot");

            rig.click(rig.hat, 0, ContainerInput.QUICK_MOVE);
            rig.expectWornEmpty("shift-click out");
            rig.expectTotal(2, "shift-click out");

            // a stack of three (a command can make them stack): one goes on, two stay
            rig.inventory().setItem(2, stackedHats(3));
            rig.click(rig.hotbar(2), 0, ContainerInput.QUICK_MOVE);
            rig.expectWornCount(1, "shift-click from a stack");
            rig.expectTotal(5, "shift-click from a stack");

            // no room in the inventory: the worn hat stay
            rig.reset();
            rig.wear(hat(DyeColor.RED));
            rig.fillInventory();
            rig.click(rig.hat, 0, ContainerInput.QUICK_MOVE);
            rig.expectWorn(DyeColor.RED, "shift-click out with a full inventory");
            rig.expectTotal(1, "shift-click out with a full inventory");
        });
    }

    public static void cursorClicks(GameTestHelper helper) {
        run(helper, rig -> {
            rig.inventory().setItem(0, stackedHats(3));
            rig.click(rig.hotbar(0), 0, ContainerInput.PICKUP);
            rig.click(rig.hat, 0, ContainerInput.PICKUP);
            rig.expectWornCount(1, "placing a stack of three");
            rig.helper.assertValueEqual(rig.menu.getCarried().getCount(), 2, "hat left on the cursor");
            rig.click(rig.hat, 0, ContainerInput.PICKUP);
            rig.click(rig.hat, 1, ContainerInput.PICKUP);
            rig.expectWornCount(1, "clicking a full slot with more of the same");
            rig.click(rig.hotbar(0), 0, ContainerInput.PICKUP);
            rig.expectTotal(3, "placing from the cursor");

            // different hat on the cursor swap with the worn ones
            rig.reset();
            rig.wear(hat(DyeColor.RED));
            rig.inventory().setItem(0, hat(DyeColor.GREEN));
            rig.click(rig.hotbar(0), 0, ContainerInput.PICKUP);
            rig.click(rig.hat, 0, ContainerInput.PICKUP);
            rig.expectWorn(DyeColor.GREEN, "swap with the cursor");
            rig.helper.assertTrue(dyed(rig.menu.getCarried(), DyeColor.RED), "the old hat should be on the cursor");
            rig.expectTotal(2, "swap with the cursor");
            rig.click(rig.hotbar(0), 0, ContainerInput.PICKUP);

            // a stick and mittens don't fit
            rig.inventory().setItem(1, new ItemStack(Items.STICK));
            rig.click(rig.hotbar(1), 0, ContainerInput.PICKUP);
            rig.click(rig.hat, 0, ContainerInput.PICKUP);
            rig.expectWorn(DyeColor.GREEN, "a stick on the hat slot");
            rig.click(rig.hotbar(1), 0, ContainerInput.PICKUP);
            rig.inventory().setItem(2, new ItemStack(CozyItems.MITTENS.get()));
            rig.click(rig.hotbar(2), 0, ContainerInput.PICKUP);
            rig.click(rig.hat, 0, ContainerInput.PICKUP);
            rig.expectWorn(DyeColor.GREEN, "mittens on the hat slot");
            rig.click(rig.hotbar(2), 0, ContainerInput.PICKUP);

            // right-click takes the worn hat onto the cursor
            rig.click(rig.hat, 1, ContainerInput.PICKUP);
            rig.expectWornEmpty("right-click take");
            rig.expectTotal(2, "right-click take");
        });
    }

    public static void numberKeys(GameTestHelper helper) {
        run(helper, rig -> {
            rig.inventory().setItem(2, hat(DyeColor.RED));
            rig.click(rig.hat, 2, ContainerInput.SWAP);
            rig.expectWorn(DyeColor.RED, "key 3 from the hotbar");
            rig.click(rig.hat, 2, ContainerInput.SWAP);
            rig.expectWornEmpty("key 3 back to the hotbar");
            rig.expectTotal(1, "key 3 there and back");

            rig.inventory().setItem(Inventory.SLOT_OFFHAND, hat(DyeColor.GREEN));
            rig.click(rig.hat, Inventory.SLOT_OFFHAND, ContainerInput.SWAP);
            rig.expectWorn(DyeColor.GREEN, "F from the offhand");
            rig.click(rig.hat, 2, ContainerInput.SWAP);
            rig.expectWorn(DyeColor.RED, "key 3 onto a taken slot");
            rig.expectTotal(2, "swapping with the hotbar");

            // a stack of three: one goes on, the worn hat go to the inventory
            rig.inventory().setItem(4, stackedHats(3));
            rig.click(rig.hat, 4, ContainerInput.SWAP);
            rig.expectWornCount(1, "key 5 with a stack");
            rig.expectTotal(5, "key 5 with a stack");

            // ... or to the ground when the inventory is full
            rig.reset();
            rig.wear(hat(DyeColor.RED));
            rig.inventory().setItem(4, stackedHats(3));
            rig.fillInventory();
            rig.click(rig.hat, 4, ContainerInput.SWAP);
            rig.expectWornCount(1, "key 5 with a stack and a full inventory");
            rig.expectTotal(4, "key 5 with a stack and a full inventory");

            rig.reset();
            rig.inventory().setItem(5, new ItemStack(Items.STICK));
            rig.inventory().setItem(6, new ItemStack(CozyItems.MITTENS.get()));
            rig.click(rig.hat, 5, ContainerInput.SWAP);
            rig.click(rig.hat, 6, ContainerInput.SWAP);
            rig.expectWornEmpty("a stick and mittens by key");
        });
    }

    public static void drag(GameTestHelper helper) {
        run(helper, rig -> {
            // left drag over the hat slot and two inventory slots: one each
            rig.menu.setCarried(stackedHats(3));
            rig.drag(0, rig.hat, InventoryMenu.INV_SLOT_START, InventoryMenu.INV_SLOT_START + 1);
            rig.expectWornCount(1, "left drag");
            rig.expectTotal(3, "left drag");

            rig.reset();
            rig.menu.setCarried(stackedHats(3));
            rig.drag(1, rig.hat, InventoryMenu.INV_SLOT_START);
            rig.expectWornCount(1, "right drag");
            rig.expectTotal(3, "right drag");

            // the creative copy drag is refused in survival
            rig.reset();
            rig.menu.setCarried(hat(DyeColor.RED));
            rig.drag(2, rig.hat, InventoryMenu.INV_SLOT_START, InventoryMenu.INV_SLOT_START + 1);
            rig.expectTotal(1, "middle drag in survival");
        });
    }

    public static void doubleClick(GameTestHelper helper) {
        run(helper, rig -> {
            rig.wear(stackedHats(1));
            rig.inventory().setItem(10, stackedHats(2));
            rig.menu.setCarried(stackedHats(1));
            rig.click(InventoryMenu.INV_SLOT_START + 11, 0, ContainerInput.PICKUP_ALL);
            rig.expectTotal(4, "double-click collect");
        });
    }

    public static void throwAndMiddleClick(GameTestHelper helper) {
        run(helper, rig -> {
            rig.wear(hat(DyeColor.RED));
            rig.click(rig.hat, 0, ContainerInput.THROW);
            rig.expectWornEmpty("Q on the hat slot");
            rig.expectTotal(1, "Q on the hat slot");

            rig.wear(hat(DyeColor.GREEN));
            rig.click(rig.hat, 1, ContainerInput.THROW);
            rig.expectWornEmpty("Ctrl+Q on the hat slot");
            rig.expectTotal(2, "Ctrl+Q on the hat slot");

            // nothing is thrown while the cursor holds something
            rig.wear(hat(DyeColor.WHITE));
            rig.menu.setCarried(new ItemStack(Items.STICK));
            rig.click(rig.hat, 0, ContainerInput.THROW);
            rig.expectWorn(DyeColor.WHITE, "Q with a full cursor");
            rig.menu.setCarried(ItemStack.EMPTY);

            // middle click copies only in creative
            rig.click(rig.hat, 2, ContainerInput.CLONE);
            rig.helper.assertTrue(rig.menu.getCarried().isEmpty(), "middle click must not copy in survival");
            rig.expectTotal(3, "middle click in survival");
        });
    }

    public static void rightClickEquip(GameTestHelper helper) {
        run(helper, rig -> {
            rig.use(InteractionHand.MAIN_HAND, hat(DyeColor.RED));
            rig.expectWorn(DyeColor.RED, "right-click with hat");
            rig.helper.assertTrue(rig.player.getMainHandItem().isEmpty(), "the hand should be empty");

            rig.use(InteractionHand.MAIN_HAND, hat(DyeColor.GREEN));
            rig.expectWorn(DyeColor.GREEN, "right-click with other hat");
            rig.helper.assertTrue(dyed(rig.player.getMainHandItem(), DyeColor.RED), "the old hat should be in the hand");
            rig.expectTotal(2, "right-click swap");

            // a stack of three: one goes on, two stay in the hand, the worn hat go to the inventory
            rig.inventory().setItem(1, rig.player.getMainHandItem());
            rig.use(InteractionHand.MAIN_HAND, stackedHats(3));
            rig.expectWornCount(1, "right-click with a stack");
            rig.helper.assertValueEqual(rig.player.getMainHandItem().getCount(), 2, "hat left in the hand");
            rig.expectTotal(5, "right-click with a stack");

            // ... or to the ground when the inventory is full
            rig.reset();
            rig.wear(hat(DyeColor.RED));
            rig.player.setItemInHand(InteractionHand.MAIN_HAND, stackedHats(3));
            rig.fillInventory();
            rig.use(InteractionHand.MAIN_HAND, rig.player.getMainHandItem());
            rig.expectWornCount(1, "right-click with a stack and a full inventory");
            rig.expectTotal(4, "right-click with a stack and a full inventory");

            rig.reset();
            rig.use(InteractionHand.OFF_HAND, hat(DyeColor.WHITE));
            rig.expectWorn(DyeColor.WHITE, "right-click with hat in the offhand");
            rig.expectTotal(1, "right-click from the offhand");
        });
    }

    public static void creativeSlotPacket(GameTestHelper helper) {
        run(helper, rig -> {
            rig.creativeSet(hat(DyeColor.RED));
            rig.expectWornEmpty("a creative slot packet in survival");

            rig.player.setGameMode(GameType.CREATIVE);
            ItemStack two = hat(DyeColor.RED);
            two.setCount(2);
            rig.creativeSet(two);
            rig.expectWornEmpty("two hat by creative packet");
            rig.creativeSet(new ItemStack(Items.STICK));
            rig.expectWornEmpty("a stick by creative packet");
            rig.creativeSet(new ItemStack(CozyItems.MITTENS.get()));
            rig.expectWornEmpty("mittens by creative packet");

            rig.creativeSet(hat(DyeColor.RED));
            rig.expectWorn(DyeColor.RED, "hat by creative packet");
            rig.creativeSet(ItemStack.EMPTY);
            rig.expectWornEmpty("clearing by creative packet");
        });
    }

    public static void deathDropsOnce(GameTestHelper helper) {
        run(helper, rig -> rig.withKeepInventory(false, () -> {
            rig.wear(hat(DyeColor.RED));
            rig.inventory().setItem(0, hat(DyeColor.GREEN));
            rig.die();
            rig.expectWornEmpty("death");
            rig.expectTotal(2, "death (worn and carried hat on the ground)");
            rig.respawn(false);
            rig.expectWornEmpty("respawn after death");
            rig.expectTotal(2, "respawn after death");
        }));
    }

    /** Like armor: a worn piece with Curse of Vanishing is gone on death, not dropped. */
    public static void cursedOnesVanish(GameTestHelper helper) {
        run(helper, rig -> rig.withKeepInventory(false, () -> {
            ItemStack cursed = hat(DyeColor.RED);
            cursed.enchant(rig.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                    .getOrThrow(Enchantments.VANISHING_CURSE), 1);
            rig.wear(cursed);
            rig.die();
            rig.expectWornEmpty("death with a cursed hat");
            rig.expectTotal(0, "death with a cursed hat");
        }));
    }

    public static void keepInventory(GameTestHelper helper) {
        run(helper, rig -> rig.withKeepInventory(true, () -> {
            rig.wear(hat(DyeColor.RED));
            rig.die();
            rig.respawn(false);
            rig.expectWorn(DyeColor.RED, "respawn with keepInventory");
            rig.expectTotal(1, "respawn with keepInventory");
        }));
    }

    public static void leavingTheEnd(GameTestHelper helper) {
        run(helper, rig -> {
            rig.wear(hat(DyeColor.RED));
            rig.respawn(true);
            rig.expectWorn(DyeColor.RED, "leaving the End");
            rig.expectTotal(1, "leaving the End");
        });
    }

    private static ItemStack hat(DyeColor dye) {
        return CozyItems.dyed(CozyItems.POMPOM_HAT.get(), dye);
    }

    /** Hats that stack, as a command or a data pack can make them. The slot still takes one. */
    private static ItemStack stackedHats(int count) {
        ItemStack stack = hat(DyeColor.GRAY);
        stack.set(DataComponents.MAX_STACK_SIZE, 64);
        stack.setCount(count);
        return stack;
    }

    private static boolean dyed(ItemStack stack, DyeColor dye) {
        DyedItemColor color = stack.get(DataComponents.DYED_COLOR);
        return color != null && color.rgb() == dye.getTextureDiffuseColor();
    }

    /** Runs the body with a fresh survival player in the test area and always removes the player afterwards. */
    private static void run(GameTestHelper helper, Consumer<Rig> body) {
        Rig rig = new Rig(helper);
        try {
            body.accept(rig);
            helper.succeed();
        } finally {
            rig.close();
        }
    }

    /** A mock player with its inventory menu, and the checks the tests share. */
    private static final class Rig {

        final GameTestHelper helper;
        final int hat;
        private final AABB dropArea;
        ServerPlayer player;
        InventoryMenu menu;

        Rig(GameTestHelper helper) {
            this.helper = helper;
            Vec3 center = helper.absoluteVec(TEST_AREA_CENTER);
            this.dropArea = new AABB(center, center).inflate(DROP_RADIUS);
            this.player = TestPlayers.survival(helper, TEST_AREA_CENTER);
            this.menu = player.inventoryMenu;
            this.hat = ((InventoryMenuCosmetics) menu).cozychristmas$cosmeticStart() + CosmeticSlot.HEAD.ordinal();
        }

        Inventory inventory() {
            return player.getInventory();
        }

        int hotbar(int index) {
            return InventoryMenu.USE_ROW_SLOT_START + index;
        }

        void click(int slot, int button, ContainerInput input) {
            menu.clicked(slot, button, input, player);
        }

        /** A mouse drag with the cursor stack: type 0 left (split evenly), 1 right (one each), 2 middle (copy). */
        void drag(int type, int... slots) {
            click(OUTSIDE, AbstractContainerMenu.getQuickcraftMask(0, type), ContainerInput.QUICK_CRAFT);
            for (int slot : slots) {
                click(slot, AbstractContainerMenu.getQuickcraftMask(1, type), ContainerInput.QUICK_CRAFT);
            }
            click(OUTSIDE, AbstractContainerMenu.getQuickcraftMask(2, type), ContainerInput.QUICK_CRAFT);
        }

        void use(InteractionHand hand, ItemStack stack) {
            player.setItemInHand(hand, stack);
            player.gameMode.useItem(player, player.level(), stack, hand);
        }

        void creativeSet(ItemStack stack) {
            player.connection.handleSetCreativeModeSlot(new ServerboundSetCreativeModeSlotPacket(hat, stack));
        }

        void wear(ItemStack stack) {
            CosmeticsManager.set(player, CosmeticSlot.HEAD, stack);
        }

        void die() {
            player.kill(level());
        }

        /** Respawns like after death ({@code false}) or like leaving the End ({@code true}); the new player stays here. */
        void respawn(boolean leavingTheEnd) {
            player = level().getServer().getPlayerList().respawn(player, leavingTheEnd,
                    leavingTheEnd ? Entity.RemovalReason.CHANGED_DIMENSION : Entity.RemovalReason.KILLED);
            TestPlayers.loaded(player);
            menu = player.inventoryMenu;
        }

        void withKeepInventory(boolean keep, Runnable body) {
            GameRules rules = level().getGameRules();
            boolean before = rules.get(GameRules.KEEP_INVENTORY);
            rules.set(GameRules.KEEP_INVENTORY, keep, level().getServer());
            try {
                body.run();
            } finally {
                rules.set(GameRules.KEEP_INVENTORY, before, level().getServer());
            }
        }

        /** Fills every empty inventory slot (hotbar included) with stone. */
        void fillInventory() {
            for (int i = 0; i < Inventory.INVENTORY_SIZE; i++) {
                if (inventory().getItem(i).isEmpty()) {
                    inventory().setItem(i, new ItemStack(Items.STONE, Items.STONE.getDefaultMaxStackSize()));
                }
            }
        }

        /** Empties inventory, cursor, crafting grid and cosmetics, and removes dropped items. */
        void reset() {
            inventory().clearContent();
            menu.setCarried(ItemStack.EMPTY);
            menu.getCraftSlots().clearContent();
            CosmeticsManager.clear(player);
            drops().forEach(Entity::discard);
        }

        void close() {
            drops().forEach(Entity::discard);
            TestPlayers.remove(helper, player);
        }

        ItemStack worn() {
            return CosmeticsManager.get(player).get(CosmeticSlot.HEAD);
        }

        void expectWorn(DyeColor dye, String step) {
            helper.assertTrue(worn().is(CozyItems.POMPOM_HAT.get()) && dyed(worn(), dye),
                    "after " + step + ": a " + dye + " hat should be worn, got " + worn());
        }

        void expectWornCount(int count, String step) {
            helper.assertValueEqual(worn().getCount(), count, "worn hat after " + step);
        }

        void expectWornEmpty(String step) {
            helper.assertTrue(worn().isEmpty(), "after " + step + ": nothing should be worn, got " + worn());
        }

        /** Pompom hats in the menu (crafting grid, armor, inventory, offhand, cosmetic slots), on the cursor and on the ground. */
        void expectTotal(int expected, String step) {
            int total = menu.getCarried().is(CozyItems.POMPOM_HAT.get()) ? menu.getCarried().getCount() : 0;
            for (int i = InventoryMenu.CRAFT_SLOT_START; i < menu.slots.size(); i++) {
                ItemStack stack = menu.getSlot(i).getItem();
                if (stack.is(CozyItems.POMPOM_HAT.get())) {
                    total += stack.getCount();
                }
            }
            for (ItemEntity drop : drops()) {
                if (drop.getItem().is(CozyItems.POMPOM_HAT.get())) {
                    total += drop.getItem().getCount();
                }
            }
            helper.assertValueEqual(total, expected, "pompom hats after " + step);
        }

        private List<ItemEntity> drops() {
            return level().getEntitiesOfClass(ItemEntity.class, dropArea, Entity::isAlive);
        }

        private ServerLevel level() {
            return helper.getLevel();
        }
    }
}
