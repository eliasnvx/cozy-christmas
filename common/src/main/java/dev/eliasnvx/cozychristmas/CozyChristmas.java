package dev.eliasnvx.cozychristmas;

import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.eliasnvx.cozychristmas.command.CozyCommands;
import dev.eliasnvx.cozychristmas.config.CozyConfig;
import dev.eliasnvx.cozychristmas.cosmetic.CosmeticsEvents;
import dev.eliasnvx.cozychristmas.karma.KarmaRules;
import dev.eliasnvx.cozychristmas.registry.CozyComponents;
import dev.eliasnvx.cozychristmas.registry.CozyItems;
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

        // components first: items read them in their properties
        CozyComponents.REGISTER.register();
        CozyItems.REGISTER.register();
        CozyItems.TABS.register();

        CosmeticsEvents.register();
        KarmaRules.register();
        CommandRegistrationEvent.EVENT.register(CozyCommands::register);

        LOGGER.info("{} initialized", MOD_ID);
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
