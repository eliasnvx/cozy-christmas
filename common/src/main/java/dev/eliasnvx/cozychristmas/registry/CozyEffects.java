package dev.eliasnvx.cozychristmas.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.food.ChimesEffect;
import dev.eliasnvx.cozychristmas.food.WarmEffect;
import dev.eliasnvx.cozychristmas.krampus.SackedEffect;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;

public final class CozyEffects {

    public static final DeferredRegister<MobEffect> REGISTER = DeferredRegister.create(CozyChristmas.MOD_ID, Registries.MOB_EFFECT);

    /** In Krampus's sack: can't move. */
    public static final RegistrySupplier<MobEffect> SACKED = REGISTER.register("sacked", SackedEffect::new);
    /** Hot cocoa: thawed and kept warm. */
    public static final RegistrySupplier<MobEffect> WARM = REGISTER.register("warm", WarmEffect::new);
    public static final ResourceKey<MobEffect> WARM_KEY = ResourceKey.create(Registries.MOB_EFFECT, CozyChristmas.id("warm"));
    /** A tangerine at midnight: the bell strikes twelve. Hidden. */
    public static final RegistrySupplier<MobEffect> CHIMES = REGISTER.register("chimes", ChimesEffect::new);

    private CozyEffects() {
    }

    /**
     * The registry's own holder of one of our effects, for effect instances and {@code hasEffect}. An Architectury 13
     * {@link RegistrySupplier} is a {@link Holder} too, but not the registry's reference: entities key their effects by
     * holder, so mixing the two would miss effects given by commands or synced from the server.
     */
    public static Holder<MobEffect> holder(RegistrySupplier<MobEffect> effect) {
        return BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect.get());
    }
}
