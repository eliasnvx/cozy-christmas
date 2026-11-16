package dev.eliasnvx.cozychristmas.gametest;

import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.config.CozyConfig;
import dev.eliasnvx.cozychristmas.cosmetic.CosmeticSlot;
import dev.eliasnvx.cozychristmas.cosmetic.CosmeticsManager;
import dev.eliasnvx.cozychristmas.goblin.PresentGoblin;
import dev.eliasnvx.cozychristmas.karma.KarmaManager;
import dev.eliasnvx.cozychristmas.krampus.Krampus;
import dev.eliasnvx.cozychristmas.krampus.KrampusSack;
import dev.eliasnvx.cozychristmas.krampus.KrampusSettings;
import dev.eliasnvx.cozychristmas.krampus.KrampusVisits;
import dev.eliasnvx.cozychristmas.registry.CozyBlocks;
import dev.eliasnvx.cozychristmas.registry.CozyEffects;
import dev.eliasnvx.cozychristmas.registry.CozyEntities;
import dev.eliasnvx.cozychristmas.registry.CozyItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Krampus, his sack and his lair on a real server, shared by both loaders (Fabric: {@code fabric/src/gametest},
 * NeoForge: {@code neoforge/.../dev/CozyGameTestsNeoForge}). Dev only: not in the released jars.
 */
public final class KrampusGameTests {

    public static final List<GameTestEntry> ALL = List.of(
            new GameTestEntry("krampus_lash_hurts_and_pulls", KrampusGameTests::lashHurtsAndPulls),
            new GameTestEntry("krampus_sack_holds_a_player_still", KrampusGameTests::sackHoldsAPlayerStill),
            new GameTestEntry("no_riding_off_in_the_sack", KrampusGameTests::noRidingOffInTheSack),
            new GameTestEntry("krampus_calls_goblins", KrampusGameTests::callsGoblins),
            new GameTestEntry("beating_krampus_clears_bad_karma", KrampusGameTests::beatingHimClearsBadKarma),
            new GameTestEntry("krampus_visits_a_naughty_player", KrampusGameTests::visitsANaughtyPlayer),
            new GameTestEntry("krampus_lair_data_loads", KrampusGameTests::lairDataLoads),
            new GameTestEntry("krampus_drops_horns_and_chain", KrampusGameTests::hornsAndChain));

    private static final Vec3 KRAMPUS = new Vec3(3.5, 1.0, 1.5);
    private static final Vec3 NEAR = new Vec3(3.5, 1.0, 3.5);
    private static final Vec3 FAR = new Vec3(3.5, 1.0, 6.5);
    private static final Vec3 SECOND = new Vec3(5.5, 1.0, 3.5);
    private static final int NAUGHTY = -80;
    private static final int NICE = 50;
    private static final int SACK_GRACE = 15;
    private static final double SEARCH = 6.0;
    private static final int MIN_BARS = 20;
    private static final int MIN_SACKS = 6;
    private static final int MIN_LADDER = 10;
    /** A player's reach with a sword (the attribute's default). */
    private static final double SWORD_REACH = 3.0;

    private KrampusGameTests() {
    }

    public static void lashHurtsAndPulls(GameTestHelper helper) {
        Krampus krampus = krampus(helper);
        Pig pig = helper.spawn(EntityType.PIG, FAR);
        pig.setNoAi(true);
        float health = pig.getHealth();
        krampus.lash(helper.getLevel(), pig);
        helper.assertTrue(pig.getHealth() < health || pig.isDeadOrDying(), "the chain hurts");
        helper.assertTrue(pig.getDeltaMovement().z < 0.0, "and pulls the pig toward Krampus, got " + pig.getDeltaMovement());
        helper.succeed();
    }

    /**
     * Survival players go in the sack and can't move; creative players never do. Mock players don't tick, so a pig
     * shows the sack bursting when the effect runs out.
     */
    public static void sackHoldsAPlayerStill(GameTestHelper helper) {
        Krampus krampus = krampus(helper);
        ServerPlayer player = TestPlayers.survival(helper, NEAR);
        int ticks = CozyConfig.common().krampus().sackTicks();
        player.setGameMode(GameType.CREATIVE);
        helper.assertFalse(krampus.canSack(player), "creative players stay out of the sack");
        player.setGameMode(GameType.SURVIVAL);
        helper.assertValueEqual(krampus.canSack(player), ticks > 0, "a survival player goes in");
        if (ticks == 0) {
            TestPlayers.remove(helper, player);
            helper.succeed();
            return;
        }
        krampus.sack(helper.getLevel(), player);
        helper.assertTrue(player.hasEffect(CozyEffects.holder(CozyEffects.SACKED)), "sacked");
        helper.assertValueEqual(player.getAttributeValue(Attributes.MOVEMENT_SPEED), 0.0, "can't walk");
        helper.assertValueEqual(player.getAttributeValue(Attributes.JUMP_STRENGTH), 0.0, "can't jump");
        helper.assertFalse(krampus.canSack(player), "one sack at a time");
        List<KrampusSack> sacks = sacks(helper);
        helper.assertTrue(sacks.size() == 1 && sacks.getFirst().victim() == player, "a sack over the player");
        TestPlayers.remove(helper, player);
        Pig pig = helper.spawn(EntityType.PIG, SECOND);
        krampus.sack(helper.getLevel(), pig);
        helper.assertValueEqual(pig.getAttributeValue(Attributes.MOVEMENT_SPEED), 0.0, "the pig can't walk");
        helper.runAfterDelay(ticks + SACK_GRACE, () -> {
            helper.assertFalse(pig.hasEffect(CozyEffects.holder(CozyEffects.SACKED)), "out after " + ticks + " ticks");
            helper.assertTrue(pig.getAttributeValue(Attributes.MOVEMENT_SPEED) > 0.0, "walking again");
            helper.assertTrue(sacks(helper).isEmpty(), "the sacks burst");
            helper.succeed();
        });
    }

    /** Sacked, you can't get on a horse or into a boat and ride away. */
    public static void noRidingOffInTheSack(GameTestHelper helper) {
        Pig pig = helper.spawn(EntityType.PIG, new Vec3(1.5, 1.0, 1.5));
        Boat boat = helper.spawn(EntityType.BOAT, new Vec3(1.5, 1.0, 1.5));
        pig.addEffect(new MobEffectInstance(CozyEffects.holder(CozyEffects.SACKED), 40, 0, false, false, true));
        pig.startRiding(boat, true);
        pig.tick();
        helper.assertFalse(pig.isPassenger(), "a sacked pig doesn't ride off in a boat");
        helper.succeed();
    }

    public static void callsGoblins(GameTestHelper helper) {
        Krampus krampus = krampus(helper);
        krampus.summonGoblins(helper.getLevel());
        AABB around = krampus.getBoundingBox().inflate(SEARCH);
        int goblins = helper.getLevel().getEntitiesOfClass(PresentGoblin.class, around).size();
        helper.assertValueEqual(goblins, CozyConfig.common().krampus().summonGoblins(), "goblins at his side");
        helper.assertValueEqual(KrampusSettings.summonsDue(0.5F), 1, "one call at half health");
        helper.succeed();
    }

    /** The naughty fighter's karma goes to 0, the nice one's stays; he drops his horns and chain. */
    public static void beatingHimClearsBadKarma(GameTestHelper helper) {
        Krampus krampus = krampus(helper);
        ServerPlayer naughty = TestPlayers.survival(helper, NEAR);
        ServerPlayer nice = TestPlayers.survival(helper, SECOND);
        try {
            KarmaManager.set(naughty, NAUGHTY);
            KarmaManager.set(nice, NICE);
            ServerLevel level = helper.getLevel();
            krampus.hurt(level.damageSources().playerAttack(nice), 1.0F);
            krampus.invulnerableTime = 0;
            krampus.hurt(level.damageSources().playerAttack(naughty), 10000.0F);
            helper.assertTrue(krampus.isDeadOrDying(), "Krampus falls");
            boolean clears = CozyConfig.common().krampus().clearsKarma();
            helper.assertValueEqual(KarmaManager.get(naughty), clears ? 0 : NAUGHTY, "the naughty fighter's karma");
            helper.assertValueEqual(KarmaManager.get(nice), NICE, "good karma stays");
            List<ItemEntity> drops = level.getEntitiesOfClass(ItemEntity.class, krampus.getBoundingBox().inflate(SEARCH));
            helper.assertTrue(drops.stream().anyMatch(drop -> drop.getItem().is(CozyItems.KRAMPUS_HORNS.get())), "his horns drop");
            helper.assertTrue(drops.stream().anyMatch(drop -> drop.getItem().is(CozyItems.KRAMPUS_CHAIN.get())), "his chain drops");
            helper.succeed();
        } finally {
            TestPlayers.remove(helper, naughty);
            TestPlayers.remove(helper, nice);
        }
    }

    /** He turns up near the player for them and marks the day; then he's sent away. */
    public static void visitsANaughtyPlayer(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.survival(helper, NEAR);
        try {
            KarmaManager.set(player, NAUGHTY);
            Krampus krampus = KrampusVisits.visit(helper.getLevel(), player);
            helper.assertTrue(krampus != null, "Krampus found room to stand");
            double distance = krampus.distanceTo(player);
            var visits = CozyConfig.common().krampus().visits();
            helper.assertTrue(distance >= visits.minDistance() - 1 && distance <= visits.maxDistance() + 2,
                    "he comes from " + visits.minDistance() + "-" + visits.maxDistance() + " blocks away, got " + distance);
            helper.assertTrue(krampus.isVisiting(), "he comes for the player");
            helper.assertValueEqual(KarmaManager.lastKrampusVisit(player), KarmaManager.today(helper.getLevel()), "the visit is marked");
            krampus.vanish(helper.getLevel());
            helper.succeed();
        } finally {
            TestPlayers.remove(helper, player);
        }
    }

    /** Structure, set, pool, loot and the template's furniture. */
    public static void lairDataLoads(GameTestHelper helper) {
        var registries = helper.getLevel().registryAccess();
        ResourceLocation id = CozyChristmas.id("krampus_lair");
        registries.lookupOrThrow(Registries.STRUCTURE).getOrThrow(ResourceKey.create(Registries.STRUCTURE, id));
        helper.assertTrue(registries.lookupOrThrow(Registries.STRUCTURE_SET)
                .get(ResourceKey.create(Registries.STRUCTURE_SET, id)).isPresent(), "structure set");
        helper.assertTrue(registries.lookupOrThrow(Registries.TEMPLATE_POOL)
                .get(ResourceKey.create(Registries.TEMPLATE_POOL, CozyChristmas.id("krampus_lair/lair"))).isPresent(), "pool");
        for (String table : List.of("entities/krampus", "chests/krampus_lair", "blocks/sack")) {
            LootTable loot = helper.getLevel().getServer().reloadableRegistries()
                    .getLootTable(ResourceKey.create(Registries.LOOT_TABLE, CozyChristmas.id(table)));
            helper.assertTrue(loot != LootTable.EMPTY, "loot table " + table);
        }
        StructureTemplate template = helper.getLevel().getServer().getStructureManager()
                .getOrCreate(CozyChristmas.id("krampus_lair/lair"));
        StructurePlaceSettings settings = new StructurePlaceSettings();
        helper.assertValueEqual(template.filterBlocks(BlockPos.ZERO, settings, Blocks.CHEST).size(), 1, "the loot chest");
        helper.assertTrue(template.filterBlocks(BlockPos.ZERO, settings, Blocks.IRON_BARS).size() >= MIN_BARS, "cages");
        helper.assertTrue(template.filterBlocks(BlockPos.ZERO, settings, CozyBlocks.SACK.get()).size() >= MIN_SACKS, "sacks");
        helper.assertTrue(template.filterBlocks(BlockPos.ZERO, settings, Blocks.LADDER).size() >= MIN_LADDER, "a way down");
        helper.assertTrue(!template.filterBlocks(BlockPos.ZERO, settings, Blocks.CHAIN).isEmpty(), "chains");
        helper.succeed();
    }

    /**
     * The horns are a head piece; the chain reaches 5 blocks (1.21.1 has no attack range component: an entity
     * interaction range modifier in the main hand).
     */
    public static void hornsAndChain(GameTestHelper helper) {
        helper.assertValueEqual(CosmeticsManager.slotOf(new ItemStack(CozyItems.KRAMPUS_HORNS.get())), CosmeticSlot.HEAD, "horns slot");
        ItemAttributeModifiers modifiers = new ItemStack(CozyItems.KRAMPUS_CHAIN.get()).getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS,
                ItemAttributeModifiers.EMPTY);
        double reach = Attributes.ENTITY_INTERACTION_RANGE.value().getDefaultValue() + modifiers.modifiers().stream()
                .filter(entry -> entry.attribute().is(Attributes.ENTITY_INTERACTION_RANGE) && entry.slot() == EquipmentSlotGroup.MAINHAND
                        && entry.modifier().operation() == AttributeModifier.Operation.ADD_VALUE)
                .mapToDouble(entry -> entry.modifier().amount())
                .sum();
        helper.assertTrue(reach > SWORD_REACH, "the chain reaches further than a sword, got " + reach);
        helper.succeed();
    }

    private static Krampus krampus(GameTestHelper helper) {
        Krampus krampus = helper.spawn(CozyEntities.KRAMPUS.get(), KRAMPUS);
        krampus.setNoAi(true);
        return krampus;
    }

    private static List<KrampusSack> sacks(GameTestHelper helper) {
        return helper.getLevel().getEntitiesOfClass(KrampusSack.class, new AABB(helper.absolutePos(BlockPos.ZERO)).inflate(SEARCH * 2),
                sack -> !sack.isRemoved());
    }
}
