package dev.eliasnvx.cozychristmas.gametest;

import dev.eliasnvx.cozychristmas.karma.KarmaManager;
import dev.eliasnvx.cozychristmas.present.PresentContents;
import dev.eliasnvx.cozychristmas.present.PresentOutcome;
import dev.eliasnvx.cozychristmas.registry.CozyBlocks;
import dev.eliasnvx.cozychristmas.registry.CozyComponents;
import dev.eliasnvx.cozychristmas.season.ChristmasSeason;
import dev.eliasnvx.cozychristmas.stocking.FireplaceBlock;
import dev.eliasnvx.cozychristmas.stocking.StockingBlock;
import dev.eliasnvx.cozychristmas.stocking.StockingBlockEntity;
import dev.eliasnvx.cozychristmas.stocking.StockingFill;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

/**
 * The fireplace and stockings on a real server, shared by both loaders (Fabric: {@code fabric/src/gametest}, NeoForge:
 * {@code neoforge/.../dev/CozyGameTestsNeoForge}). The season clock is fixed and restored within each test body, which
 * runs in one tick. Dev only: not in the released jars.
 */
public final class StockingGameTests {

    public static final List<GameTestEntry> ALL = List.of(
            new GameTestEntry("stocking_fills_by_karma_once_a_morning", StockingGameTests::fillsByKarmaOnceAMorning),
            new GameTestEntry("stocking_needs_a_fireplace_and_the_season", StockingGameTests::needsAFireplaceAndTheSeason),
            new GameTestEntry("only_the_owner_empties_a_stocking", StockingGameTests::onlyTheOwnerEmptiesIt),
            new GameTestEntry("only_the_owner_takes_a_stocking_down", StockingGameTests::onlyTheOwnerTakesItDown),
            new GameTestEntry("taking_a_stocking_down_drops_it_and_its_contents", StockingGameTests::takingItDownDrops),
            new GameTestEntry("fireplace_lights_and_goes_out", StockingGameTests::fireplaceLightsAndGoesOut));

    private static final BlockPos FIREPLACE = new BlockPos(2, 1, 2);
    private static final BlockPos MANTEL = FIREPLACE.above();
    private static final BlockPos STOCKING = MANTEL.south();
    private static final BlockPos SECOND_FIREPLACE = new BlockPos(5, 1, 2);
    private static final BlockPos SECOND_STOCKING = SECOND_FIREPLACE.above().south();
    private static final Vec3 PLAYER = new Vec3(2.5, 1.0, 5.5);
    private static final Vec3 OTHER_PLAYER = new Vec3(4.5, 1.0, 5.5);
    private static final LocalDate IN_SEASON = LocalDate.of(2026, 12, 24);
    private static final LocalDate OUT_OF_SEASON = LocalDate.of(2026, 7, 1);
    private static final int NICE = 60;
    private static final int NAUGHTY = -60;

    private StockingGameTests() {
    }

    /** A nice owner finds a gift, at most one stocking a morning; a naughty one finds coal. */
    public static void fillsByKarmaOnceAMorning(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.survival(helper, PLAYER);
        ChristmasSeason.setClock(fixed(IN_SEASON));
        try {
            StockingBlockEntity first = hang(helper, FIREPLACE, STOCKING, player);
            StockingBlockEntity second = hang(helper, SECOND_FIREPLACE, SECOND_STOCKING, player);
            KarmaManager.set(player, NICE);
            helper.assertTrue(first.tryFill(helper.getLevel()), "a new morning fills the stocking");
            ItemStack gift = first.contents();
            helper.assertTrue(gift.is(CozyBlocks.PRESENT_ITEM.get()), "a nice owner finds a present, got " + gift);
            helper.assertValueEqual(gift.get(CozyComponents.PRESENT_CONTENTS.get()),
                    new PresentContents.Surprise(Optional.of(PresentOutcome.LOOT)), "and it's a gift");
            helper.assertValueEqual(helper.getBlockState(STOCKING).getValue(StockingBlock.FILL), StockingFill.PRESENT,
                    "the present shows");
            helper.assertFalse(second.tryFill(helper.getLevel()), "one stocking a morning per player");
            helper.assertFalse(first.tryFill(helper.getLevel()), "a full stocking stays as it is");

            KarmaManager.set(player, NAUGHTY);
            long morning = StockingBlockEntity.morning(helper.getLevel());
            // as if this morning's stocking had been yesterday's: the clock can't be moved under the other tests
            KarmaManager.takeStockingGift(player, morning - 1);
            helper.assertTrue(second.tryFill(helper.getLevel()), "the second stocking fills on another morning");
            helper.assertTrue(second.contents().is(Items.COAL), "a naughty owner finds coal, got " + second.contents());
            helper.succeed();
        } finally {
            ChristmasSeason.setClock(Clock.systemDefaultZone());
            TestPlayers.remove(helper, player);
        }
    }

    public static void needsAFireplaceAndTheSeason(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.survival(helper, PLAYER);
        try {
            StockingBlockEntity stocking = hang(helper, FIREPLACE, STOCKING, player);
            ChristmasSeason.setClock(fixed(OUT_OF_SEASON));
            helper.assertFalse(stocking.tryFill(helper.getLevel()), "nothing outside the Christmas season");
            ChristmasSeason.setClock(fixed(IN_SEASON));
            helper.setBlock(FIREPLACE, Blocks.STONE_BRICKS);
            helper.assertFalse(StockingBlockEntity.hangsOverFireplace(helper.getLevel(), helper.absolutePos(STOCKING),
                    helper.getBlockState(STOCKING)), "no fireplace under the mantel any more");
            helper.assertFalse(stocking.tryFill(helper.getLevel()), "nothing without a fireplace");
            helper.setBlock(FIREPLACE, CozyBlocks.FIREPLACE.get().defaultBlockState().setValue(FireplaceBlock.FACING, Direction.SOUTH));
            helper.assertTrue(stocking.tryFill(helper.getLevel()), "back over a fireplace, in season, it fills");
            helper.succeed();
        } finally {
            ChristmasSeason.setClock(Clock.systemDefaultZone());
            TestPlayers.remove(helper, player);
        }
    }

    public static void onlyTheOwnerEmptiesIt(GameTestHelper helper) {
        ServerPlayer owner = TestPlayers.survival(helper, PLAYER);
        ServerPlayer other = TestPlayers.survival(helper, OTHER_PLAYER);
        ChristmasSeason.setClock(fixed(IN_SEASON));
        try {
            StockingBlockEntity stocking = hang(helper, FIREPLACE, STOCKING, owner);
            helper.assertTrue(stocking.tryFill(helper.getLevel()), "it fills");
            BlockState state = helper.getBlockState(STOCKING);
            BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(helper.absolutePos(STOCKING)), Direction.SOUTH,
                    helper.absolutePos(STOCKING), false);
            state.useWithoutItem(helper.getLevel(), other, hit);
            helper.assertFalse(stocking.contents().isEmpty(), "someone else's click leaves it full");
            state.useWithoutItem(helper.getLevel(), owner, hit);
            helper.assertTrue(stocking.contents().isEmpty(), "the owner empties it");
            helper.assertTrue(owner.getInventory().contains(stack -> stack.is(CozyBlocks.PRESENT_ITEM.get())), "into their pockets");
            helper.assertValueEqual(helper.getBlockState(STOCKING).getValue(StockingBlock.FILL), StockingFill.EMPTY, "shows empty");
            helper.succeed();
        } finally {
            ChristmasSeason.setClock(Clock.systemDefaultZone());
            TestPlayers.remove(helper, owner);
            TestPlayers.remove(helper, other);
        }
    }

    /** Someone else can't just break the stocking and keep what's inside: only its owner (or creative) takes it down. */
    public static void onlyTheOwnerTakesItDown(GameTestHelper helper) {
        ServerPlayer owner = TestPlayers.survival(helper, PLAYER);
        ServerPlayer other = TestPlayers.survival(helper, OTHER_PLAYER);
        try {
            hang(helper, FIREPLACE, STOCKING, owner);
            BlockState state = helper.getBlockState(STOCKING);
            BlockPos pos = helper.absolutePos(STOCKING);
            helper.assertValueEqual(state.getDestroyProgress(other, helper.getLevel(), pos), 0.0F, "another player can't break it");
            helper.assertTrue(state.getDestroyProgress(owner, helper.getLevel(), pos) > 0.0F, "its owner can");
            helper.succeed();
        } finally {
            TestPlayers.remove(helper, owner);
            TestPlayers.remove(helper, other);
        }
    }

    public static void takingItDownDrops(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.survival(helper, PLAYER);
        ChristmasSeason.setClock(fixed(IN_SEASON));
        try {
            StockingBlockEntity stocking = hang(helper, FIREPLACE, STOCKING, player);
            helper.assertTrue(stocking.tryFill(helper.getLevel()), "it fills");
            helper.destroyBlock(STOCKING);
            List<ItemEntity> drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class,
                    new AABB(helper.absolutePos(STOCKING)).inflate(2.0));
            helper.assertTrue(drops.stream().anyMatch(drop -> drop.getItem().is(CozyBlocks.STOCKING_ITEM.get())), "the stocking drops");
            helper.assertTrue(drops.stream().anyMatch(drop -> drop.getItem().is(CozyBlocks.PRESENT_ITEM.get())), "and what it held");
            helper.succeed();
        } finally {
            ChristmasSeason.setClock(Clock.systemDefaultZone());
            TestPlayers.remove(helper, player);
        }
    }

    public static void fireplaceLightsAndGoesOut(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.survival(helper, PLAYER);
        try {
            helper.setBlock(FIREPLACE, CozyBlocks.FIREPLACE.get().defaultBlockState().setValue(FireplaceBlock.FACING, Direction.SOUTH));
            BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(helper.absolutePos(FIREPLACE)), Direction.SOUTH,
                    helper.absolutePos(FIREPLACE), false);
            ItemStack flint = new ItemStack(Items.FLINT_AND_STEEL);
            player.setItemInHand(InteractionHand.MAIN_HAND, flint);
            helper.getBlockState(FIREPLACE).useItemOn(flint, helper.getLevel(), player, InteractionHand.MAIN_HAND, hit);
            BlockState lit = helper.getBlockState(FIREPLACE);
            helper.assertTrue(lit.getValue(FireplaceBlock.LIT), "flint and steel lights it");
            helper.assertValueEqual(lit.getLightEmission(), FireplaceBlock.LIGHT, "a lit fireplace glows");
            helper.assertValueEqual(flint.getDamageValue(), 1, "the flint and steel wears");
            ItemStack shovel = new ItemStack(Items.IRON_SHOVEL);
            player.setItemInHand(InteractionHand.MAIN_HAND, shovel);
            lit.useItemOn(shovel, helper.getLevel(), player, InteractionHand.MAIN_HAND, hit);
            helper.assertFalse(helper.getBlockState(FIREPLACE).getValue(FireplaceBlock.LIT), "a shovel puts it out");
            helper.assertValueEqual(helper.getBlockState(FIREPLACE).getLightEmission(), 0, "dark again");
            helper.succeed();
        } finally {
            TestPlayers.remove(helper, player);
        }
    }

    /** A fireplace facing south, a mantel block above it and a stocking hung on its front, hung "yesterday". */
    private static StockingBlockEntity hang(GameTestHelper helper, BlockPos fireplace, BlockPos stocking, ServerPlayer owner) {
        helper.setBlock(fireplace, CozyBlocks.FIREPLACE.get().defaultBlockState().setValue(FireplaceBlock.FACING, Direction.SOUTH));
        helper.setBlock(fireplace.above(), Blocks.BRICKS);
        helper.setBlock(stocking, CozyBlocks.STOCKING.get().defaultBlockState().setValue(StockingBlock.FACING, Direction.SOUTH));
        StockingBlockEntity entity = helper.getBlockEntity(stocking, StockingBlockEntity.class);
        entity.hang(owner, new ItemStack(CozyBlocks.STOCKING_ITEM.get()), StockingBlockEntity.morning(helper.getLevel()) - 1);
        return entity;
    }

    private static Clock fixed(LocalDate date) {
        return Clock.fixed(date.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC);
    }
}
