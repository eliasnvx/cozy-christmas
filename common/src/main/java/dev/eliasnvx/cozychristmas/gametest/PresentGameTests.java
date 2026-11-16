package dev.eliasnvx.cozychristmas.gametest;

import dev.eliasnvx.cozychristmas.config.CozyConfig;
import dev.eliasnvx.cozychristmas.karma.KarmaManager;
import dev.eliasnvx.cozychristmas.present.BoomSettings;
import dev.eliasnvx.cozychristmas.present.PresentBlock;
import dev.eliasnvx.cozychristmas.present.PresentBlockEntity;
import dev.eliasnvx.cozychristmas.present.PresentContents;
import dev.eliasnvx.cozychristmas.present.PresentDesign;
import dev.eliasnvx.cozychristmas.present.PresentOutcome;
import dev.eliasnvx.cozychristmas.present.SnowPresents;
import dev.eliasnvx.cozychristmas.present.WrappingTableMenu;
import dev.eliasnvx.cozychristmas.registry.CozyBlocks;
import dev.eliasnvx.cozychristmas.registry.CozyComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

/**
 * Presents and the wrapping table on a real server, shared by both loaders (Fabric: {@code fabric/src/gametest},
 * NeoForge: {@code neoforge/.../dev/CozyGameTestsNeoForge}): contents surviving a break, gifts and the giver's karma,
 * karma deciding a surprise when a player comes close, the ticking warning, the boom's damage without block damage,
 * the wrapping table's menu and where presents appear in the snow. The odds and damage math are in JUnit. Dev only:
 * not in the released jars.
 */
public final class PresentGameTests {

    public static final List<GameTestEntry> ALL = List.of(
            new GameTestEntry("present_keeps_contents_when_broken", PresentGameTests::keepsContentsWhenBroken),
            new GameTestEntry("gift_goes_to_opener_and_thanks_giver", PresentGameTests::giftThanksGiver),
            new GameTestEntry("coal_and_loot_come_out", PresentGameTests::coalAndLoot),
            new GameTestEntry("boom_hurts_but_spares_blocks", PresentGameTests::boomSparesBlocks),
            new GameTestEntry("boom_spares_items_and_decorations", PresentGameTests::boomSparesItemsAndDecorations),
            new GameTestEntry("a_decided_present_stacks_with_nothing", PresentGameTests::decidedPresentStacksWithNothing),
            new GameTestEntry("boom_ticks_while_someone_is_near", PresentGameTests::boomTicksNearPlayers),
            new GameTestEntry("wrapping_table_wraps_gifts_and_pranks", PresentGameTests::wrappingTable),
            new GameTestEntry("snow_presents_land_on_powder_snow", PresentGameTests::snowPresentsOnPowderSnow),
            new GameTestEntry("snow_presents_need_snow_and_room", PresentGameTests::snowPresents));

    private static final BlockPos PRESENT = new BlockPos(2, 1, 2);
    private static final Vec3 BESIDE = new Vec3(3.5, 1.0, 2.5);
    private static final Vec3 FAR_AWAY = new Vec3(7.5, 1.0, 7.5);
    private static final double DROP_RADIUS = 3.0;
    /** A few of the present's player checks (every 10 ticks). */
    private static final int CHECKS = 25;

    private PresentGameTests() {
    }

    public static void keepsContentsWhenBroken(GameTestHelper helper) {
        PresentContents gift = gift(Items.DIAMOND, 3, Optional.empty());
        placePresent(helper, PresentDesign.GREEN, gift);
        helper.getLevel().destroyBlock(helper.absolutePos(PRESENT), true);
        List<ItemEntity> drops = drops(helper);
        helper.assertValueEqual(drops.size(), 1, "dropped stacks");
        ItemStack present = drops.getFirst().getItem();
        helper.assertTrue(present.is(CozyBlocks.PRESENT_ITEM.get()), "the drop is a present");
        helper.assertValueEqual(present.get(CozyComponents.PRESENT_CONTENTS.get()), gift, "contents after breaking");
        BlockItemStateProperties state = present.get(DataComponents.BLOCK_STATE);
        helper.assertValueEqual(state == null ? null : state.get(PresentBlock.DESIGN), PresentDesign.GREEN, "design after breaking");
        drops.forEach(Entity::discard);
        helper.succeed();
    }

    public static void giftThanksGiver(GameTestHelper helper) {
        ServerPlayer giver = TestPlayers.survival(helper, FAR_AWAY);
        ServerPlayer opener = TestPlayers.survival(helper, BESIDE);
        try {
            int thanks = CozyConfig.common().karma().giftGiven();
            Optional<PresentContents.Giver> from = Optional.of(PresentContents.Giver.of(giver));

            placePresent(helper, PresentDesign.RED, gift(Items.DIAMOND, 3, from)).open(opener);
            helper.assertBlockPresent(Blocks.AIR, PRESENT);
            helper.assertValueEqual(count(drops(helper), Items.DIAMOND), 3, "diamonds out of the gift");
            helper.assertValueEqual(KarmaManager.get(giver), thanks, "giver's karma after a gift");

            placePresent(helper, PresentDesign.RED, gift(Items.DIAMOND, 1, from)).open(opener);
            helper.assertValueEqual(KarmaManager.get(giver), thanks, "giver's karma after a second gift the same day");

            placePresent(helper, PresentDesign.RED, gift(Items.DIAMOND, 1, Optional.of(PresentContents.Giver.of(opener)))).open(opener);
            helper.assertValueEqual(KarmaManager.get(opener), 0, "karma for opening your own gift");
            drops(helper).forEach(Entity::discard);
            helper.succeed();
        } finally {
            TestPlayers.remove(helper, giver);
            TestPlayers.remove(helper, opener);
        }
    }

    public static void coalAndLoot(GameTestHelper helper) {
        ServerPlayer opener = TestPlayers.survival(helper, BESIDE);
        try {
            placePresent(helper, PresentDesign.BLUE, new PresentContents.Surprise(PresentOutcome.COAL)).open(opener);
            List<ItemEntity> coal = drops(helper);
            helper.assertTrue(!coal.isEmpty() && coal.stream().allMatch(item -> item.getItem().is(Items.COAL)),
                    "a coal present gives coal only");
            coal.forEach(Entity::discard);

            placePresent(helper, PresentDesign.GOLD, new PresentContents.Surprise(PresentOutcome.LOOT)).open(opener);
            List<ItemEntity> loot = drops(helper);
            helper.assertTrue(!loot.isEmpty(), "a loot present gives something");
            loot.forEach(Entity::discard);
            helper.succeed();
        } finally {
            TestPlayers.remove(helper, opener);
        }
    }

    public static void boomSparesBlocks(GameTestHelper helper) {
        BoomSettings boom = CozyConfig.common().presents().boom();
        BlockPos wall = PRESENT.west();
        helper.setBlock(wall, Blocks.GLASS);
        ServerPlayer opener = TestPlayers.survival(helper, BESIDE);
        try {
            float before = opener.getHealth();
            placePresent(helper, PresentDesign.RED, new PresentContents.Surprise(PresentOutcome.BOOM)).open(opener);
            float damage = before - opener.getHealth();
            helper.assertTrue(damage > 0.0F && damage <= boom.maxDamage(),
                    "boom damage " + damage + " should be in (0, " + boom.maxDamage() + "]");
            helper.assertBlockPresent(Blocks.AIR, PRESENT);
            if (!boom.breaksBlocks()) {
                helper.assertBlockPresent(Blocks.GLASS, wall);
            }
            helper.succeed();
        } finally {
            TestPlayers.remove(helper, opener);
        }
    }

    /** A prank ticks only while someone is near; an undecided surprise is decided by whoever comes close. */
    public static void boomTicksNearPlayers(GameTestHelper helper) {
        BlockPos surprise = PRESENT.north();
        placePresent(helper, PresentDesign.RED, new PresentContents.Prank(Optional.empty()));
        placePresent(helper, surprise, PresentDesign.GREEN, PresentContents.SURPRISE);
        ServerPlayer player = TestPlayers.survival(helper, BESIDE);
        helper.runAfterDelay(CHECKS, () -> {
            try {
                helper.assertBlockProperty(PRESENT, PresentBlock.TICKING, true);
                PresentContents decided = helper.<PresentBlockEntity>getBlockEntity(surprise).contents();
                helper.assertTrue(decided instanceof PresentContents.Surprise s && s.decided().isPresent(),
                        "a surprise next to a player is decided, got " + decided);
                player.moveTo(helper.absoluteVec(FAR_AWAY));
            } catch (RuntimeException | AssertionError e) {
                TestPlayers.remove(helper, player);
                throw e;
            }
            helper.runAfterDelay(CHECKS, () -> {
                try {
                    helper.assertBlockProperty(PRESENT, PresentBlock.TICKING, false);
                    helper.succeed();
                } finally {
                    TestPlayers.remove(helper, player);
                }
            });
        });
    }

    public static void wrappingTable(GameTestHelper helper) {
        BlockPos table = new BlockPos(5, 1, 5);
        helper.setBlock(table, CozyBlocks.WRAPPING_TABLE.get());
        ServerPlayer player = TestPlayers.survival(helper, new Vec3(5.5, 1.0, 4.0));
        try {
            WrappingTableMenu menu = new WrappingTableMenu(0, player.getInventory(),
                    ContainerLevelAccess.create(helper.getLevel(), helper.absolutePos(table)));
            helper.assertFalse(menu.getSlot(WrappingTableMenu.GIFT_SLOT).mayPlace(new ItemStack(CozyBlocks.PRESENT_ITEM.get())),
                    "a present can't be wrapped");

            menu.getSlot(WrappingTableMenu.GIFT_SLOT).set(new ItemStack(Items.DIAMOND, 5));
            menu.getSlot(WrappingTableMenu.PAPER_SLOT).set(new ItemStack(Items.PAPER, 2));
            menu.clickMenuButton(player, PresentDesign.BLUE.ordinal());
            menu.clicked(WrappingTableMenu.RESULT_SLOT, 0, ClickType.QUICK_MOVE, player);
            ItemStack present = findPresent(player);
            helper.assertValueEqual(present.get(CozyComponents.PRESENT_CONTENTS.get()),
                    gift(Items.DIAMOND, 5, Optional.of(PresentContents.Giver.of(player))), "wrapped gift");
            BlockItemStateProperties state = present.get(DataComponents.BLOCK_STATE);
            helper.assertValueEqual(state == null ? null : state.get(PresentBlock.DESIGN), PresentDesign.BLUE, "chosen design");
            helper.assertTrue(menu.getSlot(WrappingTableMenu.GIFT_SLOT).getItem().isEmpty(), "the whole gift stack is wrapped");
            helper.assertValueEqual(menu.getSlot(WrappingTableMenu.PAPER_SLOT).getItem().getCount(), 1, "paper left");
            player.getInventory().clearContent();

            menu.getSlot(WrappingTableMenu.GIFT_SLOT).set(new ItemStack(Items.TNT, 3));
            menu.clicked(WrappingTableMenu.RESULT_SLOT, 0, ClickType.PICKUP, player);
            helper.assertTrue(menu.getCarried().get(CozyComponents.PRESENT_CONTENTS.get()) instanceof PresentContents.Prank,
                    "TNT makes a prank");
            helper.assertValueEqual(menu.getSlot(WrappingTableMenu.GIFT_SLOT).getItem().getCount(), 2, "TNT left");
            helper.assertTrue(menu.getSlot(WrappingTableMenu.RESULT_SLOT).getItem().isEmpty(), "no paper, no present");
            menu.setCarried(ItemStack.EMPTY);

            menu.removed(player);
            helper.assertValueEqual(player.getInventory().countItem(Items.TNT), 2, "TNT back in the inventory on close");
            helper.succeed();
        } finally {
            TestPlayers.remove(helper, player);
        }
    }

    /** Where a December present may appear: snow under it, a thin layer on solid ground, no present too close. */
    public static void snowPresents(GameTestHelper helper) {
        int spacing = 5; // small, so presents of other tests running next to this one don't count
        RandomSource random = helper.getLevel().getRandom();
        BlockPos onStone = new BlockPos(1, 1, 6);
        BlockPos onSnowBlock = new BlockPos(1, 1, 1);
        BlockPos inSnowLayer = new BlockPos(4, 1, 1);
        helper.setBlock(onStone.below(), Blocks.STONE);
        helper.setBlock(onSnowBlock.below(), Blocks.SNOW_BLOCK);
        helper.setBlock(inSnowLayer.below(), Blocks.STONE);
        helper.setBlock(inSnowLayer, Blocks.SNOW);

        helper.assertFalse(SnowPresents.trySpawnAt(helper.getLevel(), helper.absolutePos(onStone), random, spacing),
                "a present on bare stone");
        helper.assertTrue(SnowPresents.trySpawnAt(helper.getLevel(), helper.absolutePos(onSnowBlock), random, spacing),
                "a present on a snow block");
        helper.assertBlockPresent(CozyBlocks.PRESENT.get(), onSnowBlock);
        helper.assertFalse(SnowPresents.trySpawnAt(helper.getLevel(), helper.absolutePos(inSnowLayer), random, spacing),
                "a present 3 blocks from another one");
        helper.setBlock(onSnowBlock, Blocks.AIR);
        helper.assertTrue(SnowPresents.trySpawnAt(helper.getLevel(), helper.absolutePos(inSnowLayer), random, spacing),
                "a present in a snow layer");
        helper.assertBlockPresent(CozyBlocks.PRESENT.get(), inSnowLayer);
        PresentContents contents = helper.<PresentBlockEntity>getBlockEntity(inSnowLayer).contents();
        helper.assertValueEqual(contents, PresentContents.SURPRISE, "a snow present is an undecided surprise");
        helper.succeed();
    }

    /** A prank goes off in your face, not in the loot of the present opened before it or the armor stand by the tree. */
    public static void boomSparesItemsAndDecorations(GameTestHelper helper) {
        ServerPlayer opener = TestPlayers.survival(helper, BESIDE);
        try {
            Vec3 near = Vec3.atBottomCenterOf(helper.absolutePos(PRESENT)).add(1.0, 0.0, 0.0);
            ItemEntity diamond = new ItemEntity(helper.getLevel(), near.x, near.y, near.z, new ItemStack(Items.DIAMOND));
            helper.getLevel().addFreshEntity(diamond);
            ArmorStand stand = helper.spawn(EntityType.ARMOR_STAND, PRESENT.relative(Direction.NORTH));
            placePresent(helper, PresentDesign.RED, new PresentContents.Surprise(PresentOutcome.BOOM)).open(opener);
            helper.assertTrue(diamond.isAlive(), "the diamond on the ground survives");
            helper.assertTrue(stand.isAlive(), "the armor stand survives");
            helper.succeed();
        } finally {
            TestPlayers.remove(helper, opener);
        }
    }

    /** A decided surprise, broken off as an item, mustn't stack with a known one: stacking would tell loot from coal. */
    public static void decidedPresentStacksWithNothing(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.survival(helper, BESIDE);
        try {
            placePresent(helper, PresentDesign.RED, new PresentContents.Surprise(PresentOutcome.LOOT));
            helper.getLevel().destroyBlock(helper.absolutePos(PRESENT), true, player);
            ItemStack dropped = helper.getLevel().getEntitiesOfClass(ItemEntity.class,
                            new AABB(helper.absolutePos(PRESENT)).inflate(DROP_RADIUS)).stream()
                    .map(ItemEntity::getItem).filter(stack -> stack.is(CozyBlocks.PRESENT_ITEM.get())).findFirst()
                    .orElseThrow(() -> new AssertionError("the present drops"));
            helper.assertValueEqual(dropped.getMaxStackSize(), 1, "a decided present doesn't stack");
            helper.succeed();
        } finally {
            TestPlayers.remove(helper, player);
        }
    }

    /**
     * Powder snow is snowy ground too (the decisions say so): presents sit on top of it. It isn't in
     * {@code #minecraft:blocks_motion_no_leaves}, so the motion heightmap stops in it: the spot handed over is the
     * powder snow itself. (The test area's barrier roof is in the heightmap's way, so the spot is given directly.)
     */
    public static void snowPresentsOnPowderSnow(GameTestHelper helper) {
        BlockPos column = new BlockPos(6, 0, 6);
        helper.setBlock(column, Blocks.STONE);
        helper.setBlock(column.above(), Blocks.POWDER_SNOW);
        helper.assertTrue(SnowPresents.trySpawnAt(helper.getLevel(), helper.absolutePos(column.above()),
                helper.getLevel().getRandom(), 0), "a present on powder snow");
        helper.assertBlockPresent(CozyBlocks.PRESENT.get(), column.above(2));
        helper.succeed();
    }

    private static PresentContents gift(Item item, int count,
                                        Optional<PresentContents.Giver> from) {
        return new PresentContents.Gift(new ItemStack(item, count), from);
    }

    private static PresentBlockEntity placePresent(GameTestHelper helper, PresentDesign design, PresentContents contents) {
        return placePresent(helper, PRESENT, design, contents);
    }

    private static PresentBlockEntity placePresent(GameTestHelper helper, BlockPos pos, PresentDesign design,
                                                   PresentContents contents) {
        helper.setBlock(pos.below(), Blocks.STONE);
        helper.setBlock(pos, CozyBlocks.PRESENT.get().defaultBlockState().setValue(PresentBlock.DESIGN, design));
        PresentBlockEntity present = helper.<PresentBlockEntity>getBlockEntity(pos);
        present.setContents(contents);
        return present;
    }

    private static List<ItemEntity> drops(GameTestHelper helper) {
        Vec3 center = helper.absoluteVec(Vec3.atCenterOf(PRESENT));
        return helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(center, center).inflate(DROP_RADIUS));
    }

    private static int count(List<ItemEntity> drops, Item item) {
        return drops.stream().filter(drop -> drop.getItem().is(item)).mapToInt(drop -> drop.getItem().getCount()).sum();
    }

    private static ItemStack findPresent(ServerPlayer player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(CozyBlocks.PRESENT_ITEM.get())) {
                return stack;
            }
        }
        throw new AssertionError("no present in the inventory");
    }
}
