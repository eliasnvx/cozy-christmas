package dev.eliasnvx.cozychristmas.neoforge.compat;

import dev.eliasnvx.cozychristmas.compat.ExternalSlots;
import net.minecraft.core.NonNullList;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;


/**
 * Curios (SPEC compat, soft dependency, NeoForge only): our cosmetics in Curios slots count as worn and render like
 * ours, the Curios way. A piece in a regular slot counts for the Toasty set and is drawn while its slot's show toggle is
 * on; a piece in a cosmetic slot is drawn over what's worn there and counts for nothing. Which item fits which Curios
 * slot is data: {@code data/curios/tags/item} (head, necklace, body, hands) and the player's slots in
 * {@code data/cozychristmas/curios/entities}. Loaded only with Curios. Ported from Catgirl & Catboy Mod.
 */
public final class CuriosSlots {

    public static final String MOD_ID = "curios";

    private CuriosSlots() {
    }

    public static void register() {
        ExternalSlots.register((player, visitor) -> CuriosApi.getCuriosInventory(player).ifPresent(inventory -> {
            for (ICurioStacksHandler handler : inventory.getCurios().values()) {
                visit(handler, visitor);
            }
        }));
    }

    private static void visit(ICurioStacksHandler handler, ExternalSlots.Visitor visitor) {
        IDynamicStackHandler stacks = handler.getStacks();
        NonNullList<Boolean> renders = handler.getRenders();
        for (int i = 0; i < stacks.getSlots(); i++) {
            boolean shown = i >= renders.size() || renders.get(i);
            visitor.accept(stacks.getStackInSlot(i), shown ? ExternalSlots.Role.WORN : ExternalSlots.Role.HIDDEN);
        }
        if (handler.hasCosmetic()) {
            IDynamicStackHandler cosmetics = handler.getCosmeticStacks();
            for (int i = 0; i < cosmetics.getSlots(); i++) {
                visitor.accept(cosmetics.getStackInSlot(i), ExternalSlots.Role.LOOK);
            }
        }
    }
}
