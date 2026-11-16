package dev.eliasnvx.cozychristmas.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.krampus.SackedEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;

public final class CozyEffects {

    public static final DeferredRegister<MobEffect> REGISTER = DeferredRegister.create(CozyChristmas.MOD_ID, Registries.MOB_EFFECT);

    /** In Krampus's sack: can't move. */
    public static final RegistrySupplier<MobEffect> SACKED = REGISTER.register("sacked", SackedEffect::new);

    private CozyEffects() {
    }
}
