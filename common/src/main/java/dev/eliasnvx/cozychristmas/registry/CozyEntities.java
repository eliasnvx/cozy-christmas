package dev.eliasnvx.cozychristmas.registry;

import dev.architectury.registry.level.biome.BiomeModifications;
import dev.architectury.registry.level.entity.EntityAttributeRegistry;
import dev.architectury.registry.level.entity.SpawnPlacementsRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.config.CommonConfig;
import dev.eliasnvx.cozychristmas.config.CozyConfig;
import dev.eliasnvx.cozychristmas.goblin.GingerbreadMan;
import dev.eliasnvx.cozychristmas.goblin.PresentGoblin;
import dev.eliasnvx.cozychristmas.goblin.PresentGoblinSettings;
import dev.eliasnvx.cozychristmas.reindeer.Reindeer;
import dev.eliasnvx.cozychristmas.reindeer.ReindeerSettings;
import dev.eliasnvx.cozychristmas.workshop.Elf;
import dev.eliasnvx.cozychristmas.workshop.Santa;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Entity types, their attributes (from the config) and natural spawns. Santa and the elves only come with the workshop
 * (structure templates), so they're {@code MISC} like villagers and never count against the mob cap. Present goblins
 * and gingerbread men are monsters; gingerbread men spawn only through the goblin camp's spawn overrides.
 */
public final class CozyEntities {

    public static final DeferredRegister<EntityType<?>> REGISTER = DeferredRegister.create(CozyChristmas.MOD_ID, Registries.ENTITY_TYPE);

    public static final ResourceKey<EntityType<?>> ELF_KEY = key("elf");
    public static final RegistrySupplier<EntityType<Elf>> ELF = REGISTER.register(ELF_KEY.identifier(),
            () -> EntityType.Builder.<Elf>of(Elf::new, MobCategory.MISC)
                    .sized(0.5F, 1.05F).eyeHeight(0.9F).clientTrackingRange(10).build(ELF_KEY));

    public static final ResourceKey<EntityType<?>> SANTA_KEY = key("santa");
    public static final RegistrySupplier<EntityType<Santa>> SANTA = REGISTER.register(SANTA_KEY.identifier(),
            () -> EntityType.Builder.<Santa>of(Santa::new, MobCategory.MISC)
                    .sized(0.8F, 2.0F).eyeHeight(1.7F).clientTrackingRange(10).build(SANTA_KEY));

    public static final ResourceKey<EntityType<?>> REINDEER_KEY = key("reindeer");
    public static final RegistrySupplier<EntityType<Reindeer>> REINDEER = REGISTER.register(REINDEER_KEY.identifier(),
            () -> EntityType.Builder.<Reindeer>of(Reindeer::new, MobCategory.CREATURE)
                    .sized(1.1F, 1.45F).eyeHeight(1.35F).passengerAttachments(1.3F).clientTrackingRange(10).build(REINDEER_KEY));

    public static final ResourceKey<EntityType<?>> PRESENT_GOBLIN_KEY = key("present_goblin");
    /** Not allowed in peaceful, except a goblin holding loot (see {@link PresentGoblin#checkDespawn}). */
    public static final RegistrySupplier<EntityType<PresentGoblin>> PRESENT_GOBLIN = REGISTER.register(PRESENT_GOBLIN_KEY.identifier(),
            () -> EntityType.Builder.<PresentGoblin>of(PresentGoblin::new, MobCategory.MONSTER)
                    .sized(0.5F, 0.95F).eyeHeight(0.8F).notInPeaceful().clientTrackingRange(8).build(PRESENT_GOBLIN_KEY));

    public static final ResourceKey<EntityType<?>> GINGERBREAD_MAN_KEY = key("gingerbread_man");
    public static final RegistrySupplier<EntityType<GingerbreadMan>> GINGERBREAD_MAN = REGISTER.register(GINGERBREAD_MAN_KEY.identifier(),
            () -> EntityType.Builder.<GingerbreadMan>of(GingerbreadMan::new, MobCategory.MONSTER)
                    .sized(0.45F, 0.8F).eyeHeight(0.65F).notInPeaceful().clientTrackingRange(8).build(GINGERBREAD_MAN_KEY));

    private CozyEntities() {
    }

    private static ResourceKey<EntityType<?>> key(String name) {
        return ResourceKey.create(Registries.ENTITY_TYPE, CozyChristmas.id(name));
    }

    /** After {@link #REGISTER} is registered: attributes and spawns read the config, which is loaded by then. */
    public static void init() {
        CommonConfig config = CozyConfig.common();
        EntityAttributeRegistry.register(ELF, () -> Elf.createAttributes(config.workshop().elf()));
        EntityAttributeRegistry.register(SANTA, () -> Santa.createAttributes(config.workshop().santa()));
        EntityAttributeRegistry.register(REINDEER, () -> Reindeer.createAttributes(config.reindeer()));
        EntityAttributeRegistry.register(PRESENT_GOBLIN, () -> PresentGoblin.createAttributes(config.goblins().presentGoblin()));
        EntityAttributeRegistry.register(GINGERBREAD_MAN, () -> GingerbreadMan.createAttributes(config.goblins().gingerbreadMan()));

        SpawnPlacementsRegistry.register(REINDEER, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Reindeer::checkReindeerSpawnRules);
        SpawnPlacementsRegistry.register(PRESENT_GOBLIN, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules);
        // any light: their spawns are the camps' overrides only, and the camp is theirs by day too
        SpawnPlacementsRegistry.register(GINGERBREAD_MAN, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkAnyLightMonsterSpawnRules);
        ReindeerSettings reindeer = config.reindeer();
        if (reindeer.spawnWeight() > 0) {
            BiomeModifications.addProperties(context -> context.hasTag(CozyTags.SPAWNS_REINDEER), (context, properties) ->
                    properties.getSpawnProperties().addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(
                            REINDEER.get(), UniformInt.of(reindeer.minGroup(), reindeer.maxGroup())), reindeer.spawnWeight()));
        }
        PresentGoblinSettings goblin = config.goblins().presentGoblin();
        if (goblin.spawnWeight() > 0) {
            BiomeModifications.addProperties(context -> context.hasTag(CozyTags.SPAWNS_PRESENT_GOBLINS), (context, properties) ->
                    properties.getSpawnProperties().addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(
                            PRESENT_GOBLIN.get(), UniformInt.of(goblin.minGroup(), goblin.maxGroup())), goblin.spawnWeight()));
        }
    }
}
