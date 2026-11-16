package dev.eliasnvx.cozychristmas.gametest;

import com.mojang.authlib.GameProfile;
import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.config.CozyConfig;
import dev.eliasnvx.cozychristmas.karma.KarmaManager;
import dev.eliasnvx.cozychristmas.present.PresentBlockEntity;
import dev.eliasnvx.cozychristmas.present.PresentContents;
import dev.eliasnvx.cozychristmas.present.PresentOutcome;
import dev.eliasnvx.cozychristmas.registry.CozyBlocks;
import dev.eliasnvx.cozychristmas.registry.CozyComponents;
import dev.eliasnvx.cozychristmas.registry.CozyEntities;
import dev.eliasnvx.cozychristmas.reindeer.Reindeer;
import dev.eliasnvx.cozychristmas.season.ChristmasSeason;
import dev.eliasnvx.cozychristmas.workshop.Elf;
import dev.eliasnvx.cozychristmas.workshop.Santa;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

/**
 * Santa's workshop on a real server, shared by both loaders: the alarm when an elf is hurt, theft from the
 * warehouse, Santa turning the naughty away and giving the nice their daily present and discount, taming and feeding
 * reindeer, flight following the season, and the structure's data loading. Dev only: not in the released jars.
 */
public final class WorkshopGameTests {

    public static final List<GameTestEntry> ALL = List.of(
            new GameTestEntry("hurting_an_elf_brings_santa", WorkshopGameTests::hurtingAnElfBringsSanta),
            new GameTestEntry("stealing_from_the_warehouse_angers_santa", WorkshopGameTests::stealingAngersSanta),
            new GameTestEntry("santa_turns_away_the_naughty", WorkshopGameTests::santaTurnsAwayTheNaughty),
            new GameTestEntry("santa_gives_the_nice_a_present_and_a_discount", WorkshopGameTests::santaGivesTheNice),
            new GameTestEntry("reindeer_tame_with_carrots", WorkshopGameTests::reindeerTameWithCarrots),
            new GameTestEntry("reindeer_fly_in_season", WorkshopGameTests::reindeerFlyInSeason),
            new GameTestEntry("a_flying_reindeer_is_a_flying_vehicle", WorkshopGameTests::flyingReindeerIsAFlyingVehicle),
            new GameTestEntry("santa_stays_angry_across_a_relog", WorkshopGameTests::santaStaysAngryAcrossARelog),
            new GameTestEntry("warehouse_presents_wait_for_their_opener", WorkshopGameTests::stockWaitsForItsOpener),
            new GameTestEntry("blowing_up_the_warehouse_is_theft", WorkshopGameTests::blowingUpTheWarehouseIsTheft),
            new GameTestEntry("workshop_data_loads", WorkshopGameTests::workshopDataLoads));

    private static final Vec3 PLAYER = new Vec3(1.5, 1.0, 1.5);
    private static final Vec3 SANTA = new Vec3(6.5, 1.0, 6.5);
    private static final Vec3 HELPER = new Vec3(3.5, 1.0, 3.5);
    private static final BlockPos STOCK = new BlockPos(2, 1, 4);
    private static final LocalDate IN_SEASON = LocalDate.of(2026, 12, 10);
    private static final int NICE = 60;
    private static final int NAUGHTY = -60;

    private WorkshopGameTests() {
    }

    public static void hurtingAnElfBringsSanta(GameTestHelper helper) {
        Santa santa = helper.spawn(CozyEntities.SANTA.get(), SANTA);
        Elf elf = helper.spawn(CozyEntities.ELF.get(), HELPER);
        ServerPlayer player = TestPlayers.survival(helper, PLAYER);
        try {
            elf.hurt(helper.getLevel().damageSources().playerAttack(player), 1.0F);
            helper.assertTrue(santa.isAngryAt(player), "Santa goes after whoever hurt an elf");
            helper.assertTrue(elf.isFleeingFrom(player), "the elf runs from them");
            helper.assertValueEqual(KarmaManager.get(player), CozyConfig.common().karma().hurtProtected(),
                    "karma for hurting an elf");
            helper.succeed();
        } finally {
            TestPlayers.remove(helper, player);
        }
    }

    public static void stealingAngersSanta(GameTestHelper helper) {
        Santa santa = helper.spawn(CozyEntities.SANTA.get(), SANTA);
        ServerPlayer player = TestPlayers.survival(helper, PLAYER);
        try {
            // a player's own present is just a present
            presentAt(helper, STOCK, false).open(player);
            helper.assertFalse(santa.isAngryAt(player), "opening an ordinary present is fine");
            helper.assertValueEqual(KarmaManager.get(player), 0, "karma after an ordinary present");

            presentAt(helper, STOCK, true).open(player);
            helper.assertTrue(santa.isAngryAt(player), "Santa goes after a thief");
            helper.assertValueEqual(KarmaManager.get(player), CozyConfig.common().karma().stealPresent(), "karma for stealing");

            KarmaManager.set(player, 0);
            presentAt(helper, STOCK, true);
            player.gameMode.destroyBlock(helper.absolutePos(STOCK));
            helper.assertValueEqual(KarmaManager.get(player), CozyConfig.common().karma().stealPresent(),
                    "breaking a stock present is stealing too");
            helper.killAllEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class);
            helper.succeed();
        } finally {
            TestPlayers.remove(helper, player);
        }
    }

    public static void santaTurnsAwayTheNaughty(GameTestHelper helper) {
        Santa santa = helper.spawn(CozyEntities.SANTA.get(), SANTA);
        ServerPlayer player = TestPlayers.survival(helper, SANTA.add(1.0, 0.0, 0.0));
        try {
            KarmaManager.set(player, NAUGHTY);
            santa.mobInteract(player, InteractionHand.MAIN_HAND);
            helper.assertFalse(player.containerMenu instanceof MerchantMenu, "no trading with the naughty");
            helper.assertTrue(santa.getUnhappyCounter() > 0, "Santa shakes his head");
            helper.succeed();
        } finally {
            TestPlayers.remove(helper, player);
        }
    }

    public static void santaGivesTheNice(GameTestHelper helper) {
        Santa santa = helper.spawn(CozyEntities.SANTA.get(), SANTA);
        ServerPlayer player = TestPlayers.survival(helper, SANTA.add(1.0, 0.0, 0.0));
        ChristmasSeason.setClock(fixed(IN_SEASON));
        try {
            KarmaManager.set(player, NICE);
            santa.mobInteract(player, InteractionHand.MAIN_HAND);
            ItemStack present = findPresent(player);
            helper.assertTrue(!present.isEmpty(), "a nice player gets a present in season");
            helper.assertValueEqual(present.get(CozyComponents.PRESENT_CONTENTS.get()),
                    new PresentContents.Surprise(Optional.of(PresentOutcome.LOOT)), "Santa's present is a gift");
            helper.assertFalse(player.containerMenu instanceof MerchantMenu, "the present comes first");

            player.getInventory().clearContent();
            santa.mobInteract(player, InteractionHand.MAIN_HAND);
            helper.assertTrue(findPresent(player).isEmpty(), "one present a day");
            helper.assertTrue(player.containerMenu instanceof MerchantMenu, "then Santa trades");
            boolean discounted = false;
            for (MerchantOffer offer : santa.getOffers()) {
                int base = offer.getBaseCostA().getCount();
                helper.assertTrue(offer.getCostA().getCount() <= base, "never dearer for the nice");
                discounted |= CozyConfig.common().workshop().santa().discount(base) > 0
                        && offer.getCostA().getCount() < base;
            }
            helper.assertTrue(discounted, "the nice get a discount");

            player.closeContainer();
            for (MerchantOffer offer : santa.getOffers()) {
                helper.assertValueEqual(offer.getSpecialPriceDiff(), 0, "the discount lasts one visit");
            }
            helper.succeed();
        } finally {
            ChristmasSeason.setClock(Clock.systemDefaultZone());
            TestPlayers.remove(helper, player);
        }
    }

    public static void reindeerTameWithCarrots(GameTestHelper helper) {
        Reindeer reindeer = helper.spawn(CozyEntities.REINDEER.get(), HELPER);
        ServerPlayer player = TestPlayers.survival(helper, PLAYER);
        try {
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.CARROT, 64));
            int fed = 0;
            while (!reindeer.isTamed() && fed < 64) {
                reindeer.mobInteract(player, InteractionHand.MAIN_HAND);
                fed++;
            }
            helper.assertTrue(reindeer.isTamed(), "carrots tame a reindeer");
            helper.assertValueEqual(player.getMainHandItem().getCount(), 64 - fed, "one carrot per try");
            helper.assertValueEqual(KarmaManager.get(player), CozyConfig.common().karma().feedReindeer(),
                    "feeding karma once a day, however many carrots");
            helper.succeed();
        } finally {
            TestPlayers.remove(helper, player);
        }
    }

    /** Flight needs a saddle and follows the season through {@code reindeer.flight} (the dates are in JUnit). */
    public static void reindeerFlyInSeason(GameTestHelper helper) {
        Reindeer reindeer = helper.spawn(CozyEntities.REINDEER.get(), HELPER);
        reindeer.setTamed(true);
        reindeer.refreshFlight(true);
        helper.assertFalse(reindeer.canFly(), "no flying without a saddle");
        reindeer.equipSaddle(new ItemStack(Items.SADDLE), null);
        helper.assertValueEqual(reindeer.canFly(), CozyConfig.common().reindeer().flight().allows(true), "flight in season");
        reindeer.refreshFlight(false);
        helper.assertValueEqual(reindeer.canFly(), CozyConfig.common().reindeer().flight().allows(false),
                "flight out of season");
        helper.succeed();
    }

    /** A dedicated server kicks a rider whose mount floats unless it says it flies (allow-flight is off by default). */
    public static void flyingReindeerIsAFlyingVehicle(GameTestHelper helper) {
        Reindeer reindeer = helper.spawn(CozyEntities.REINDEER.get(), HELPER);
        reindeer.setTamed(true);
        reindeer.equipSaddle(new ItemStack(Items.SADDLE), null);
        reindeer.refreshFlight(true);
        helper.assertValueEqual(reindeer.isFlyingVehicle(), reindeer.canFly(), "a reindeer that may fly is a flying vehicle");
        reindeer.refreshFlight(false);
        helper.assertValueEqual(reindeer.isFlyingVehicle(), reindeer.canFly(), "and one that may not isn't");
        helper.succeed();
    }

    /** Santa's anger follows the player, not the player object: logging out and back in doesn't calm him. */
    public static void santaStaysAngryAcrossARelog(GameTestHelper helper) {
        Santa santa = helper.spawn(CozyEntities.SANTA.get(), SANTA);
        ServerPlayer player = TestPlayers.survival(helper, PLAYER);
        try {
            helper.assertTrue(santa.angerAt(player), "Santa is angry");
            ServerPlayer back = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(),
                    new GameProfile(player.getUUID(), player.getScoreboardName()), ClientInformation.createDefault());
            helper.assertTrue(santa.isAngryAt(back), "still angry at the same player back from a relog");
            helper.succeed();
        } finally {
            TestPlayers.remove(helper, player);
        }
    }

    /** A warehouse present isn't decided by whoever walks past: the thief's karma, after the theft, decides it. */
    public static void stockWaitsForItsOpener(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.survival(helper, PLAYER);
        BlockPos at = BlockPos.containing(PLAYER).east();
        helper.setBlock(at, CozyBlocks.PRESENT.get());
        PresentBlockEntity present = helper.<PresentBlockEntity>getBlockEntity(at);
        present.setWorkshopStock(true);
        KarmaManager.set(player, 60);
        helper.runAfterDelay(12, () -> { // past a check (every 10 ticks)
            try {
                helper.assertValueEqual(present.contents(), PresentContents.SURPRISE, "still undecided with a player beside it");
                helper.succeed();
            } finally {
                TestPlayers.remove(helper, player);
            }
        });
    }

    /** TNT a player lit in the warehouse is their theft, like breaking the presents by hand. */
    public static void blowingUpTheWarehouseIsTheft(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.survival(helper, PLAYER);
        try {
            BlockPos at = BlockPos.containing(PLAYER).east(3);
            helper.setBlock(at, CozyBlocks.PRESENT.get());
            helper.<PresentBlockEntity>getBlockEntity(at).setWorkshopStock(true);
            int karma = KarmaManager.get(player);
            Vec3 blast = Vec3.atCenterOf(helper.absolutePos(at));
            PrimedTnt tnt = new PrimedTnt(helper.getLevel(), blast.x, blast.y, blast.z, player);
            helper.getLevel().explode(tnt, blast.x, blast.y, blast.z, 1.5F, Level.ExplosionInteraction.TNT);
            helper.assertBlockPresent(Blocks.AIR, at);
            helper.assertValueEqual(KarmaManager.get(player), karma + CozyConfig.common().karma().stealPresent(),
                    "blowing up Santa's presents is stealing them");
            helper.succeed();
        } finally {
            TestPlayers.remove(helper, player);
        }
    }

    /** The structure, its set and pools are registered, and every piece's template loads. */
    public static void workshopDataLoads(GameTestHelper helper) {
        var registries = helper.getLevel().registryAccess();
        helper.assertTrue(registries.lookupOrThrow(Registries.STRUCTURE)
                .get(ResourceKey.create(Registries.STRUCTURE, CozyChristmas.id("santas_workshop"))).isPresent(), "structure");
        helper.assertTrue(registries.lookupOrThrow(Registries.STRUCTURE_SET)
                .get(ResourceKey.create(Registries.STRUCTURE_SET, CozyChristmas.id("santas_workshop"))).isPresent(), "structure set");
        for (String piece : List.of("plaza", "hall", "house", "warehouse", "stable")) {
            ResourceLocation id = CozyChristmas.id("workshop/" + piece);
            helper.assertTrue(registries.lookupOrThrow(Registries.TEMPLATE_POOL)
                    .get(ResourceKey.create(Registries.TEMPLATE_POOL, id)).isPresent(), "template pool " + id);
            Optional<StructureTemplate> template = helper.getLevel().getServer().getStructureManager().get(id);
            helper.assertTrue(template.isPresent() && template.get().getSize().getX() > 0, "template " + id);
        }
        StructureTemplate hall = helper.getLevel().getServer().getStructureManager()
                .getOrCreate(CozyChristmas.id("workshop/hall"));
        helper.assertValueEqual(hall.filterBlocks(BlockPos.ZERO, new StructurePlaceSettings(), Blocks.CRAFTING_TABLE).size(), 4,
                "workbenches in the hall");
        helper.succeed();
    }

    private static PresentBlockEntity presentAt(GameTestHelper helper, BlockPos pos, boolean stock) {
        helper.setBlock(pos, CozyBlocks.PRESENT.get());
        PresentBlockEntity present = helper.<PresentBlockEntity>getBlockEntity(pos);
        present.setContents(new PresentContents.Surprise(PresentOutcome.COAL)); // no boom near the test player
        present.setWorkshopStock(stock);
        return present;
    }

    private static ItemStack findPresent(ServerPlayer player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(CozyBlocks.PRESENT_ITEM.get())) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static Clock fixed(LocalDate date) {
        ZoneId zone = ZoneOffset.UTC;
        return Clock.fixed(date.atStartOfDay(zone).toInstant(), zone);
    }
}
