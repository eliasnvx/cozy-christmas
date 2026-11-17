package dev.eliasnvx.cozychristmas.mixin.client;

import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.wood.CozyWoods;
import dev.eliasnvx.cozychristmas.wood.WoodSet;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.level.block.state.properties.WoodType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * {@link Sheets} freezes its sign material maps at class load, which may happen before our woods register.
 * Return our materials directly so signs and hanging signs always render.
 */
@Mixin(Sheets.class)
public abstract class SheetsMixin {

    @Inject(method = "getSignMaterial", at = @At("HEAD"), cancellable = true)
    private static void cozychristmas$signMaterial(WoodType type, CallbackInfoReturnable<Material> cir) {
        for (WoodSet set : CozyWoods.ALL) {
            if (set.woodType == type) {
                cir.setReturnValue(new Material(Sheets.SIGN_SHEET, CozyChristmas.id("entity/signs/" + set.name)));
                return;
            }
        }
    }

    @Inject(method = "getHangingSignMaterial", at = @At("HEAD"), cancellable = true)
    private static void cozychristmas$hangingSignMaterial(WoodType type, CallbackInfoReturnable<Material> cir) {
        for (WoodSet set : CozyWoods.ALL) {
            if (set.woodType == type) {
                cir.setReturnValue(new Material(Sheets.SIGN_SHEET, CozyChristmas.id("entity/signs/hanging/" + set.name)));
                return;
            }
        }
    }
}
