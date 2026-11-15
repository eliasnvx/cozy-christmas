package dev.eliasnvx.cozychristmas.gametest;

import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.config.CozyConfig;
import dev.eliasnvx.cozychristmas.goblin.GingerbreadMan;
import dev.eliasnvx.cozychristmas.goblin.PresentGoblin;
import dev.eliasnvx.cozychristmas.karma.KarmaManager;
import dev.eliasnvx.cozychristmas.registry.CozyBlocks;
import dev.eliasnvx.cozychristmas.registry.CozyEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Present goblins, gingerbread men and their camp on a real server, shared by both loaders (Fabric:
 * {@code fabric/src/gametest}, NeoForge: {@code neoforge/.../dev/CozyGameTestsNeoForge}). Dev only: not in the
 * released jars.
 */
public final class GoblinGameTests {

    public static final List<GameTestEntry> ALL = List.of(
            new GameTestEntry("present_goblin_snatches_a_hotbar_stack", GoblinGameTests::snatchesAHotbarStack),
            new GameTestEntry("present_goblin_leaves_creative_players_alone", GoblinGameTests::leavesCreativeAlone),
            new GameTestEntry("killing_a_thief_returns_the_loot", GoblinGameTests::killingAThiefReturnsTheLoot),
            new GameTestEntry("gingerbread_men_go_soggy", GoblinGameTests::gingerbreadMenGoSoggy),
            new GameTestEntry("goblin_camp_data_loads", GoblinGameTests::goblinCampDataLoads),
            new GameTestEntry("goblins_and_reindeer_spawn_in_snowy_biomes", GoblinGameTests::spawnInSnowyBiomes));

    private static final Vec3 PLAYER = new Vec3(1.5, 1.0, 1.5);
    private static final Vec3 GOBLIN = new Vec3(2.5, 1.0, 1.5);
    private static final BlockPos POOL = new BlockPos(5, 1, 5);
    private static final int SWORD_SLOT = 3;
    private static final int STICK_SLOT = 7;
    private static final int STICKS = 12;
    private static final int SOAK_TICKS = 30;
    private static final int CAMP_PRESENTS = 8;

    private GoblinGameTests() {
    }

    /** Its attack takes a whole hotbar stack instead of hurting; then it holds it, won't despawn and stops attacking. */
    public static void snatchesAHotbarStack(GameTestHelper helper) {
        PresentGoblin goblin = helper.spawn(CozyEntities.PRESENT_GOBLIN.get(), GOBLIN);
        ServerPlayer player = TestPlayers.survival(helper, PLAYER);
        try {
            player.getInventory().setItem(SWORD_SLOT, new ItemStack(Items.IRON_SWORD));
            player.getInventory().setItem(STICK_SLOT, new ItemStack(Items.STICK, STICKS));
            float health = player.getHealth();
            helper.assertTrue(goblin.doHurtTarget(helper.getLevel(), player), "the snatch counts as a hit");
            helper.assertValueEqual(player.getHealth(), health, "a snatch doesn't hurt");
            ItemStack loot = goblin.getMainHandItem();
            boolean tookSword = loot.is(Items.IRON_SWORD) && player.getInventory().getItem(SWORD_SLOT).isEmpty();
            boolean tookSticks = loot.is(Items.STICK) && loot.getCount() == STICKS && player.getInventory().getItem(STICK_SLOT).isEmpty();
            helper.assertTrue(tookSword || tookSticks, "a whole hotbar stack moved into its hand, got " + loot);
            helper.assertTrue(goblin.hasLoot() && !goblin.removeWhenFarAway(1.0E6), "a goblin with loot never despawns");
            helper.assertFalse(goblin.canRob(player), "one stack per goblin");
            helper.succeed();
        } finally {
            TestPlayers.remove(helper, player);
        }
    }

    public static void leavesCreativeAlone(GameTestHelper helper) {
        PresentGoblin goblin = helper.spawn(CozyEntities.PRESENT_GOBLIN.get(), GOBLIN);
        ServerPlayer player = TestPlayers.survival(helper, PLAYER);
        try {
            helper.assertFalse(goblin.canRob(player), "an empty hotbar has nothing to take");
            player.getInventory().setItem(0, new ItemStack(Items.DIAMOND));
            helper.assertTrue(goblin.canRob(player), "a survival player with a diamond is fair game");
            player.setGameMode(GameType.CREATIVE);
            helper.assertFalse(goblin.canRob(player), "creative players are left alone");
            helper.succeed();
        } finally {
            TestPlayers.remove(helper, player);
        }
    }

    /** The killer gets the stack back, a surprise present on top and some karma. */
    public static void killingAThiefReturnsTheLoot(GameTestHelper helper) {
        PresentGoblin goblin = helper.spawn(CozyEntities.PRESENT_GOBLIN.get(), GOBLIN);
        ServerPlayer player = TestPlayers.survival(helper, PLAYER);
        try {
            player.getInventory().setItem(0, new ItemStack(Items.DIAMOND, 5));
            helper.assertTrue(goblin.steal(player), "the goblin steals the diamonds");
            int karma = KarmaManager.get(player);
            goblin.hurtServer(helper.getLevel(), helper.getLevel().damageSources().playerAttack(player), 1000.0F);
            helper.assertTrue(goblin.isDeadOrDying(), "the goblin dies");
            AABB around = goblin.getBoundingBox().inflate(3.0);
            List<ItemEntity> drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class, around);
            helper.assertTrue(drops.stream().anyMatch(drop -> drop.getItem().is(Items.DIAMOND) && drop.getItem().getCount() == 5),
                    "the diamonds drop");
            helper.assertTrue(drops.stream().anyMatch(drop -> drop.getItem().is(CozyBlocks.PRESENT_ITEM.get())),
                    "a present drops with them");
            helper.assertValueEqual(KarmaManager.get(player), karma + CozyConfig.common().karma().returnStolen(),
                    "karma for getting the loot back");
            helper.succeed();
        } finally {
            TestPlayers.remove(helper, player);
        }
    }

    public static void gingerbreadMenGoSoggy(GameTestHelper helper) {
        helper.setBlock(POOL, Blocks.WATER);
        GingerbreadMan man = helper.spawn(CozyEntities.GINGERBREAD_MAN.get(), Vec3.atBottomCenterOf(POOL));
        man.setNoAi(true);
        float health = man.getHealth();
        helper.runAfterDelay(SOAK_TICKS, () -> {
            helper.assertValueEqual(man.isSensitiveToWater(), CozyConfig.common().goblins().gingerbreadMan().soggy(),
                    "soggy follows the config");
            helper.assertTrue(!man.isSensitiveToWater() || man.getHealth() < health, "water hurts a gingerbread man");
            helper.succeed();
        });
    }

    /** Structure, set, pool and template are there; the camp has its fire, presents and chest; it spawns both mobs. */
    public static void goblinCampDataLoads(GameTestHelper helper) {
        var registries = helper.getLevel().registryAccess();
        Identifier id = CozyChristmas.id("goblin_camp");
        Structure camp = registries.lookupOrThrow(Registries.STRUCTURE).getValueOrThrow(ResourceKey.create(Registries.STRUCTURE, id));
        helper.assertTrue(registries.lookupOrThrow(Registries.STRUCTURE_SET)
                .get(ResourceKey.create(Registries.STRUCTURE_SET, id)).isPresent(), "structure set");
        helper.assertTrue(registries.lookupOrThrow(Registries.TEMPLATE_POOL)
                .get(ResourceKey.create(Registries.TEMPLATE_POOL, CozyChristmas.id("goblin_camp/camp"))).isPresent(), "template pool");
        StructureSpawnOverride monsters = camp.spawnOverrides().get(MobCategory.MONSTER);
        helper.assertTrue(monsters != null && spawns(monsters, CozyEntities.PRESENT_GOBLIN.get())
                && spawns(monsters, CozyEntities.GINGERBREAD_MAN.get()), "the camp spawns goblins and gingerbread men");
        StructureTemplate template = helper.getLevel().getServer().getStructureTemplateManager()
                .getOrCreate(CozyChristmas.id("goblin_camp/camp"));
        StructurePlaceSettings settings = new StructurePlaceSettings();
        helper.assertValueEqual(template.filterBlocks(BlockPos.ZERO, settings, Blocks.CAMPFIRE).size(), 1, "the camp fire");
        helper.assertValueEqual(template.filterBlocks(BlockPos.ZERO, settings, CozyBlocks.PRESENT.get()).size(), CAMP_PRESENTS,
                "the heap of stolen presents");
        helper.assertValueEqual(template.filterBlocks(BlockPos.ZERO, settings, Blocks.CHEST).size(), 1, "the loot chest");
        helper.succeed();
    }

    /**
     * 26.3 keeps natural spawns in the biome's environment attribute {@code gameplay/natural_mob_spawns}, no longer in
     * the biome itself; this checks that the loader's biome modification still lands there (goblins at night,
     * reindeer herds, both in the snowy taiga by default).
     */
    public static void spawnInSnowyBiomes(GameTestHelper helper) {
        Biome biome = helper.getLevel().registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.SNOWY_TAIGA).value();
        MobSpawnSettings spawns = biome.getAttributes().applyModifier(EnvironmentAttributes.NATURAL_MOB_SPAWNS,
                EnvironmentAttributes.NATURAL_MOB_SPAWNS.defaultValue());
        boolean goblins = CozyConfig.common().goblins().presentGoblin().spawnWeight() == 0
                || spawns(spawns.getMobsInCategory(MobCategory.MONSTER), CozyEntities.PRESENT_GOBLIN.get());
        boolean reindeer = CozyConfig.common().reindeer().spawnWeight() == 0
                || spawns(spawns.getMobsInCategory(MobCategory.CREATURE), CozyEntities.REINDEER.get());
        helper.assertTrue(goblins && reindeer, "goblins " + goblins + ", reindeer " + reindeer + "; monsters "
                + names(spawns.getMobsInCategory(MobCategory.MONSTER)) + ", creatures "
                + names(spawns.getMobsInCategory(MobCategory.CREATURE)));
        helper.succeed();
    }

    private static String names(WeightedList<MobSpawnSettings.SpawnerData> spawns) {
        return spawns.unwrap().stream().map(weighted -> EntityType.getKey(weighted.value().type()).toString()).toList().toString();
    }

    private static boolean spawns(WeightedList<MobSpawnSettings.SpawnerData> spawns, EntityType<?> type) {
        return spawns.unwrap().stream().anyMatch(weighted -> weighted.value().type() == type);
    }

    private static boolean spawns(StructureSpawnOverride override, EntityType<?> type) {
        return spawns(override.spawns(), type);
    }
}
