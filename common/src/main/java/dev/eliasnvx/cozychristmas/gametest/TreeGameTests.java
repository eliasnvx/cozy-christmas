package dev.eliasnvx.cozychristmas.gametest;

import dev.eliasnvx.cozychristmas.config.CozyConfig;
import dev.eliasnvx.cozychristmas.karma.KarmaManager;
import dev.eliasnvx.cozychristmas.present.PresentBlockEntity;
import dev.eliasnvx.cozychristmas.present.PresentContents;
import dev.eliasnvx.cozychristmas.registry.CozyBlocks;
import dev.eliasnvx.cozychristmas.season.ChristmasSeason;
import dev.eliasnvx.cozychristmas.stocking.StockingBlockEntity;
import dev.eliasnvx.cozychristmas.tree.ChristmasTree;
import dev.eliasnvx.cozychristmas.tree.OrnamentBlock;
import dev.eliasnvx.cozychristmas.tree.OrnamentColor;
import dev.eliasnvx.cozychristmas.tree.TreeSettings;
import dev.eliasnvx.cozychristmas.tree.TreeStarBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.Vec3;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

/**
 * The Christmas tree on a real server, shared by both loaders (Fabric: {@code fabric/src/gametest}, NeoForge:
 * {@code neoforge/.../dev/CozyGameTestsNeoForge}). A small spruce on a stone floor: a trunk three logs high, two
 * tiers of leaves, the star on top and ornaments hung under the lower tier. The season clock is fixed and restored
 * within each test body, which runs in one tick. Dev only: not in the released jars.
 */
public final class TreeGameTests {

    public static final List<GameTestEntry> ALL = List.of(
            new GameTestEntry("tree_brings_a_present_once_a_morning", TreeGameTests::bringsAPresentOnceAMorning),
            new GameTestEntry("tree_needs_ornaments_the_season_and_an_owner", TreeGameTests::needsOrnamentsSeasonAndOwner),
            new GameTestEntry("one_tree_a_morning_per_player", TreeGameTests::oneTreeAMorningPerPlayer),
            new GameTestEntry("presents_under_a_tree_stop_piling_up", TreeGameTests::presentsStopPilingUp),
            new GameTestEntry("ornaments_and_the_star_hold_on_to_the_tree", TreeGameTests::ornamentsAndStarHoldOn));

    private static final int SIZE = 8;
    private static final BlockPos TRUNK = new BlockPos(3, 1, 3);
    private static final int TRUNK_HEIGHT = 3;
    private static final BlockPos STAR = TRUNK.above(5);
    /** Under the lower tier's leaves: hung from them, clear of the ground where presents go. */
    private static final List<BlockPos> ORNAMENT_SPOTS = List.of(new BlockPos(1, 2, 3), new BlockPos(5, 2, 3),
            new BlockPos(3, 2, 1), new BlockPos(3, 2, 5), new BlockPos(2, 2, 2), new BlockPos(4, 2, 4));
    private static final BlockPos SMALL_TREE = new BlockPos(6, 1, 6);
    private static final BlockPos SMALL_STAR = SMALL_TREE.above(2);
    private static final Vec3 PLAYER = new Vec3(0.5, 1.0, 7.5);
    private static final LocalDate IN_SEASON = LocalDate.of(2026, 12, 20);
    private static final LocalDate OUT_OF_SEASON = LocalDate.of(2026, 7, 1);

    private TreeGameTests() {
    }

    /** A decorated tree: one present on the ground by the trunk, already decided, and no second one that morning. */
    public static void bringsAPresentOnceAMorning(GameTestHelper helper) {
        ServerPlayer owner = TestPlayers.survival(helper, PLAYER);
        ChristmasSeason.setClock(fixed(IN_SEASON));
        try {
            TreeStarBlockEntity star = tree(helper, owner, ORNAMENT_SPOTS.size());
            BlockPos spot = star.tryGift(helper.getLevel());
            helper.assertTrue(spot != null, "a decorated tree brings a present on a new morning");
            BlockPos base = helper.absolutePos(TRUNK);
            helper.assertValueEqual(helper.getLevel().getBlockState(spot).getBlock(), CozyBlocks.PRESENT.get(), "a present");
            helper.assertTrue(spot.getY() == base.getY() && spot.distManhattan(base) > 0
                    && Math.max(Math.abs(spot.getX() - base.getX()), Math.abs(spot.getZ() - base.getZ())) <= settings().presentRadius(),
                    "on the ground by the trunk, at " + helper.relativePos(spot));
            PresentContents contents = ((PresentBlockEntity) helper.getLevel().getBlockEntity(spot)).contents();
            helper.assertTrue(contents instanceof PresentContents.Surprise surprise && surprise.decided().isPresent(),
                    "the owner's karma already decided it, got " + contents);
            helper.assertTrue(star.tryGift(helper.getLevel()) == null, "one present a morning");
            helper.assertValueEqual(presents(helper), 1, "presents under the tree");

            long morning = StockingBlockEntity.morning(helper.getLevel());
            // as if this morning's present had been yesterday's: the clock can't be moved under the other tests
            star.putUp(owner, morning - 1);
            KarmaManager.takeTreeGift(owner, morning - 1);
            helper.assertTrue(star.tryGift(helper.getLevel()) != null, "another morning, another present");
            helper.assertValueEqual(presents(helper), 2, "presents under the tree");
            helper.succeed();
        } finally {
            ChristmasSeason.setClock(Clock.systemDefaultZone());
            TestPlayers.remove(helper, owner);
        }
    }

    public static void needsOrnamentsSeasonAndOwner(GameTestHelper helper) {
        ServerPlayer owner = TestPlayers.survival(helper, PLAYER);
        ChristmasSeason.setClock(fixed(IN_SEASON));
        try {
            TreeStarBlockEntity star = tree(helper, owner, settings().minOrnaments() - 1);
            ChristmasTree.Scan scan = ChristmasTree.scan(helper.getLevel(), helper.absolutePos(STAR), settings());
            helper.assertValueEqual(scan.base(), helper.absolutePos(TRUNK), "the trunk's base");
            helper.assertValueEqual(scan.ornaments(), settings().minOrnaments() - 1, "ornaments counted");
            helper.assertValueEqual(ChristmasTree.status(scan, settings()), ChristmasTree.Status.BARE, "too few ornaments");
            helper.assertTrue(star.tryGift(helper.getLevel()) == null, "a bare tree brings nothing");

            hangOrnament(helper, ORNAMENT_SPOTS.get(settings().minOrnaments() - 1), OrnamentColor.GOLD);
            helper.assertValueEqual(ChristmasTree.status(ChristmasTree.scan(helper.getLevel(), helper.absolutePos(STAR), settings()),
                    settings()), ChristmasTree.Status.READY, "decorated now");
            ChristmasSeason.setClock(fixed(OUT_OF_SEASON));
            helper.assertTrue(star.tryGift(helper.getLevel()) == null, "nothing outside the Christmas season");
            ChristmasSeason.setClock(fixed(IN_SEASON));

            // a star set by a structure has no owner and brings nothing
            helper.setBlock(STAR, Blocks.AIR);
            helper.setBlock(STAR, CozyBlocks.TREE_STAR.get());
            TreeStarBlockEntity ownerless = helper.getBlockEntity(STAR, TreeStarBlockEntity.class);
            helper.assertTrue(ownerless.owner() == null && ownerless.tryGift(helper.getLevel()) == null, "no owner, no present");

            // a player putting the star up owns the tree
            BlockState starState = helper.getBlockState(STAR);
            starState.getBlock().setPlacedBy(helper.getLevel(), helper.absolutePos(STAR), starState, owner,
                    new ItemStack(CozyBlocks.TREE_STAR_ITEM.get()));
            helper.assertValueEqual(ownerless.owner(), owner.getUUID(), "whoever puts the star up owns the tree");
            helper.assertTrue(ownerless.tryGift(helper.getLevel()) == null, "from the next morning on");
            ownerless.putUp(owner, StockingBlockEntity.morning(helper.getLevel()) - 1);
            helper.assertTrue(ownerless.tryGift(helper.getLevel()) != null, "decorated, in season, owned: a present");
            helper.succeed();
        } finally {
            ChristmasSeason.setClock(Clock.systemDefaultZone());
            TestPlayers.remove(helper, owner);
        }
    }

    /** Two of a player's trees: only one gets the morning's present. */
    public static void oneTreeAMorningPerPlayer(GameTestHelper helper) {
        ServerPlayer owner = TestPlayers.survival(helper, PLAYER);
        ChristmasSeason.setClock(fixed(IN_SEASON));
        try {
            TreeStarBlockEntity first = tree(helper, owner, ORNAMENT_SPOTS.size());
            // a sapling-sized second tree; the big one's ornaments are within its reach
            helper.setBlock(SMALL_TREE, Blocks.SPRUCE_LOG);
            helper.setBlock(SMALL_TREE.above(), leaves());
            helper.setBlock(SMALL_STAR, CozyBlocks.TREE_STAR.get());
            TreeStarBlockEntity second = helper.getBlockEntity(SMALL_STAR, TreeStarBlockEntity.class);
            second.putUp(owner, StockingBlockEntity.morning(helper.getLevel()) - 1);
            helper.assertTrue(first.tryGift(helper.getLevel()) != null, "the first tree brings the morning's present");
            helper.assertTrue(second.tryGift(helper.getLevel()) == null, "the second waits for another morning");
            helper.succeed();
        } finally {
            ChristmasSeason.setClock(Clock.systemDefaultZone());
            TestPlayers.remove(helper, owner);
        }
    }

    public static void presentsStopPilingUp(GameTestHelper helper) {
        ServerPlayer owner = TestPlayers.survival(helper, PLAYER);
        ChristmasSeason.setClock(fixed(IN_SEASON));
        try {
            TreeStarBlockEntity star = tree(helper, owner, ORNAMENT_SPOTS.size());
            List<BlockPos> heap = List.of(TRUNK.east(), TRUNK.west(), TRUNK.north(), TRUNK.south());
            for (int i = 0; i < settings().maxPresents(); i++) {
                helper.setBlock(heap.get(i), CozyBlocks.PRESENT.get());
            }
            helper.assertTrue(star.tryGift(helper.getLevel()) == null, "no room: presents nobody picked up are still there");
            helper.setBlock(heap.getFirst(), Blocks.AIR);
            helper.assertTrue(star.tryGift(helper.getLevel()) != null, "one picked up, a new one comes the same morning");
            helper.succeed();
        } finally {
            ChristmasSeason.setClock(Clock.systemDefaultZone());
            TestPlayers.remove(helper, owner);
        }
    }

    public static void ornamentsAndStarHoldOn(GameTestHelper helper) {
        floor(helper);
        BlockPos leaf = new BlockPos(3, 4, 3);
        helper.setBlock(leaf, leaves());
        BlockPos under = leaf.below();
        BlockPos side = leaf.east();
        BlockState hanging = ornament(OrnamentColor.RED, AttachFace.CEILING, Direction.NORTH);
        BlockState onSide = ornament(OrnamentColor.BLUE, AttachFace.WALL, Direction.EAST);
        helper.assertTrue(hanging.canSurvive(helper.getLevel(), helper.absolutePos(under)), "hangs under leaves");
        helper.assertTrue(onSide.canSurvive(helper.getLevel(), helper.absolutePos(side)), "hangs on the side of leaves");
        helper.assertFalse(onSide.canSurvive(helper.getLevel(), helper.absolutePos(side.east())), "not in the air");
        helper.setBlock(under, hanging);
        helper.setBlock(side, onSide);
        BlockPos star = leaf.above();
        helper.setBlock(star, CozyBlocks.TREE_STAR.get());
        helper.assertTrue(CozyBlocks.TREE_STAR.get().defaultBlockState().canSurvive(helper.getLevel(), helper.absolutePos(star)),
                "the star stands on leaves");

        helper.setBlock(leaf, Blocks.AIR);
        helper.assertBlockPresent(Blocks.AIR, under);
        helper.assertBlockPresent(Blocks.AIR, side);
        helper.assertBlockPresent(Blocks.AIR, star);
        helper.succeed();
    }

    /**
     * The test tree, owned by {@code owner} as if the star went up yesterday, with {@code ornaments} of
     * {@link #ORNAMENT_SPOTS} hung.
     */
    private static TreeStarBlockEntity tree(GameTestHelper helper, ServerPlayer owner, int ornaments) {
        floor(helper);
        for (int y = 0; y < TRUNK_HEIGHT; y++) {
            helper.setBlock(TRUNK.above(y), Blocks.SPRUCE_LOG);
        }
        BlockPos lowTier = TRUNK.above(TRUNK_HEIGHT - 1);
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                int ring = Math.abs(dx) + Math.abs(dz);
                if (ring > 0 && ring <= 2) {
                    helper.setBlock(lowTier.offset(dx, 0, dz), leaves());
                }
                if (ring <= 1) {
                    helper.setBlock(lowTier.offset(dx, 1, dz), leaves());
                }
            }
        }
        helper.setBlock(lowTier.above(2), leaves());
        helper.setBlock(STAR, CozyBlocks.TREE_STAR.get());
        for (int i = 0; i < ornaments; i++) {
            hangOrnament(helper, ORNAMENT_SPOTS.get(i), OrnamentColor.values()[i % OrnamentColor.values().length]);
        }
        TreeStarBlockEntity star = helper.getBlockEntity(STAR, TreeStarBlockEntity.class);
        star.putUp(owner, StockingBlockEntity.morning(helper.getLevel()) - 1);
        return star;
    }

    private static void floor(GameTestHelper helper) {
        for (int x = 0; x < SIZE; x++) {
            for (int z = 0; z < SIZE; z++) {
                helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
            }
        }
    }

    private static BlockState leaves() {
        return Blocks.SPRUCE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
    }

    private static void hangOrnament(GameTestHelper helper, BlockPos pos, OrnamentColor color) {
        helper.setBlock(pos, ornament(color, AttachFace.CEILING, Direction.NORTH));
    }

    private static BlockState ornament(OrnamentColor color, AttachFace face, Direction facing) {
        return CozyBlocks.ORNAMENTS.get(color).get().defaultBlockState().setValue(OrnamentBlock.FACE, face)
                .setValue(OrnamentBlock.FACING, facing);
    }

    private static int presents(GameTestHelper helper) {
        return ChristmasTree.presentsAround(helper.getLevel(), helper.absolutePos(TRUNK), settings());
    }

    private static TreeSettings settings() {
        return CozyConfig.common().tree();
    }

    private static Clock fixed(LocalDate date) {
        return Clock.fixed(date.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC);
    }
}
