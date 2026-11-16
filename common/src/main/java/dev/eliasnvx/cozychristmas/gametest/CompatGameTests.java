package dev.eliasnvx.cozychristmas.gametest;

import dev.architectury.platform.Platform;
import dev.eliasnvx.cozychristmas.compat.ExternalSlots;
import dev.eliasnvx.cozychristmas.compat.sereneseasons.SereneSeasonsCalendar;
import dev.eliasnvx.cozychristmas.compat.snowyspirit.SnowySpiritGuards;
import dev.eliasnvx.cozychristmas.config.CozyConfig;
import dev.eliasnvx.cozychristmas.cosmetic.CosmeticInventory;
import dev.eliasnvx.cozychristmas.cosmetic.CosmeticSlot;
import dev.eliasnvx.cozychristmas.cosmetic.CosmeticsManager;
import dev.eliasnvx.cozychristmas.cosmetic.ToastySet;
import dev.eliasnvx.cozychristmas.registry.CozyEntities;
import dev.eliasnvx.cozychristmas.registry.CozyItems;
import dev.eliasnvx.cozychristmas.season.ChristmasSeason;
import dev.eliasnvx.cozychristmas.season.SeasonMoment;
import dev.eliasnvx.cozychristmas.season.SubSeason;
import dev.eliasnvx.cozychristmas.workshop.Santa;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

/**
 * Compat on a real server, shared by both loaders. The slot-mod path runs with a stand-in source (Curios itself is
 * checked by the Forge-only {@code CuriosGameTest} in a {@code -Pcompat=curios} run); the Serene Seasons check
 * runs its real calendar when that mod is installed ({@code -Pcompat=seasons}) and checks the fallback otherwise.
 * Dev only: not in the released jars.
 */
public final class CompatGameTests {

    public static final List<GameTestEntry> ALL = List.of(
            new GameTestEntry("cosmetics_in_other_mods_slots_count_as_worn", CompatGameTests::otherModsSlotsCount),
            new GameTestEntry("hidden_and_cosmetic_only_slots_split_looks_from_bonuses", CompatGameTests::hiddenAndLookOnlySlots),
            new GameTestEntry("season_reads_serene_seasons_calendar", CompatGameTests::sereneSeasonsCalendar),
            new GameTestEntry("reindeer_pull_snowy_spirit_sleds", CompatGameTests::reindeerPullSleds),
            new GameTestEntry("santa_calls_snowy_spirit_guards", CompatGameTests::santaCallsGuards));

    private static final Vec3 PLAYER = new Vec3(2.5, 1.0, 2.5);

    private CompatGameTests() {
    }

    /** A Toasty set split between our slots and a slot mod's: it counts, ours wins where both hold something. */
    public static void otherModsSlotsCount(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.survival(helper, PLAYER);
        ExternalSlots.Source source = (wearer, visitor) -> {
            if (wearer == player) {
                visitor.accept(new ItemStack(CozyItems.SCARF.get()), ExternalSlots.Role.WORN);
                visitor.accept(new ItemStack(CozyItems.MITTENS.get()), ExternalSlots.Role.WORN);
                visitor.accept(new ItemStack(CozyItems.EAR_WARMERS.get()), ExternalSlots.Role.WORN);
                visitor.accept(new ItemStack(CozyItems.sweaters().get(0).get()), ExternalSlots.Role.WORN);
                visitor.accept(ItemStack.EMPTY, ExternalSlots.Role.WORN);
            }
        };
        ExternalSlots.register(source);
        try {
            CosmeticsManager.set(player, CosmeticSlot.HEAD, new ItemStack(CozyItems.POMPOM_HAT.get()));
            helper.assertTrue(CosmeticsManager.worn(player).get(CosmeticSlot.NECK).is(CozyItems.SCARF.get()),
                    "a scarf in another mod's slot is worn");
            helper.assertTrue(CosmeticsManager.worn(player).get(CosmeticSlot.HEAD).is(CozyItems.POMPOM_HAT.get()),
                    "our own slot wins over the other mod's");
            helper.assertTrue(CosmeticsManager.get(player).get(CosmeticSlot.NECK).isEmpty(), "our slots stay as they are");
            helper.assertTrue(ToastySet.isWorn(player), "the set counts across both");
            helper.succeed();
        } finally {
            ExternalSlots.unregister(source);
            TestPlayers.remove(helper, player);
        }
    }

    /**
     * Curios' semantics: a piece with its slot's show toggle off still counts but isn't drawn; a piece in a cosmetic-only
     * slot is drawn over what's worn there but counts for nothing.
     */
    public static void hiddenAndLookOnlySlots(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.survival(helper, PLAYER);
        ItemStack sweater = new ItemStack(CozyItems.sweaters().get(0).get());
        ItemStack otherSweater = new ItemStack(CozyItems.sweaters().get(1).get());
        ExternalSlots.Source source = (wearer, visitor) -> {
            if (wearer == player) {
                visitor.accept(new ItemStack(CozyItems.SCARF.get()), ExternalSlots.Role.HIDDEN);
                visitor.accept(new ItemStack(CozyItems.MITTENS.get()), ExternalSlots.Role.LOOK);
                visitor.accept(sweater, ExternalSlots.Role.WORN);
                visitor.accept(otherSweater, ExternalSlots.Role.LOOK);
            }
        };
        ExternalSlots.register(source);
        try {
            CosmeticInventory worn = CosmeticsManager.worn(player);
            CosmeticInventory looks = CosmeticsManager.looks(player);
            helper.assertTrue(worn.get(CosmeticSlot.NECK).is(CozyItems.SCARF.get()), "a hidden scarf still counts");
            helper.assertTrue(looks.get(CosmeticSlot.NECK).isEmpty(), "but isn't drawn");
            helper.assertTrue(worn.get(CosmeticSlot.HANDS).isEmpty(), "cosmetic-only mittens don't count");
            helper.assertTrue(looks.get(CosmeticSlot.HANDS).is(CozyItems.MITTENS.get()), "but are drawn");
            helper.assertTrue(worn.get(CosmeticSlot.BODY).is(sweater.getItem()), "the worn sweater counts");
            helper.assertTrue(looks.get(CosmeticSlot.BODY).is(otherSweater.getItem()), "the cosmetic one is what's drawn");
            helper.succeed();
        } finally {
            ExternalSlots.unregister(source);
            TestPlayers.remove(helper, player);
        }
    }

    public static void sereneSeasonsCalendar(GameTestHelper helper) {
        if (!Platform.isModLoaded(SereneSeasonsCalendar.MOD_ID)) {
            GameTestAsserts.assertValueEqual(helper, ChristmasSeason.calendarMoment(), Optional.empty(), "no seasons mod, no calendar");
            helper.succeed();
            return;
        }
        var server = helper.getLevel().getServer();
        server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "season set mid_winter");
        Optional<SeasonMoment> moment = ChristmasSeason.calendarMoment();
        helper.assertTrue(moment.isPresent() && moment.get().subSeason() == SubSeason.MID_WINTER && moment.get().day() == 0,
                "the calendar reads Serene Seasons' Overworld: " + moment);
        server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "season set early_summer");
        GameTestAsserts.assertValueEqual(helper, ChristmasSeason.calendarMoment().map(SeasonMoment::subSeason), Optional.of(SubSeason.EARLY_SUMMER),
                "and follows it");
        helper.succeed();
    }

    /** Snowy Spirit's sleds take any animal in its puller tag: our reindeer is in it (data, there with or without the mod). */
    public static void reindeerPullSleds(GameTestHelper helper) {
        TagKey<EntityType<?>> pullers = TagKey.create(Registries.ENTITY_TYPE,
                new ResourceLocation(SnowySpiritGuards.MOD_ID, "sled_pullers"));
        helper.assertTrue(CozyEntities.REINDEER.get().is(pullers), "reindeer pull Snowy Spirit's sleds");
        helper.succeed();
    }

    /** With Snowy Spirit, Santa at home in his workshop calls his gingerbread golem guards once; without it, none. */
    public static void santaCallsGuards(GameTestHelper helper) {
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                helper.setBlock(x, 0, z, Blocks.STONE);
            }
        }
        Santa santa = helper.spawn(CozyEntities.SANTA.get(), new Vec3(3.5, 1.0, 3.5));
        santa.setNoAi(true);
        santa.restrictTo(santa.blockPosition(), 20); // a workshop's Santa has his home
        santa.guardWorkshop(helper.getLevel());
        santa.guardWorkshop(helper.getLevel()); // once only
        long guards = helper.getLevel().getEntities((Entity) null, santa.getBoundingBox().inflate(12.0),
                entity -> SnowySpiritGuards.GINGERBREAD_GOLEM.equals(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()))).size();
        int expected = SnowySpiritGuards.available() ? CozyConfig.common().workshop().santa().snowySpiritGuards() : 0;
        GameTestAsserts.assertValueEqual(helper, guards, (long) expected, "gingerbread golem guards");
        helper.getLevel().getEntities((Entity) null, santa.getBoundingBox().inflate(12.0), entity -> !(entity instanceof Player))
                .forEach(Entity::discard);
        helper.succeed();
    }
}
