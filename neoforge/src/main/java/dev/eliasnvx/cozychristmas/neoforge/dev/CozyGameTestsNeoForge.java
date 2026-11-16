package dev.eliasnvx.cozychristmas.neoforge.dev;

import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.gametest.CompatGameTests;
import dev.eliasnvx.cozychristmas.gametest.CosmeticSlotGameTests;
import dev.eliasnvx.cozychristmas.gametest.FoodGameTests;
import dev.eliasnvx.cozychristmas.gametest.GameTestEntry;
import dev.eliasnvx.cozychristmas.gametest.GoblinGameTests;
import dev.eliasnvx.cozychristmas.gametest.HeadPieceGameTests;
import dev.eliasnvx.cozychristmas.gametest.KarmaGameTests;
import dev.eliasnvx.cozychristmas.gametest.KrampusGameTests;
import dev.eliasnvx.cozychristmas.gametest.PresentGameTests;
import dev.eliasnvx.cozychristmas.gametest.SleighGameTests;
import dev.eliasnvx.cozychristmas.gametest.SnowmanGameTests;
import dev.eliasnvx.cozychristmas.gametest.StockingGameTests;
import dev.eliasnvx.cozychristmas.gametest.ToastySetGameTests;
import dev.eliasnvx.cozychristmas.gametest.TreeGameTests;
import dev.eliasnvx.cozychristmas.gametest.WorkshopGameTests;
import dev.eliasnvx.cozychristmas.neoforge.compat.CuriosSlots;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.TestFunction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Registers the shared GameTests ({@link KarmaGameTests}, {@link CosmeticSlotGameTests}, {@link ToastySetGameTests},
 * {@link HeadPieceGameTests}, {@link PresentGameTests}, {@link WorkshopGameTests}, {@link GoblinGameTests},
 * {@link StockingGameTests}, {@link SnowmanGameTests}, {@link KrampusGameTests}, {@link FoodGameTests},
 * {@link TreeGameTests}, {@link CompatGameTests}, {@link SleighGameTests}) on NeoForge; with Curios installed only {@link CuriosGameTest}.
 * Started by {@code CozyChristmasNeoForge} only when GameTests are enabled ({@code ./gradlew :neoforge:runGameTestServer},
 * part of {@code build}); not in the released jar. Public because the game test registry invokes {@link #tests()}
 * reflectively.
 */
public final class CozyGameTestsNeoForge {

    /** 8x8x8 of air like Fabric's default, {@code data/cozychristmas/structure/empty.nbt} (dev only, like this class). */
    private static final String EMPTY_STRUCTURE = CozyChristmas.id("empty").toString();
    private static final int MAX_TICKS = 100;
    private static final long SETUP_TICKS = 0L;

    private CozyGameTestsNeoForge() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener((RegisterGameTestsEvent event) -> event.register(CozyGameTestsNeoForge.class));
    }

    /** One test function per shared test, all in the {@code cozychristmas} batch. */
    @GameTestGenerator
    public static List<TestFunction> tests() {
        List<GameTestEntry> entries = new ArrayList<>();
        if (ModList.get().isLoaded(CuriosSlots.MOD_ID)) {
            // -Pcompat=curios: only the Curios check; Curios can't sync its slots to the shared tests' mock players
            entries.add(new GameTestEntry("curios_slots_count_as_worn", CuriosGameTest::cosmeticsInCuriosSlotsCount));
        } else {
            for (List<GameTestEntry> tests : List.of(KarmaGameTests.ALL, CosmeticSlotGameTests.ALL, ToastySetGameTests.ALL,
                    HeadPieceGameTests.ALL, PresentGameTests.ALL, WorkshopGameTests.ALL, GoblinGameTests.ALL,
                    StockingGameTests.ALL, SnowmanGameTests.ALL, KrampusGameTests.ALL, FoodGameTests.ALL, TreeGameTests.ALL,
                    CompatGameTests.ALL, SleighGameTests.ALL)) {
                entries.addAll(tests);
            }
        }
        return entries.stream()
                .map(entry -> new TestFunction(CozyChristmas.MOD_ID, CozyChristmas.MOD_ID + "." + entry.name(), EMPTY_STRUCTURE,
                        MAX_TICKS, SETUP_TICKS, true, entry.body()))
                .toList();
    }
}
