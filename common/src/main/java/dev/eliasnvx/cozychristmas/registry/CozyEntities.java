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
import dev.eliasnvx.cozychristmas.krampus.Krampus;
import dev.eliasnvx.cozychristmas.krampus.KrampusSack;
import dev.eliasnvx.cozychristmas.reindeer.Reindeer;
import dev.eliasnvx.cozychristmas.reindeer.ReindeerSettings;
import dev.eliasnvx.cozychristmas.sleigh.SantasSleigh;
import dev.eliasnvx.cozychristmas.snowman.EvilSnowman;
import dev.eliasnvx.cozychristmas.snowman.EvilSnowmanSettings;
import dev.eliasnvx.cozychristmas.snowman.IceSnowball;
import dev.eliasnvx.cozychristmas.workshop.Elf;
import dev.eliasnvx.cozychristmas.workshop.Santa;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

/**
 * Entity types, their attributes (from the config) and natural spawns. Santa and the elves only come with the workshop
 * (structure templates), so they're {@code MISC} like villagers and never count against the mob cap. Present goblins
 * and gingerbread men are monsters; gingerbread men spawn only through the goblin camp's spawn overrides. Evil snowmen
 * are monsters of snowy nights. 1.21.1 has no {@code notInPeaceful()}: the monsters leave peaceful worlds through
 * {@code Monster#shouldDespawnInPeaceful}, and missing entity loot tables are empty, so no {@code noLootTable()} either.
 */
public final class CozyEntities {

    public static final DeferredRegister<EntityType<?>> REGISTER = DeferredRegister.create(CozyChristmas.MOD_ID, Registries.ENTITY_TYPE);

    public static final ResourceKey<EntityType<?>> ELF_KEY = key("elf");
    public static final RegistrySupplier<EntityType<Elf>> ELF = REGISTER.register(ELF_KEY.location(),
            () -> EntityType.Builder.<Elf>of(Elf::new, MobCategory.MISC)
                    .sized(0.5F, 1.05F).eyeHeight(0.9F).clientTrackingRange(10).build(ELF_KEY.location().toString()));

    public static final ResourceKey<EntityType<?>> SANTA_KEY = key("santa");
    public static final RegistrySupplier<EntityType<Santa>> SANTA = REGISTER.register(SANTA_KEY.location(),
            () -> EntityType.Builder.<Santa>of(Santa::new, MobCategory.MISC)
                    .sized(0.8F, 2.0F).eyeHeight(1.7F).clientTrackingRange(10).build(SANTA_KEY.location().toString()));

    public static final ResourceKey<EntityType<?>> REINDEER_KEY = key("reindeer");
    public static final RegistrySupplier<EntityType<Reindeer>> REINDEER = REGISTER.register(REINDEER_KEY.location(),
            () -> EntityType.Builder.<Reindeer>of(Reindeer::new, MobCategory.CREATURE)
                    .sized(1.1F, 1.45F).eyeHeight(1.35F).passengerAttachments(1.3F).clientTrackingRange(10)
                    .build(REINDEER_KEY.location().toString()));

    public static final ResourceKey<EntityType<?>> PRESENT_GOBLIN_KEY = key("present_goblin");
    /** Not allowed in peaceful, except a goblin holding loot (see {@link PresentGoblin#checkDespawn}). */
    public static final RegistrySupplier<EntityType<PresentGoblin>> PRESENT_GOBLIN = REGISTER.register(PRESENT_GOBLIN_KEY.location(),
            () -> EntityType.Builder.<PresentGoblin>of(PresentGoblin::new, MobCategory.MONSTER)
                    .sized(0.5F, 0.95F).eyeHeight(0.8F).clientTrackingRange(8).build(PRESENT_GOBLIN_KEY.location().toString()));

    public static final ResourceKey<EntityType<?>> GINGERBREAD_MAN_KEY = key("gingerbread_man");
    public static final RegistrySupplier<EntityType<GingerbreadMan>> GINGERBREAD_MAN = REGISTER.register(GINGERBREAD_MAN_KEY.location(),
            () -> EntityType.Builder.<GingerbreadMan>of(GingerbreadMan::new, MobCategory.MONSTER)
                    .sized(0.45F, 0.8F).eyeHeight(0.65F).clientTrackingRange(8).build(GINGERBREAD_MAN_KEY.location().toString()));

    public static final ResourceKey<EntityType<?>> EVIL_SNOWMAN_KEY = key("evil_snowman");
    public static final RegistrySupplier<EntityType<EvilSnowman>> EVIL_SNOWMAN = REGISTER.register(EVIL_SNOWMAN_KEY.location(),
            () -> EntityType.Builder.<EvilSnowman>of(EvilSnowman::new, MobCategory.MONSTER)
                    .sized(0.7F, 1.9F).eyeHeight(1.7F).clientTrackingRange(8).build(EVIL_SNOWMAN_KEY.location().toString()));

    public static final ResourceKey<EntityType<?>> ICE_SNOWBALL_KEY = key("ice_snowball");
    /** Sized, tracked and updated like vanilla's snowball. */
    public static final RegistrySupplier<EntityType<IceSnowball>> ICE_SNOWBALL = REGISTER.register(ICE_SNOWBALL_KEY.location(),
            () -> EntityType.Builder.<IceSnowball>of(IceSnowball::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10).build(ICE_SNOWBALL_KEY.location().toString()));

    public static final ResourceKey<EntityType<?>> KRAMPUS_KEY = key("krampus");
    /** A boss: fire can't hurt him, he never spawns naturally (his lair and his visits bring him). */
    public static final RegistrySupplier<EntityType<Krampus>> KRAMPUS = REGISTER.register(KRAMPUS_KEY.location(),
            () -> EntityType.Builder.<Krampus>of(Krampus::new, MobCategory.MONSTER)
                    .sized(1.0F, 2.75F).eyeHeight(2.45F).fireImmune().clientTrackingRange(10).build(KRAMPUS_KEY.location().toString()));

    public static final ResourceKey<EntityType<?>> KRAMPUS_SACK_KEY = key("krampus_sack");
    public static final RegistrySupplier<EntityType<KrampusSack>> KRAMPUS_SACK = REGISTER.register(KRAMPUS_SACK_KEY.location(),
            () -> EntityType.Builder.<KrampusSack>of(KrampusSack::new, MobCategory.MISC)
                    .noSummon().sized(1.15F, 2.3F).clientTrackingRange(10).updateInterval(1).build(KRAMPUS_SACK_KEY.location().toString()));

    public static final ResourceKey<EntityType<?>> SANTAS_SLEIGH_KEY = key("santas_sleigh");
    /** Santa sits at the back; tracked far, so it's seen climbing into the night sky. */
    public static final RegistrySupplier<EntityType<SantasSleigh>> SANTAS_SLEIGH = REGISTER.register(SANTAS_SLEIGH_KEY.location(),
            () -> EntityType.Builder.<SantasSleigh>of(SantasSleigh::new, MobCategory.MISC)
                    .sized(2.0F, 1.3F).passengerAttachments(new Vec3(0.0, 0.55, -0.55))
                    .clientTrackingRange(16).updateInterval(1).build(SANTAS_SLEIGH_KEY.location().toString()));

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
        EntityAttributeRegistry.register(EVIL_SNOWMAN, () -> EvilSnowman.createAttributes(config.evilSnowman()));
        EntityAttributeRegistry.register(KRAMPUS, () -> Krampus.createAttributes(config.krampus()));

        SpawnPlacementsRegistry.register(REINDEER, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Reindeer::checkReindeerSpawnRules);
        SpawnPlacementsRegistry.register(PRESENT_GOBLIN, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules);
        // any light: their spawns are the camps' overrides only, and the camp is theirs by day too
        SpawnPlacementsRegistry.register(GINGERBREAD_MAN, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkAnyLightMonsterSpawnRules);
        SpawnPlacementsRegistry.register(EVIL_SNOWMAN, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                EvilSnowman::checkEvilSnowmanSpawnRules);
        ReindeerSettings reindeer = config.reindeer();
        if (reindeer.spawnWeight() > 0) {
            BiomeModifications.addProperties(context -> context.hasTag(CozyTags.SPAWNS_REINDEER), (context, properties) ->
                    properties.getSpawnProperties().addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(
                            REINDEER.get(), reindeer.spawnWeight(), reindeer.minGroup(), reindeer.maxGroup())));
        }
        PresentGoblinSettings goblin = config.goblins().presentGoblin();
        if (goblin.spawnWeight() > 0) {
            BiomeModifications.addProperties(context -> context.hasTag(CozyTags.SPAWNS_PRESENT_GOBLINS), (context, properties) ->
                    properties.getSpawnProperties().addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(
                            PRESENT_GOBLIN.get(), goblin.spawnWeight(), goblin.minGroup(), goblin.maxGroup())));
        }
        EvilSnowmanSettings snowman = config.evilSnowman();
        if (snowman.spawnWeight() > 0) {
            BiomeModifications.addProperties(context -> context.hasTag(CozyTags.SPAWNS_EVIL_SNOWMEN), (context, properties) ->
                    properties.getSpawnProperties().addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(
                            EVIL_SNOWMAN.get(), snowman.spawnWeight(), snowman.minGroup(), snowman.maxGroup())));
        }
    }
}
