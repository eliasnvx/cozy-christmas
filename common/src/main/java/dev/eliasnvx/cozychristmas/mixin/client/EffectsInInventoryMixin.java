package dev.eliasnvx.cozychristmas.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.eliasnvx.cozychristmas.client.cosmetic.CosmeticsPanel;
import dev.eliasnvx.cozychristmas.cosmetic.menu.CosmeticsLayout;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EffectsInInventory;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Status effects are listed right of the inventory; with the cosmetics panel there, they move right of the panel.
 * The width is added to what the screen's width reads as, not replaced (MixinExtras chains): other mods with a panel
 * there (Catgirl &amp; Catboy Mod) widen it too, and the effects end up right of every panel.
 */
@Mixin(EffectsInInventory.class)
public abstract class EffectsInInventoryMixin {

    @Shadow
    @Final
    private AbstractContainerScreen<?> screen;

    @ModifyExpressionValue(method = {"canSeeEffects", "extractRenderState"}, at = @At(value = "FIELD",
            opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;imageWidth:I"))
    private int cozychristmas$besidePanel(int width) {
        return CosmeticsPanel.visible(screen) ? width + CosmeticsLayout.GAP + CosmeticsLayout.WIDTH : width;
    }
}
