package dev.eliasnvx.cozychristmas.registry;

import dev.eliasnvx.cozychristmas.CozyChristmas;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

/** The mod's tags: the data-driven knobs for packs. */
public final class CozyTags {

    /** Hurting or killing these mobs is naughty. Farm animals are left out on purpose: food isn't a sin. */
    public static final TagKey<EntityType<?>> KARMA_PROTECTED = TagKey.create(Registries.ENTITY_TYPE,
            CozyChristmas.id("karma_protected"));

    private CozyTags() {
    }
}
