package dev.eliasnvx.cozychristmas.registry;

import dev.eliasnvx.cozychristmas.CozyChristmas;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;

/** The mod's tags: the data-driven knobs for packs. */
public final class CozyTags {

    /** Hurting or killing these mobs is naughty. Farm animals are left out on purpose: food isn't a sin. */
    public static final TagKey<EntityType<?>> KARMA_PROTECTED = TagKey.create(Registries.ENTITY_TYPE,
            CozyChristmas.id("karma_protected"));

    /** Every knitted sweater, whatever the pattern. */
    public static final TagKey<Item> SWEATERS = TagKey.create(Registries.ITEM, CozyChristmas.id("sweaters"));

    /** Pieces of the Toasty set: one in each of its slots completes it ({@code ToastySet}). */
    public static final TagKey<Item> TOASTY_SET = TagKey.create(Registries.ITEM, CozyChristmas.id("toasty_set"));

    /** Blocks the Toasty set is faster on, under the feet or around them (snow layers). */
    public static final TagKey<Block> TOASTY_SNOW = TagKey.create(Registries.BLOCK, CozyChristmas.id("toasty_snow"));

    /** Wrapped at the wrapping table, these make a prank: the present goes boom (one is used, not the stack). */
    public static final TagKey<Item> WRAPS_AS_BOOM = TagKey.create(Registries.ITEM, CozyChristmas.id("wraps_as_boom"));

    /** The wrapping table won't take these as a gift (presents: no present in a present). */
    public static final TagKey<Item> UNWRAPPABLE = TagKey.create(Registries.ITEM, CozyChristmas.id("unwrappable"));

    /** What the wrapping table wraps with; one per present. */
    public static final TagKey<Item> WRAPPING_PAPER = TagKey.create(Registries.ITEM, CozyChristmas.id("wrapping_paper"));

    /** Presents appear in December on top of these (and in thin snow layers on solid ground). */
    public static final TagKey<Block> SNOW_PRESENT_GROUND = TagKey.create(Registries.BLOCK, CozyChristmas.id("snow_present_ground"));

    /** Elves work at these: crafting table, wrapping table and other workbenches. */
    public static final TagKey<Block> ELF_WORKSTATIONS = TagKey.create(Registries.BLOCK, CozyChristmas.id("elf_workstations"));

    /** A player holding one of these near an elf is a threat: the elf runs. */
    public static final TagKey<Item> SCARES_ELVES = TagKey.create(Registries.ITEM, CozyChristmas.id("scares_elves"));

    /** Elves follow a player holding one of these. */
    public static final TagKey<Item> ELF_TREATS = TagKey.create(Registries.ITEM, CozyChristmas.id("elf_treats"));

    /** What a reindeer eats (heals, grows, breeds with the golden ones) and follows. */
    public static final TagKey<Item> REINDEER_FOOD = TagKey.create(Registries.ITEM, CozyChristmas.id("reindeer_food"));

    /** Fed to a wild reindeer, each has a chance to tame it. */
    public static final TagKey<Item> REINDEER_TAMING_ITEMS = TagKey.create(Registries.ITEM, CozyChristmas.id("reindeer_taming_items"));

    /** Wild reindeer spawn on these. */
    public static final TagKey<Block> REINDEER_SPAWNABLE_ON = TagKey.create(Registries.BLOCK, CozyChristmas.id("reindeer_spawnable_on"));

    /** Biomes with wild reindeer herds. */
    public static final TagKey<Biome> SPAWNS_REINDEER = TagKey.create(Registries.BIOME, CozyChristmas.id("spawns_reindeer"));

    /** Items a present goblin never steals (empty by default: packs can protect keys, backpacks, quest items). */
    public static final TagKey<Item> GOBLIN_PROOF = TagKey.create(Registries.ITEM, CozyChristmas.id("goblin_proof"));

    /** Biomes where present goblins prowl at night, outside their camps. */
    public static final TagKey<Biome> SPAWNS_PRESENT_GOBLINS = TagKey.create(Registries.BIOME, CozyChristmas.id("spawns_present_goblins"));

    /** Blocks a stocking counts as hung over: on the block above one of these (or two above). */
    public static final TagKey<Block> FIREPLACES = TagKey.create(Registries.BLOCK, CozyChristmas.id("fireplaces"));

    /** Items that light a fireplace (flint and steel, fire charges). */
    public static final TagKey<Item> LIGHTS_FIREPLACES = TagKey.create(Registries.ITEM, CozyChristmas.id("lights_fireplaces"));

    /** Other items that hang as a stocking when used on a wall (Femboy Mod's socks): they look like a striped sock. */
    public static final TagKey<Item> HANGS_AS_STOCKING = TagKey.create(Registries.ITEM, CozyChristmas.id("hangs_as_stocking"));

    private CozyTags() {
    }
}
