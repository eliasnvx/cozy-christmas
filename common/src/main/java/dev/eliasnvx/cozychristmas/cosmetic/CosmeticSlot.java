package dev.eliasnvx.cozychristmas.cosmetic;

import com.mojang.serialization.Codec;
import dev.eliasnvx.cozychristmas.CozyChristmas;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.ByIdMap;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.Locale;
import java.util.function.IntFunction;

/**
 * Cosmetic slots, worn on top of armor (armor is never removed or hidden). Declaration order = screen order.
 * Stored by id, so slots can be added later without breaking saves.
 */
public enum CosmeticSlot implements StringRepresentable {
    HEAD(EquipmentSlot.HEAD),
    NECK(EquipmentSlot.CHEST),
    BODY(EquipmentSlot.CHEST),
    HANDS(EquipmentSlot.CHEST);

    public static final Codec<CosmeticSlot> CODEC = StringRepresentable.fromEnum(CosmeticSlot::values);
    /** An unknown id (a client can send any) reads as the first slot instead of throwing. */
    private static final IntFunction<CosmeticSlot> BY_ID =
            ByIdMap.continuous(Enum::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, CosmeticSlot> STREAM_CODEC =
            ByteBufCodecs.idMapper(BY_ID, Enum::ordinal);

    private final String name = name().toLowerCase(Locale.ROOT);
    private final Identifier id = CozyChristmas.id(name);
    private final EquipmentSlot armor;

    CosmeticSlot(EquipmentSlot armor) {
        this.armor = armor;
    }

    public Identifier id() {
        return id;
    }

    /** The armor piece this slot is drawn over: while it shows, the cosmetic is drawn a size larger. */
    public EquipmentSlot armor() {
        return armor;
    }

    /** {@code cosmetic_slot.cozychristmas.<slot>} */
    public String translationKey() {
        return id.toLanguageKey("cosmetic_slot");
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
