package dev.eliasnvx.cozychristmas.cosmetic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Item data {@code cozychristmas:cosmetic}: the item is worn in this slot. Item data, not an item class, so modpacks
 * can make any item wearable by adding the NBT (it then needs a look to be drawn, see
 * {@code client.cosmetic.CosmeticLooks}). Later versions make this a data component with the same id.
 */
public record Cosmetic(CosmeticSlot slot) {

    public static final Codec<Cosmetic> CODEC = RecordCodecBuilder.create(i -> i.group(
            CosmeticSlot.CODEC.fieldOf("slot").forGetter(Cosmetic::slot)
    ).apply(i, Cosmetic::new));
}
