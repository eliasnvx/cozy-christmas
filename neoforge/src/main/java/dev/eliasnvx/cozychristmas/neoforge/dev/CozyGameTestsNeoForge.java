package dev.eliasnvx.cozychristmas.neoforge.dev;

import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.gametest.CosmeticSlotGameTests;
import dev.eliasnvx.cozychristmas.gametest.GameTestEntry;
import dev.eliasnvx.cozychristmas.gametest.KarmaGameTests;
import dev.eliasnvx.cozychristmas.gametest.PresentGameTests;
import dev.eliasnvx.cozychristmas.gametest.ToastySetGameTests;
import dev.eliasnvx.cozychristmas.gametest.WorkshopGameTests;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Registers the shared GameTests ({@link KarmaGameTests}, {@link CosmeticSlotGameTests}, {@link ToastySetGameTests},
 * {@link PresentGameTests}, {@link WorkshopGameTests}) on NeoForge. Started by {@code CozyChristmasNeoForge} only when
 * GameTests are enabled ({@code ./gradlew :neoforge:runGameTestServer}, part of {@code build}); not in the released jar.
 */
public final class CozyGameTestsNeoForge {

    /** 8x8x8 of air like Fabric's default, {@code data/cozychristmas/structure/empty.nbt} (dev only, like this class). */
    private static final Identifier EMPTY_STRUCTURE = CozyChristmas.id("empty");
    private static final int MAX_TICKS = 100;

    private CozyGameTestsNeoForge() {
    }

    public static void register(IEventBus modBus) {
        DeferredRegister<Consumer<GameTestHelper>> functions = DeferredRegister.create(Registries.TEST_FUNCTION, CozyChristmas.MOD_ID);
        List<DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>>> holders = new ArrayList<>();
        for (List<GameTestEntry> tests : List.of(KarmaGameTests.ALL, CosmeticSlotGameTests.ALL, ToastySetGameTests.ALL,
                PresentGameTests.ALL, WorkshopGameTests.ALL)) {
            for (GameTestEntry entry : tests) {
                holders.add(functions.register(entry.name(), entry::body));
            }
        }
        functions.register(modBus);

        modBus.addListener((RegisterGameTestsEvent event) -> {
            Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(CozyChristmas.id("default"));
            for (DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> holder : holders) {
                event.registerTest(holder.getId(), new FunctionGameTestInstance(holder.unwrapKey().orElseThrow(),
                        new TestData<>(environment, EMPTY_STRUCTURE, MAX_TICKS, 0, true)));
            }
        });
    }
}
