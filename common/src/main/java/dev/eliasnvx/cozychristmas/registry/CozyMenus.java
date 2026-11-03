package dev.eliasnvx.cozychristmas.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.present.WrappingTableMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public final class CozyMenus {

    public static final DeferredRegister<MenuType<?>> REGISTER = DeferredRegister.create(CozyChristmas.MOD_ID, Registries.MENU);

    // the constructor is private in vanilla; Architectury's access widener opens it
    public static final RegistrySupplier<MenuType<WrappingTableMenu>> WRAPPING_TABLE = REGISTER.register("wrapping_table",
            () -> new MenuType<>(WrappingTableMenu::new, FeatureFlags.VANILLA_SET));

    private CozyMenus() {
    }
}
