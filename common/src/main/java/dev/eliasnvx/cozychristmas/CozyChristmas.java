package dev.eliasnvx.cozychristmas;

import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.platform.Platform;
import dev.eliasnvx.cozychristmas.command.CozyCommands;
import dev.eliasnvx.cozychristmas.compat.sereneseasons.SereneSeasonsCalendar;
import dev.eliasnvx.cozychristmas.config.CozyConfig;
import dev.eliasnvx.cozychristmas.cosmetic.CosmeticsEvents;
import dev.eliasnvx.cozychristmas.cosmetic.ToastySet;
import dev.eliasnvx.cozychristmas.food.Chestnuts;
import dev.eliasnvx.cozychristmas.karma.KarmaRules;
import dev.eliasnvx.cozychristmas.krampus.KrampusVisits;
import dev.eliasnvx.cozychristmas.present.SnowPresents;
import dev.eliasnvx.cozychristmas.registry.CozyBlocks;
import dev.eliasnvx.cozychristmas.registry.CozyEffects;
import dev.eliasnvx.cozychristmas.registry.CozyEntities;
import dev.eliasnvx.cozychristmas.registry.CozyItems;
import dev.eliasnvx.cozychristmas.registry.CozyMenus;
import dev.eliasnvx.cozychristmas.registry.CozyParticles;
import dev.eliasnvx.cozychristmas.registry.CozySounds;
import dev.eliasnvx.cozychristmas.season.SeasonSource;
import dev.eliasnvx.cozychristmas.wood.CozyWoods;
import dev.eliasnvx.cozychristmas.snowman.IceSnowball;
import dev.eliasnvx.cozychristmas.stocking.StockingEvents;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
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

        // blocks before items: block items look them up
        CozyWoods.init();
        CozyBlocks.REGISTER.register();
        CozyEffects.REGISTER.register();
        CozyEntities.REGISTER.register(); // before items: spawn eggs refer to the entity types
        CozyItems.REGISTER.register();
        CozyItems.ICE_SNOWBALL.listen(CozyChristmas::registerIceSnowballDispensing);
        CozyBlocks.BLOCK_ENTITIES.register();
        CozyMenus.REGISTER.register();
        CozySounds.REGISTER.register();
        CozyParticles.REGISTER.register();
        CozyItems.TABS.register();
        CozyEntities.init();
        CozyWoods.initWorldgen();

        CosmeticsEvents.register();
        StockingEvents.register();
        ToastySet.register();
        SnowPresents.register();
        KrampusVisits.register();
        Chestnuts.register();
        KarmaRules.register();
        CommandRegistrationEvent.EVENT.register(CozyCommands::register);
        registerCompat();

        LOGGER.info("{} initialized", MOD_ID);
    }

    /** Dispensers throw ice snowballs like snowballs (vanilla's snowball behavior, our projectile). */
    private static void registerIceSnowballDispensing(Item iceSnowball) {
        DispenserBlock.registerBehavior(iceSnowball, new AbstractProjectileDispenseBehavior() {
            @Override
            protected Projectile getProjectile(Level level, Position position, ItemStack stack) {
                return new IceSnowball(level, position.x(), position.y(), position.z(), stack);
            }
        });
    }

    /** Soft dependencies: their classes are only touched when the mod is installed. */
    private static void registerCompat() {
        if (Platform.isModLoaded(SereneSeasonsCalendar.MOD_ID)) {
            SereneSeasonsCalendar.register();
        } else if (CozyConfig.common().season().source() == SeasonSource.SERENE_SEASONS) {
            LOGGER.warn("season.source is serene_seasons, but Serene Seasons isn't installed: the season follows the date");
        }
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
