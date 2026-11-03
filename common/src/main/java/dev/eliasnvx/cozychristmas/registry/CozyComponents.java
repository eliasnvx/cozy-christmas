package dev.eliasnvx.cozychristmas.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.cosmetic.Cosmetic;
import dev.eliasnvx.cozychristmas.present.PresentContents;
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

    /** What's inside a present item; the block entity carries the same while it's placed. */
    public static final RegistrySupplier<DataComponentType<PresentContents>> PRESENT_CONTENTS = REGISTER.register("present_contents",
            () -> DataComponentType.<PresentContents>builder()
                    .persistent(PresentContents.CODEC)
                    .networkSynchronized(PresentContents.STREAM_CODEC)
                    .cacheEncoding()
                    .build());

    private CozyComponents() {
    }
}
