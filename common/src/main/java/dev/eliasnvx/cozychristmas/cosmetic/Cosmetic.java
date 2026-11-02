package dev.eliasnvx.cozychristmas.cosmetic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * Data component {@code cozychristmas:cosmetic}: the item is worn in this slot. A component, not an item class, so
 * modpacks can make any item wearable with a default-component tweak (it then needs a look to be drawn, see
 * {@code client.cosmetic.CosmeticLooks}).
 */
public record Cosmetic(CosmeticSlot slot) {

    public static final Codec<Cosmetic> CODEC = RecordCodecBuilder.create(i -> i.group(
            CosmeticSlot.CODEC.fieldOf("slot").forGetter(Cosmetic::slot)
    ).apply(i, Cosmetic::new));

    public static final StreamCodec<ByteBuf, Cosmetic> STREAM_CODEC =
            CosmeticSlot.STREAM_CODEC.map(Cosmetic::new, Cosmetic::slot);
}
