package dev.eliasnvx.cozychristmas.registry;

import dev.eliasnvx.cozychristmas.CozyChristmas;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;

/** Damage types (data: {@code data/cozychristmas/damage_type}, both in {@code #minecraft:is_explosion}). */
public final class CozyDamageTypes {

    /** A present found in the world went boom. */
    public static final ResourceKey<DamageType> PRESENT_BOOM = key("present_boom");
    /** A player's wrapped prank went boom: the death message names them. */
    public static final ResourceKey<DamageType> PRESENT_PRANK = key("present_prank");

    private CozyDamageTypes() {
    }

    private static ResourceKey<DamageType> key(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, CozyChristmas.id(name));
    }
}
