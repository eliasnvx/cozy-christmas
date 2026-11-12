package dev.eliasnvx.cozychristmas.client.cosmetic;

import dev.architectury.registry.registries.RegistrySupplier;
import dev.eliasnvx.cozychristmas.registry.CozyItems;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Which look each wearable item has. Items without one are worn but not drawn (a modpack's custom cosmetic). */
public final class CosmeticLooks {

    private static final Map<Item, CosmeticLook> LOOKS = new ConcurrentHashMap<>();

    private CosmeticLooks() {
    }

    /** Built-in looks; called once the items exist (client init runs after registration). */
    public static void registerDefaults() {
        for (RegistrySupplier<Item> sweater : CozyItems.sweaters()) {
            sweater.listen(item -> register(item, CosmeticLook.of(CosmeticShape.SWEATER, sweater.getId().getPath(), false,
                    CosmeticLook.UNTINTED)));
        }
        knit(CozyItems.POMPOM_HAT, CosmeticShape.POMPOM_HAT);
        knit(CozyItems.SCARF, CosmeticShape.SCARF);
        knit(CozyItems.MITTENS, CosmeticShape.MITTENS);
        knit(CozyItems.EAR_WARMERS, CosmeticShape.EAR_WARMERS);
        dyeable(CozyItems.ELF_HAT, CosmeticShape.ELF_HAT, CozyItems.ELF_GREEN);
        look(CozyItems.ANTLER_HEADBAND, CosmeticLook.of(CosmeticShape.ANTLER_HEADBAND,
                CozyItems.ANTLER_HEADBAND.getId().getPath(), false, CosmeticLook.UNTINTED));
    }

    /** Dyeable knitwear: a grayscale knit tinted with the dye, white trims on top. */
    private static void knit(RegistrySupplier<Item> item, CosmeticShape shape) {
        dyeable(item, shape, CozyItems.KNIT_RED);
    }

    /** Grayscale base tinted with the dye ({@code undyed} without one), untinted overlay on top. */
    private static void dyeable(RegistrySupplier<Item> item, CosmeticShape shape, int undyed) {
        look(item, CosmeticLook.of(shape, item.getId().getPath(), true, undyed));
    }

    private static void look(RegistrySupplier<Item> item, CosmeticLook look) {
        item.listen(registered -> register(registered, look));
    }

    public static void register(Item item, CosmeticLook look) {
        LOOKS.put(item, look);
    }

    public static @Nullable CosmeticLook get(Item item) {
        return LOOKS.get(item);
    }
}
