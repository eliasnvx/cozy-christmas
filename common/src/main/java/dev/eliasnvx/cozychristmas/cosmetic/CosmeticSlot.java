package dev.eliasnvx.cozychristmas.cosmetic;

import com.mojang.serialization.Codec;
import dev.eliasnvx.cozychristmas.CozyChristmas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.Locale;

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

    private final String name = name().toLowerCase(Locale.ROOT);
    private final ResourceLocation id = CozyChristmas.id(name);
    private final EquipmentSlot armor;

    CosmeticSlot(EquipmentSlot armor) {
        this.armor = armor;
    }

    public ResourceLocation id() {
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
