package dev.eliasnvx.cozychristmas.registry;

import dev.eliasnvx.cozychristmas.CozyChristmas;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
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

    private CozyTags() {
    }
}
