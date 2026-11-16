package dev.eliasnvx.cozychristmas.neoforge.compat;

import dev.eliasnvx.cozychristmas.compat.ExternalSlots;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.function.Consumer;

/**
 * Curios (SPEC compat, soft dependency, NeoForge only): our cosmetics in Curios slots count as worn (the Toasty set
 * too) and render like ours. Which item fits which Curios slot is data: {@code data/curios/tags/item} (head, necklace,
 * body, hands) and the player's slots in {@code data/cozychristmas/curios/entities}. Loaded only with Curios. Ported
 * from Catgirl & Catboy Mod.
 */
public final class CuriosSlots {

    public static final String MOD_ID = "curios";

    private CuriosSlots() {
    }

    public static void register() {
        ExternalSlots.register((player, action) -> CuriosApi.getCuriosInventory(player).ifPresent(inventory -> {
            for (ICurioStacksHandler handler : inventory.getCurios().values()) {
                visit(handler.getStacks(), action);
                visit(handler.getCosmeticStacks(), action);
            }
        }));
    }

    private static void visit(IDynamicStackHandler stacks, Consumer<ItemStack> action) {
        for (int i = 0; i < stacks.getSlots(); i++) {
            action.accept(stacks.getStackInSlot(i));
        }
    }
}
