package dev.eliasnvx.cozychristmas.gametest;

import dev.architectury.platform.Platform;
import dev.eliasnvx.cozychristmas.compat.ExternalSlots;
import dev.eliasnvx.cozychristmas.compat.sereneseasons.SereneSeasonsCalendar;
import dev.eliasnvx.cozychristmas.cosmetic.CosmeticSlot;
import dev.eliasnvx.cozychristmas.cosmetic.CosmeticsManager;
import dev.eliasnvx.cozychristmas.cosmetic.ToastySet;
import dev.eliasnvx.cozychristmas.registry.CozyItems;
import dev.eliasnvx.cozychristmas.season.ChristmasSeason;
import dev.eliasnvx.cozychristmas.season.SeasonMoment;
import dev.eliasnvx.cozychristmas.season.SubSeason;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

/**
 * Compat on a real server, shared by both loaders. The slot-mod path runs with a stand-in source (Curios itself is
 * checked by the NeoForge-only {@code CuriosGameTest} in a {@code -Pcompat=curios} run); the Serene Seasons check
 * runs its real calendar when that mod is installed ({@code -Pcompat=seasons}) and checks the fallback otherwise.
 * Dev only: not in the released jars.
 */
public final class CompatGameTests {

    public static final List<GameTestEntry> ALL = List.of(
            new GameTestEntry("cosmetics_in_other_mods_slots_count_as_worn", CompatGameTests::otherModsSlotsCount),
            new GameTestEntry("season_reads_serene_seasons_calendar", CompatGameTests::sereneSeasonsCalendar));

    private static final Vec3 PLAYER = new Vec3(2.5, 1.0, 2.5);

    private CompatGameTests() {
    }

    /** A Toasty set split between our slots and a slot mod's: it counts, ours wins where both hold something. */
    public static void otherModsSlotsCount(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.survival(helper, PLAYER);
        ExternalSlots.Source source = (wearer, action) -> {
            if (wearer == player) {
                action.accept(new ItemStack(CozyItems.SCARF.get()));
                action.accept(new ItemStack(CozyItems.MITTENS.get()));
                action.accept(new ItemStack(CozyItems.EAR_WARMERS.get()));
                action.accept(new ItemStack(CozyItems.sweaters().getFirst().get()));
                action.accept(ItemStack.EMPTY);
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

    public static void sereneSeasonsCalendar(GameTestHelper helper) {
        if (!Platform.isModLoaded(SereneSeasonsCalendar.MOD_ID)) {
            helper.assertValueEqual(ChristmasSeason.calendarMoment(), Optional.empty(), "no seasons mod, no calendar");
            helper.succeed();
            return;
        }
        var server = helper.getLevel().getServer();
        server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "season set mid_winter");
        Optional<SeasonMoment> moment = ChristmasSeason.calendarMoment();
        helper.assertTrue(moment.isPresent() && moment.get().subSeason() == SubSeason.MID_WINTER && moment.get().day() == 0,
                "the calendar reads Serene Seasons' Overworld: " + moment);
        server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "season set early_summer");
        helper.assertValueEqual(ChristmasSeason.calendarMoment().map(SeasonMoment::subSeason), Optional.of(SubSeason.EARLY_SUMMER),
                "and follows it");
        helper.succeed();
    }
}
