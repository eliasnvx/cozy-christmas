package dev.eliasnvx.cozychristmas.gametest;

import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.platform.Platform;
import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.compat.snowyspirit.SnowySpiritGuards;
import dev.eliasnvx.cozychristmas.config.CozyConfig;
import dev.eliasnvx.cozychristmas.cosmetic.CosmeticSlot;
import dev.eliasnvx.cozychristmas.cosmetic.CosmeticsManager;
import dev.eliasnvx.cozychristmas.karma.KarmaManager;
import dev.eliasnvx.cozychristmas.present.PresentBlock;
import dev.eliasnvx.cozychristmas.present.PresentDesign;
import dev.eliasnvx.cozychristmas.registry.CozyBlocks;
import dev.eliasnvx.cozychristmas.registry.CozyEntities;
import dev.eliasnvx.cozychristmas.registry.CozyItems;
import dev.eliasnvx.cozychristmas.season.ChristmasSeason;
import dev.eliasnvx.cozychristmas.sleigh.SantasSleigh;
import dev.eliasnvx.cozychristmas.stocking.FireplaceBlock;
import dev.eliasnvx.cozychristmas.stocking.StockingBlock;
import dev.eliasnvx.cozychristmas.stocking.StockingBlockEntity;
import dev.eliasnvx.cozychristmas.stocking.StockingFill;
import dev.eliasnvx.cozychristmas.stocking.StockingStyle;
import dev.eliasnvx.cozychristmas.tree.OrnamentBlock;
import dev.eliasnvx.cozychristmas.tree.OrnamentColor;
import dev.eliasnvx.cozychristmas.tree.TreeStarBlockEntity;
import dev.eliasnvx.cozychristmas.workshop.Santa;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.AccessibilityOnboardingScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Dev-only check in a real client, on both loaders (1.21.1 has no client GameTest API): {@code ./gradlew
 * :fabric:runDevCheck}, {@code :fabric:runProdDevCheck} or {@code :neoforge:runDevCheck} opens the world
 * {@code saves/cozychristmas-check} from the title screen (flat; the Gradle tasks delete it first, so every run starts
 * fresh), on a fixed Christmas Eve.
 * It dresses the player in knitwear, lines up the mobs, builds a fireplace with stockings, presents, a decorated tree with
 * its gifts and Santa's loaded sleigh, takes screenshots (F2-style, {@code screenshots} in the run directory, named in
 * the log) by day and at night, checks a few things on the server and quits. With Snowy Spirit, Santa calls his
 * gingerbread golem guards into the lineup; with Jade, a shot of its tooltip on a stocking. Results are logged as
 * "DEVCHECK ok|FAIL ...". Not in the released jars, and only started by the loaders' dev glue with
 * {@code -Dcozychristmas.devcheck=true}.
 */
public final class DevCheck {

    private static final String WORLD = "cozychristmas-check";
    private static final LocalDate EVE = LocalDate.of(2026, 12, 24);
    private static final int SETTLE = 20;
    /** Where each scene stands, from the player's spawn block: the lineup north, fireplace east, tree west, sleigh far north. */
    private static final int LINEUP_NORTH = 8;
    private static final double LINEUP_SPACING = 2.5;
    private static final int SCENE_SIDE = 24;
    private static final int SCENE_NORTH = 4;
    private static final int SLEIGH_NORTH = 40;
    private static final int FLOOR = 48;
    private static final int LOADED = 14;
    private static final int TRUNK_HEIGHT = 9;
    /** (height above the ground, reach, diamond): the plaza tree's tiers. */
    private static final int[][] TIERS = {{1, 3, 4}, {2, 3, 4}, {3, 2, 3}, {4, 2, 3}, {5, 2, 2}, {6, 1, 2}, {7, 1, 1}, {8, 1, 1}};
    private static final double SIDE_CHANCE = 0.18;
    private static final double UNDER_CHANCE = 0.3;
    private static final long SEED = 1225L;
    private static final int TREE_GIFTS = 3;
    private static final Direction[] SIDES = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
    private static final double EYES = 1.62;

    private static final TreeMap<Integer, Consumer<Minecraft>> STEPS = new TreeMap<>();
    private static int ticks = -1;
    private static boolean opening;
    private static @Nullable Supplier<Screen> modList;
    private static BlockPos base = BlockPos.ZERO;

    private DevCheck() {
    }

    /** @param modList the loader's mod list screen, or null to skip that shot */
    public static void start(@Nullable Supplier<Screen> modList) {
        DevCheck.modList = modList;
        ChristmasSeason.setClock(Clock.fixed(EVE.atTime(12, 0).toInstant(ZoneOffset.UTC), ZoneOffset.UTC));
        plan();
        ClientTickEvent.CLIENT_POST.register(DevCheck::tick);
        CozyChristmas.LOGGER.info("DEVCHECK armed");
    }

    /** The whole run, one step after another; each delay is in client ticks after the previous step. */
    private static void plan() {
        int at = 2 * SETTLE;
        at = step(at, 0, mc -> server(mc, DevCheck::setUp));
        at = step(at, 2 * SETTLE, mc -> camera(mc, CameraType.THIRD_PERSON_FRONT, true));
        at = step(at, SETTLE, mc -> shot(mc, "outfit_front"));
        at = step(at, 2, mc -> camera(mc, CameraType.THIRD_PERSON_BACK, true));
        at = step(at, SETTLE, mc -> shot(mc, "outfit_back"));
        at = step(at, 2, mc -> {
            camera(mc, CameraType.FIRST_PERSON, false);
            mc.setScreen(new InventoryScreen(mc.player));
        });
        at = step(at, SETTLE, mc -> shot(mc, "inventory"));
        at = step(at, 2, mc -> {
            mc.setScreen(null);
            command(mc, "gamemode spectator @p");
            camera(mc, CameraType.FIRST_PERSON, true);
            look(mc, base.getX() + 0.5, base.getY(), base.getZ() + 5.5, base.getX() + 0.5, base.getY() + 1.0, base.getZ() - LINEUP_NORTH);
        });
        at = step(at, 2 * SETTLE, mc -> shot(mc, "mobs"));
        at = fireplaceAndTree(at, "day");
        at = step(at, 2, mc -> {
            BlockPos sleigh = base.north(SLEIGH_NORTH);
            look(mc, sleigh.getX() + 5.5, sleigh.getY(), sleigh.getZ() + 2.5, sleigh.getX(), sleigh.getY() + 0.6, sleigh.getZ() - 1.0);
        });
        at = step(at, 2 * SETTLE, mc -> shot(mc, "sleigh"));
        at = step(at, 2, mc -> command(mc, "time set midnight"));
        at = fireplaceAndTree(at, "night");
        at = step(at, 2, mc -> {
            // Jade: a stocking right ahead, from the ground, with the HUD
            command(mc, "time set noon");
            command(mc, "gamemode creative @p");
            BlockPos fireplace = base.east(SCENE_SIDE).north(SCENE_NORTH);
            look(mc, fireplace.getX() + 0.5, fireplace.getY(), fireplace.getZ() + 3.5,
                    fireplace.getX() + 0.5, fireplace.getY() + 1.5, fireplace.getZ() + 1.0);
            mc.options.hideGui = false;
        });
        at = step(at, 2 * SETTLE, mc -> {
            if (Platform.isModLoaded("jade")) {
                shot(mc, "jade_stocking");
            }
        });
        at = step(at, 2, mc -> server(mc, DevCheck::checks));
        at = step(at, 2, mc -> mc.setScreen(modList == null ? null : modList.get()));
        at = step(at, SETTLE, mc -> {
            if (modList != null) {
                shot(mc, "mod_list");
            }
        });
        step(at, 2, mc -> {
            CozyChristmas.LOGGER.info("DEVCHECK done");
            ChristmasSeason.setClock(Clock.systemDefaultZone());
            exitAfter(Thread.currentThread());
            mc.stop();
        });
    }

    private static int fireplaceAndTree(int at, String time) {
        at = step(at, 2, mc -> {
            BlockPos fireplace = base.east(SCENE_SIDE).north(SCENE_NORTH);
            look(mc, fireplace.getX() + 0.5, fireplace.getY(), fireplace.getZ() + 8.5,
                    fireplace.getX() + 0.5, fireplace.getY() + 1.0, fireplace.getZ());
        });
        at = step(at, 2 * SETTLE, mc -> shot(mc, "fireplace_" + time));
        at = step(at, 2, mc -> {
            BlockPos tree = base.west(SCENE_SIDE).north(SCENE_NORTH);
            look(mc, tree.getX() + 0.5, tree.getY() + 4, tree.getZ() + 13.5, tree.getX() + 0.5, tree.getY() + 5, tree.getZ() + 0.5);
        });
        return step(at, 2 * SETTLE, mc -> shot(mc, "tree_" + time));
    }

    private static int step(int at, int delay, Consumer<Minecraft> action) {
        int when = at + delay;
        STEPS.merge(when, action, Consumer::andThen);
        return when;
    }

    private static void tick(Minecraft mc) {
        // the dev window usually isn't focused: without this the game pauses and every shot is the pause menu
        mc.options.pauseOnLostFocus = false;
        // a fresh run directory starts on the accessibility onboarding screen, not the title screen
        if (mc.level == null && !opening && (mc.screen instanceof TitleScreen || mc.screen instanceof AccessibilityOnboardingScreen)) {
            opening = true;
            openWorld(mc);
        }
        if (mc.player == null || mc.getSingleplayerServer() == null) {
            return;
        }
        // advancement and recipe toasts sit over the inventory's corner
        mc.getToasts().clear();
        ticks++;
        Consumer<Minecraft> action = STEPS.get(ticks);
        if (action != null) {
            action.accept(mc);
        }
    }

    private static void openWorld(Minecraft mc) {
        if (mc.getLevelSource().levelExists(WORLD)) {
            mc.createWorldOpenFlows().openWorld(WORLD, () -> mc.setScreen(new TitleScreen()));
            return;
        }
        LevelSettings settings = new LevelSettings(WORLD, GameType.SURVIVAL, false, Difficulty.PEACEFUL, true, new GameRules(),
                WorldDataConfiguration.DEFAULT);
        mc.createWorldOpenFlows().createFreshLevel(WORLD, settings, new WorldOptions(WORLD.hashCode(), false, false),
                registries -> registries.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value()
                        .createWorldDimensions(), new TitleScreen());
    }

    private static void setUp(MinecraftServer server) {
        ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
        ServerLevel level = (ServerLevel) player.level();
        // from the world's spawn, not where the player left off last run: the scenes stand in the same place every time
        base = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, level.getSharedSpawnPos());
        run(server, "gamemode survival @p");
        // peaceful would remove the goblin, the gingerbread man and the snowman from the lineup (AI-less, they're harmless)
        run(server, "difficulty easy");
        run(server, "gamerule sendCommandFeedback false");
        run(server, "time set noon");
        run(server, "weather clear");
        run(server, "gamerule doMobSpawning false");
        run(server, "gamerule doDaylightCycle false");
        run(server, "kill @e[type=!player]");
        run(server, String.format(Locale.ROOT, "fill %d %d %d %d %d %d minecraft:snow_block", base.getX() - FLOOR, base.getY() - 1,
                base.getZ() - FLOOR - SLEIGH_NORTH / 2, base.getX() + FLOOR, base.getY() - 1, base.getZ() + FLOOR / 3));
        // face north, where the lineup stands: the front shot's camera then sits south with nothing in its way
        player.teleportTo(level, base.getX() + 0.5, base.getY(), base.getZ() + 0.5, 180.0F, 0.0F);

        CosmeticsManager.set(player, CosmeticSlot.HEAD, new ItemStack(CozyItems.POMPOM_HAT.get()));
        CosmeticsManager.set(player, CosmeticSlot.NECK, new ItemStack(CozyItems.SCARF.get()));
        CosmeticsManager.set(player, CosmeticSlot.BODY, new ItemStack(CozyItems.REINDEER_SWEATER.get()));
        CosmeticsManager.set(player, CosmeticSlot.HANDS, new ItemStack(CozyItems.MITTENS.get()));
        player.getInventory().clearContent();
        for (var sweater : CozyItems.sweaters()) {
            player.getInventory().add(new ItemStack(sweater.get()));
        }

        lineup(level);
        fireplace(level, base.east(SCENE_SIDE).north(SCENE_NORTH), player);
        tree(level, base.west(SCENE_SIDE).north(SCENE_NORTH), player);
        sleigh(level, base.north(SLEIGH_NORTH));
        CozyChristmas.LOGGER.info("DEVCHECK set up");
    }

    /** Every mob side by side, an elf carrying a present and a fawn among them; Santa at home calls his guards. */
    private static void lineup(ServerLevel level) {
        List<EntityType<? extends Mob>> types = List.of(CozyEntities.SANTA.get(), CozyEntities.ELF.get(), CozyEntities.REINDEER.get(),
                CozyEntities.REINDEER.get(), CozyEntities.KRAMPUS.get(), CozyEntities.PRESENT_GOBLIN.get(),
                CozyEntities.GINGERBREAD_MAN.get(), CozyEntities.EVIL_SNOWMAN.get());
        double first = base.getX() + 0.5 - LINEUP_SPACING * (types.size() - 1) / 2.0;
        for (int i = 0; i < types.size(); i++) {
            Mob mob = types.get(i).create(level);
            if (mob == null) {
                continue;
            }
            mob.moveTo(first + i * LINEUP_SPACING, base.getY(), base.getZ() - LINEUP_NORTH + 0.5, 0.0F, 0.0F);
            mob.setYHeadRot(0.0F);
            mob.setYBodyRot(0.0F);
            mob.setNoAi(true);
            mob.setPersistenceRequired();
            if (i == 1) {
                mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(CozyBlocks.PRESENT_ITEM.get()));
            }
            if (i == 3 && mob instanceof AgeableMob fawn) {
                fawn.setBaby(true);
            }
            level.addFreshEntity(mob);
            if (mob instanceof Santa santa) {
                santa.restrictTo(santa.blockPosition(), 16);
                santa.guardWorkshop(level);
            }
        }
    }

    /** Three fireplaces under a brick chimney breast, a stocking of each fill on them, the four presents and the table. */
    private static void fireplace(ServerLevel level, BlockPos center, ServerPlayer player) {
        StockingFill[] fills = StockingFill.values();
        for (int i = 0; i < fills.length; i++) {
            BlockPos pos = center.east(i - 1);
            level.setBlock(pos, CozyBlocks.FIREPLACE.get().defaultBlockState()
                    .setValue(FireplaceBlock.FACING, Direction.SOUTH).setValue(FireplaceBlock.LIT, true), Block.UPDATE_ALL);
            level.setBlock(pos.above(), Blocks.BRICKS.defaultBlockState(), Block.UPDATE_ALL);
            level.setBlock(pos.above(2), Blocks.BRICKS.defaultBlockState(), Block.UPDATE_ALL);
            BlockPos spot = pos.above().south();
            level.setBlock(spot, CozyBlocks.STOCKING.get().defaultBlockState()
                    .setValue(StockingBlock.FACING, Direction.SOUTH)
                    .setValue(StockingBlock.FILL, fills[i])
                    .setValue(StockingBlock.STYLE, StockingStyle.values()[i % StockingStyle.values().length]), Block.UPDATE_ALL);
            if (i == 0 && level.getBlockEntity(spot) instanceof StockingBlockEntity stocking) {
                stocking.hang(player, new ItemStack(CozyBlocks.STOCKING_ITEM.get()), StockingBlockEntity.morning(level));
            }
        }
        PresentDesign[] designs = PresentDesign.values();
        for (int i = 0; i < designs.length; i++) {
            level.setBlock(center.south(3).west(4).east(i), CozyBlocks.PRESENT.get().defaultBlockState()
                    .setValue(PresentBlock.DESIGN, designs[i]), Block.UPDATE_ALL);
        }
        level.setBlock(center.south(3).east(3), CozyBlocks.WRAPPING_TABLE.get().defaultBlockState(), Block.UPDATE_ALL);
    }

    /** A spruce built in tiers, ornaments hung at random but always the same, the star on top and its gifts under it. */
    private static void tree(ServerLevel level, BlockPos foot, ServerPlayer player) {
        BlockState log = Blocks.SPRUCE_LOG.defaultBlockState();
        BlockState leaves = Blocks.SPRUCE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
        for (int y = 0; y < TRUNK_HEIGHT; y++) {
            level.setBlock(foot.above(y), log, Block.UPDATE_ALL);
        }
        List<BlockPos> leafSpots = new ArrayList<>();
        for (int[] tier : TIERS) {
            for (int dx = -tier[1]; dx <= tier[1]; dx++) {
                for (int dz = -tier[1]; dz <= tier[1]; dz++) {
                    if ((dx != 0 || dz != 0) && Math.abs(dx) + Math.abs(dz) <= tier[2]) {
                        BlockPos pos = foot.offset(dx, tier[0], dz);
                        level.setBlock(pos, leaves, Block.UPDATE_ALL);
                        leafSpots.add(pos);
                    }
                }
            }
        }
        BlockPos top = foot.above(TRUNK_HEIGHT);
        level.setBlock(top, leaves, Block.UPDATE_ALL);
        BlockPos star = top.above();
        level.setBlock(star, CozyBlocks.TREE_STAR.get().defaultBlockState(), Block.UPDATE_ALL);

        Random random = new Random(SEED);
        OrnamentColor[] colors = OrnamentColor.values();
        for (BlockPos leaf : leafSpots) {
            for (Direction side : SIDES) {
                BlockPos spot = leaf.relative(side);
                if (level.getBlockState(spot).isAir() && random.nextDouble() < SIDE_CHANCE) {
                    hang(level, spot, colors[random.nextInt(colors.length)], AttachFace.WALL, side);
                }
            }
            BlockPos under = leaf.below();
            if (under.getY() > foot.getY() && level.getBlockState(under).isAir() && random.nextDouble() < UNDER_CHANCE) {
                hang(level, under, colors[random.nextInt(colors.length)], AttachFace.CEILING, Direction.NORTH);
            }
        }
        // its presents by the real rule, the morning moved back between them
        if (level.getBlockEntity(star) instanceof TreeStarBlockEntity entity) {
            long morning = StockingBlockEntity.morning(level);
            int gifts = 0;
            for (int i = 0; i < TREE_GIFTS; i++) {
                entity.putUp(player, morning - 1);
                KarmaManager.takeTreeGift(player, morning - 1);
                if (entity.tryGift(level) != null) {
                    gifts++;
                }
            }
            report(gifts == TREE_GIFTS, "the tree brought " + gifts + " of " + TREE_GIFTS + " presents");
        }
    }

    private static void hang(ServerLevel level, BlockPos pos, OrnamentColor color, AttachFace face, Direction facing) {
        BlockState state = CozyBlocks.ORNAMENTS.get(color).get().defaultBlockState()
                .setValue(OrnamentBlock.FACE, face).setValue(OrnamentBlock.FACING, facing);
        if (state.canSurvive(level, pos)) {
            level.setBlock(pos, state, Block.UPDATE_ALL);
        }
    }

    /** Santa's sleigh, half loaded; on Christmas Eve it harnesses its team on its next check. */
    private static void sleigh(ServerLevel level, BlockPos at) {
        SantasSleigh sleigh = CozyEntities.SANTAS_SLEIGH.get().create(level);
        if (sleigh == null) {
            report(false, "Santa's sleigh created");
            return;
        }
        sleigh.moveTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, 180.0F, 0.0F);
        level.addFreshEntity(sleigh);
        for (int i = 0; i < LOADED; i++) {
            sleigh.loadPresent(null);
        }
    }

    private static void checks(MinecraftServer server) {
        ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
        ServerLevel level = (ServerLevel) player.level();
        report(CosmeticsManager.get(player).get(CosmeticSlot.BODY).is(CozyItems.REINDEER_SWEATER.get()),
                "the sweater stored in the loader's attachment");
        List<SantasSleigh> sleighs = level.getEntitiesOfClass(SantasSleigh.class, new AABB(base.north(SLEIGH_NORTH)).inflate(8.0));
        report(!sleighs.isEmpty() && sleighs.getFirst().isHarnessed(), "the sleigh harnessed its team on Christmas Eve");
        if (SnowySpiritGuards.available()) {
            long golems = level.getEntities((Entity) null, new AABB(base.north(LINEUP_NORTH)).inflate(24.0),
                    entity -> SnowySpiritGuards.GINGERBREAD_GOLEM.equals(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()))).size();
            report(golems == CozyConfig.common().workshop().santa().snowySpiritGuards(), "Santa called " + golems + " gingerbread golems");
        }
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() -> report(mc.player != null
                && CosmeticsManager.looks(mc.player).get(CosmeticSlot.BODY).is(CozyItems.REINDEER_SWEATER.get()),
                "cosmetics synced to the client"));
    }

    /**
     * Architectury Transformer's dev runtime leaves non-daemon thread pools behind, so the JVM never exits after a
     * clean stop: exit once the render thread has finished shutting down.
     */
    private static void exitAfter(Thread render) {
        Thread exit = new Thread(() -> {
            try {
                render.join();
            } catch (InterruptedException e) {
                return;
            }
            System.exit(0);
        }, "cozychristmas-devcheck-exit");
        exit.setDaemon(true);
        exit.start();
    }

    private static void report(boolean ok, String what) {
        CozyChristmas.LOGGER.info("DEVCHECK {} {}", ok ? "ok" : "FAIL", what);
    }

    private static void server(Minecraft mc, Consumer<MinecraftServer> task) {
        MinecraftServer server = mc.getSingleplayerServer();
        server.execute(() -> task.accept(server));
    }

    private static void run(MinecraftServer server, String command) {
        server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), command);
    }

    private static void command(Minecraft mc, String command) {
        server(mc, server -> run(server, command));
    }

    /** Puts the player's eyes (not feet) at {@code from}'s eye height, looking at the given point. */
    private static void look(Minecraft mc, double x, double y, double z, double atX, double atY, double atZ) {
        Vec3 delta = new Vec3(atX, atY, atZ).subtract(x, y + EYES, z);
        double yaw = Math.toDegrees(Math.atan2(-delta.x, delta.z));
        double pitch = Math.toDegrees(-Math.atan2(delta.y, Math.sqrt(delta.x * delta.x + delta.z * delta.z)));
        command(mc, String.format(Locale.ROOT, "tp @p %.2f %.2f %.2f %.1f %.1f", x, y, z, yaw, pitch));
    }

    private static void camera(Minecraft mc, CameraType type, boolean hideGui) {
        mc.options.setCameraType(type);
        mc.options.hideGui = hideGui;
    }

    private static void shot(Minecraft mc, String name) {
        mc.gui.getChat().clearMessages(false);
        // like F2: a timestamped file in run/screenshots; the log says which shot it is
        CozyChristmas.LOGGER.info("DEVCHECK screenshot {}{}", Platform.isNeoForge() ? "neoforge_" : "fabric_", name);
        Screenshot.grab(mc.gameDirectory, mc.getMainRenderTarget(), message -> {
        });
    }
}
