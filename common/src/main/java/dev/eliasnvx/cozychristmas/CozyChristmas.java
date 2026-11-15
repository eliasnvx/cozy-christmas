package dev.eliasnvx.cozychristmas;

import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.eliasnvx.cozychristmas.command.CozyCommands;
import dev.eliasnvx.cozychristmas.config.CozyConfig;
import dev.eliasnvx.cozychristmas.cosmetic.CosmeticsEvents;
import dev.eliasnvx.cozychristmas.cosmetic.ToastySet;
import dev.eliasnvx.cozychristmas.karma.KarmaRules;
import dev.eliasnvx.cozychristmas.present.SnowPresents;
import dev.eliasnvx.cozychristmas.registry.CozyBlocks;
import dev.eliasnvx.cozychristmas.registry.CozyComponents;
import dev.eliasnvx.cozychristmas.registry.CozyEntities;
import dev.eliasnvx.cozychristmas.registry.CozyItems;
import dev.eliasnvx.cozychristmas.registry.CozyMenus;
import dev.eliasnvx.cozychristmas.registry.CozyParticles;
import dev.eliasnvx.cozychristmas.registry.CozySounds;
import dev.eliasnvx.cozychristmas.stocking.StockingEvents;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Common entry point, called by each loader's main initializer. */
public final class CozyChristmas {

    public static final String MOD_ID = "cozychristmas";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static boolean initialized;

    private CozyChristmas() {
    }

    public static synchronized void init() {
        if (initialized) {
            throw new IllegalStateException("CozyChristmas.init() called twice");
        }
        initialized = true;

        CozyConfig.loadCommon();

        // components first: items read them in their properties; blocks before items: block items look them up
        CozyComponents.REGISTER.register();
        CozyBlocks.REGISTER.register();
        CozyEntities.REGISTER.register(); // before items: spawn eggs refer to the entity types
        CozyItems.REGISTER.register();
        CozyBlocks.BLOCK_ENTITIES.register();
        CozyMenus.REGISTER.register();
        CozySounds.REGISTER.register();
        CozyParticles.REGISTER.register();
        CozyItems.TABS.register();
        CozyEntities.init();

        CosmeticsEvents.register();
        StockingEvents.register();
        ToastySet.register();
        SnowPresents.register();
        KarmaRules.register();
        CommandRegistrationEvent.EVENT.register(CozyCommands::register);

        LOGGER.info("{} initialized", MOD_ID);
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
