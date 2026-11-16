package dev.eliasnvx.cozychristmas.mixin.client;

import dev.eliasnvx.cozychristmas.client.cosmetic.CosmeticsPanel;
import dev.eliasnvx.cozychristmas.cosmetic.menu.CosmeticsLayout;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Status effects are listed right of the inventory; with the cosmetics panel there, they move right of the panel.
 * Both methods start with {@code int x = leftPos + imageWidth + 2}; the panel's width is added to that local where
 * it's first stored. Added, not replaced: other mods with a panel there (Catgirl &amp; Catboy Mod does the same) shift
 * it too, and the effects end up right of every panel. Only the first store: NeoForge stores it again from its effects
 * event, which already saw the shifted value. Same as Catgirl &amp; Catboy Mod's 1.21.1 mixin.
 */
@Mixin(EffectRenderingInventoryScreen.class)
public abstract class EffectRenderingInventoryScreenMixin {

    @ModifyVariable(method = "canSeeEffects", at = @At(value = "STORE", ordinal = 0), index = 1)
    private int cozychristmas$besidePanel(int x) {
        return cozychristmas$shifted(x);
    }

    @ModifyVariable(method = "renderEffects", at = @At(value = "STORE", ordinal = 0), index = 4)
    private int cozychristmas$effectsBesidePanel(int x) {
        return cozychristmas$shifted(x);
    }

    private int cozychristmas$shifted(int x) {
        return CosmeticsPanel.visible((EffectRenderingInventoryScreen<?>) (Object) this)
                ? x + CosmeticsLayout.GAP + CosmeticsLayout.WIDTH : x;
    }
}
