package dev.eliasnvx.cozychristmas.forge.dev;

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
import dev.eliasnvx.cozychristmas.forge.compat.CuriosSlots;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraftforge.event.RegisterGameTestsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;

import java.util.ArrayList;
import java.util.List;

/**
 * Registers the shared GameTests ({@link KarmaGameTests}, {@link CosmeticSlotGameTests}, {@link ToastySetGameTests},
 * {@link HeadPieceGameTests}, {@link PresentGameTests}, {@link WorkshopGameTests}, {@link GoblinGameTests},
 * {@link StockingGameTests}, {@link SnowmanGameTests}, {@link KrampusGameTests}, {@link FoodGameTests},
 * {@link TreeGameTests}, {@link CompatGameTests}, {@link SleighGameTests}) on Forge; with Curios installed only {@link CuriosGameTest}.
 * Started by {@code CozyChristmasForge} only when GameTests are enabled ({@code ./gradlew :forge:runGameTestServer},
 * part of {@code build}); not in the released jar. Public because the game test registry invokes {@link #tests()}
 * reflectively. The shared tests' mock players join over an EmbeddedChannel connection that has no
 * {@code packet_handler} in its pipeline, so Forge 47 skips its network filters and sends every mod's packets to it:
 * no connection setup is needed (the 1.21.1 NeoForge glue had to negotiate the channels).
 */
public final class CozyGameTestsForge {

    /** 8x8x8 of air like Fabric's default, {@code data/cozychristmas/structures/empty.nbt} (1.20.1 data version; dev only, like this class). */
    private static final String EMPTY_STRUCTURE = CozyChristmas.id("empty").toString();
    private static final int MAX_TICKS = 100;
    private static final long SETUP_TICKS = 0L;

    private CozyGameTestsForge() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener((RegisterGameTestsEvent event) -> event.register(CozyGameTestsForge.class));
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
