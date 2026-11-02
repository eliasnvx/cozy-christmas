package dev.eliasnvx.cozychristmas.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.cosmetic.Cosmetic;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;

public final class CozyComponents {

    public static final DeferredRegister<DataComponentType<?>> REGISTER =
            DeferredRegister.create(CozyChristmas.MOD_ID, Registries.DATA_COMPONENT_TYPE);

    public static final RegistrySupplier<DataComponentType<Cosmetic>> COSMETIC = REGISTER.register("cosmetic",
            () -> DataComponentType.<Cosmetic>builder()
                    .persistent(Cosmetic.CODEC)
                    .networkSynchronized(Cosmetic.STREAM_CODEC)
                    .cacheEncoding()
                    .build());

    private CozyComponents() {
    }
}
