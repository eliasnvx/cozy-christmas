package dev.eliasnvx.cozychristmas.mixin;

import dev.eliasnvx.cozychristmas.present.PresentItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Decided surprise presents stack to 1. Later versions copy a stack size component in the loot table; 1.20.1 vanilla only
 * asks the item, so the stack asks {@link PresentItem#getMaxStackSize(ItemStack)} (Forge would ask it too).
 */
@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Inject(method = "getMaxStackSize", at = @At("HEAD"), cancellable = true)
    private void cozychristmas$presentStackSize(CallbackInfoReturnable<Integer> cir) {
        ItemStack self = (ItemStack) (Object) this;
        if (self.getItem() instanceof PresentItem present) {
            cir.setReturnValue(present.getMaxStackSize(self));
        }
    }
}
