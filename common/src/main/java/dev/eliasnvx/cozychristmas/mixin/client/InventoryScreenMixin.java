package dev.eliasnvx.cozychristmas.mixin.client;

import dev.eliasnvx.cozychristmas.client.cosmetic.CosmeticsPanel;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The cosmetics panel on the right of the survival inventory. */
@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin {

    @Inject(method = "extractBackground", at = @At("TAIL"))
    private void cozychristmas$cosmeticsPanel(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        CosmeticsPanel.extractBackground((AbstractContainerScreen<?>) (Object) this, g);
    }
}
