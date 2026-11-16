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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Predicate;

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
            new GameTestEntry("goblins_never_take_a_totem", GoblinGameTests::neverTakeATotem),
            new GameTestEntry("stolen_loot_comes_back_whatever_the_rules", GoblinGameTests::lootComesBackWhateverTheRules),
            new GameTestEntry("returning_stolen_loot_rewards_once_a_day", GoblinGameTests::returningLootRewardsOnceADay),
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
            helper.assertTrue(goblin.doHurtTarget(player), "the snatch counts as a hit");
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
            goblin.hurt(helper.getLevel().damageSources().playerAttack(player), 1000.0F);
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

    /** A stolen totem would save the goblin from the killing blow and be used up: goblins leave totems alone. */
    public static void neverTakeATotem(GameTestHelper helper) {
        PresentGoblin goblin = helper.spawn(CozyEntities.PRESENT_GOBLIN.get(), GOBLIN);
        ServerPlayer player = TestPlayers.survival(helper, PLAYER);
        try {
            player.getInventory().setItem(0, new ItemStack(Items.TOTEM_OF_UNDYING));
            helper.assertFalse(goblin.canRob(player), "a totem alone is nothing to take");
            player.getInventory().setItem(1, new ItemStack(Items.DIRT, 3));
            helper.assertTrue(goblin.steal(player), "the dirt goes");
            helper.assertTrue(goblin.getMainHandItem().is(Items.DIRT), "not the totem, got " + goblin.getMainHandItem());
            helper.assertTrue(player.getInventory().getItem(0).is(Items.TOTEM_OF_UNDYING), "the totem stays");
            helper.succeed();
        } finally {
            TestPlayers.remove(helper, player);
        }
    }

    /** The stack is the player's: it drops with mob loot off, and even with Curse of Vanishing on it. */
    public static void lootComesBackWhateverTheRules(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.survival(helper, PLAYER);
        var rules = helper.getLevel().getGameRules();
        boolean mobDrops = rules.getBoolean(GameRules.RULE_DOMOBLOOT);
        try {
            rules.getRule(GameRules.RULE_DOMOBLOOT).set(false, helper.getLevel().getServer());
            PresentGoblin first = helper.spawn(CozyEntities.PRESENT_GOBLIN.get(), GOBLIN);
            player.getInventory().setItem(0, new ItemStack(Items.DIAMOND, 5));
            helper.assertTrue(first.steal(player), "the goblin steals the diamonds");
            first.hurt(helper.getLevel().damageSources().playerAttack(player), 1000.0F);
            helper.assertTrue(dropped(helper, first, stack -> stack.is(Items.DIAMOND) && stack.getCount() == 5),
                    "the diamonds come back with mob loot off");
            rules.getRule(GameRules.RULE_DOMOBLOOT).set(true, helper.getLevel().getServer());

            PresentGoblin second = helper.spawn(CozyEntities.PRESENT_GOBLIN.get(), GOBLIN.add(0.0, 0.0, 3.0));
            ItemStack sword = new ItemStack(Items.IRON_SWORD);
            sword.enchant(helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                    .getOrThrow(Enchantments.VANISHING_CURSE), 1);
            player.getInventory().setItem(0, sword);
            helper.assertTrue(second.steal(player), "the goblin steals the cursed sword");
            second.hurt(helper.getLevel().damageSources().playerAttack(player), 1000.0F);
            helper.assertTrue(dropped(helper, second, stack -> stack.is(Items.IRON_SWORD)), "the cursed sword comes back");
            helper.succeed();
        } finally {
            rules.getRule(GameRules.RULE_DOMOBLOOT).set(mobDrops, helper.getLevel().getServer());
            TestPlayers.remove(helper, player);
        }
    }

    /**
     * Feeding goblins junk and killing them mustn't mint karma and presents: the reward is once a game day. Mob loot is
     * off: a goblin's own loot table drops a present one kill in ten, which would look like a second reward.
     */
    public static void returningLootRewardsOnceADay(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.survival(helper, PLAYER);
        var rules = helper.getLevel().getGameRules();
        boolean mobDrops = rules.getBoolean(GameRules.RULE_DOMOBLOOT);
        rules.getRule(GameRules.RULE_DOMOBLOOT).set(false, helper.getLevel().getServer());
        try {
            int karma = KarmaManager.get(player);
            for (int round = 0; round < 2; round++) {
                PresentGoblin goblin = helper.spawn(CozyEntities.PRESENT_GOBLIN.get(), GOBLIN.add(0.0, 0.0, round * 3.0));
                player.getInventory().setItem(0, new ItemStack(Items.COBBLESTONE, 64));
                helper.assertTrue(goblin.steal(player), "the goblin steals the cobblestone");
                goblin.hurt(helper.getLevel().damageSources().playerAttack(player), 1000.0F);
                helper.assertTrue(dropped(helper, goblin, stack -> stack.is(Items.COBBLESTONE)), "the cobblestone comes back");
                helper.assertValueEqual(dropped(helper, goblin, stack -> stack.is(CozyBlocks.PRESENT_ITEM.get())), round == 0,
                        "a present only the first time today, round " + round);
            }
            helper.assertValueEqual(KarmaManager.get(player), karma + CozyConfig.common().karma().returnStolen(),
                    "karma for returned loot once a day");
            helper.succeed();
        } finally {
            rules.getRule(GameRules.RULE_DOMOBLOOT).set(mobDrops, helper.getLevel().getServer());
            TestPlayers.remove(helper, player);
        }
    }

    private static boolean dropped(GameTestHelper helper, PresentGoblin goblin, Predicate<ItemStack> what) {
        return helper.getLevel().getEntitiesOfClass(ItemEntity.class, goblin.getBoundingBox().inflate(1.5)).stream()
                .anyMatch(drop -> what.test(drop.getItem()));
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
        ResourceLocation id = CozyChristmas.id("goblin_camp");
        Structure camp = registries.lookupOrThrow(Registries.STRUCTURE).getOrThrow(ResourceKey.create(Registries.STRUCTURE, id)).value();
        helper.assertTrue(registries.lookupOrThrow(Registries.STRUCTURE_SET)
                .get(ResourceKey.create(Registries.STRUCTURE_SET, id)).isPresent(), "structure set");
        helper.assertTrue(registries.lookupOrThrow(Registries.TEMPLATE_POOL)
                .get(ResourceKey.create(Registries.TEMPLATE_POOL, CozyChristmas.id("goblin_camp/camp"))).isPresent(), "template pool");
        StructureSpawnOverride monsters = camp.spawnOverrides().get(MobCategory.MONSTER);
        helper.assertTrue(monsters != null && spawns(monsters, CozyEntities.PRESENT_GOBLIN.get())
                && spawns(monsters, CozyEntities.GINGERBREAD_MAN.get()), "the camp spawns goblins and gingerbread men");
        StructureTemplate template = helper.getLevel().getServer().getStructureManager()
                .getOrCreate(CozyChristmas.id("goblin_camp/camp"));
        StructurePlaceSettings settings = new StructurePlaceSettings();
        helper.assertValueEqual(template.filterBlocks(BlockPos.ZERO, settings, Blocks.CAMPFIRE).size(), 1, "the camp fire");
        helper.assertValueEqual(template.filterBlocks(BlockPos.ZERO, settings, CozyBlocks.PRESENT.get()).size(), CAMP_PRESENTS,
                "the heap of stolen presents");
        helper.assertValueEqual(template.filterBlocks(BlockPos.ZERO, settings, Blocks.CHEST).size(), 1, "the loot chest");
        helper.succeed();
    }

    /**
     * The loader's biome modification lands in the biome's spawn settings (goblins at night, reindeer herds, both in the
     * snowy taiga by default).
     */
    public static void spawnInSnowyBiomes(GameTestHelper helper) {
        Biome biome = helper.getLevel().registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.SNOWY_TAIGA).value();
        MobSpawnSettings spawns = biome.getMobSettings();
        boolean goblins = CozyConfig.common().goblins().presentGoblin().spawnWeight() == 0
                || spawns(spawns.getMobs(MobCategory.MONSTER), CozyEntities.PRESENT_GOBLIN.get());
        boolean reindeer = CozyConfig.common().reindeer().spawnWeight() == 0
                || spawns(spawns.getMobs(MobCategory.CREATURE), CozyEntities.REINDEER.get());
        helper.assertTrue(goblins && reindeer, "goblins " + goblins + ", reindeer " + reindeer + "; monsters "
                + names(spawns.getMobs(MobCategory.MONSTER)) + ", creatures "
                + names(spawns.getMobs(MobCategory.CREATURE)));
        helper.succeed();
    }

    private static String names(WeightedRandomList<MobSpawnSettings.SpawnerData> spawns) {
        return spawns.unwrap().stream().map(spawner -> EntityType.getKey(spawner.type).toString()).toList().toString();
    }

    private static boolean spawns(WeightedRandomList<MobSpawnSettings.SpawnerData> spawns, EntityType<?> type) {
        return spawns.unwrap().stream().anyMatch(spawner -> spawner.type == type);
    }

    private static boolean spawns(StructureSpawnOverride override, EntityType<?> type) {
        return spawns(override.spawns(), type);
    }
}
