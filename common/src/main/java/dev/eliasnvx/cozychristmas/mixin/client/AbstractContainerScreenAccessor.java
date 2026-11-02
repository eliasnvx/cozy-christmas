package dev.eliasnvx.cozychristmas.mixin.client;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** The inventory's position moves with the recipe book; the cosmetics panel sits on its right edge. */
@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {

    @Accessor("leftPos")
    int cozychristmas$leftPos();

    @Accessor("topPos")
    int cozychristmas$topPos();

    @Accessor("imageWidth")
    int cozychristmas$imageWidth();
}
