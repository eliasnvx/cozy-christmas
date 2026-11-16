package dev.eliasnvx.cozychristmas.client.cosmetic;

import dev.eliasnvx.cozychristmas.CozyChristmas;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * How a worn item is drawn: its shape and textures. Like leather armor, the base texture is multiplied by the item's
 * dye color ({@code minecraft:dyed_color}, else {@link #defaultColor}) and the optional overlay is drawn on top
 * untinted (white trims, pompom). Untinted looks ({@link #UNTINTED}) draw the base texture as it is.
 */
public record CosmeticLook(CosmeticShape shape, RenderType base, @Nullable RenderType overlay, int defaultColor) {

    public static final int UNTINTED = -1;

    /** A look with textures {@code textures/entity/cosmetic/<name>.png} (and {@code <name>_overlay.png}). */
    public static CosmeticLook of(CosmeticShape shape, String name, boolean withOverlay, int defaultColor) {
        return new CosmeticLook(shape, type(name), withOverlay ? type(name + "_overlay") : null, defaultColor);
    }

    public boolean tinted() {
        return defaultColor != UNTINTED;
    }

    /** 24-bit color to multiply the base texture with; {@code dye} is the worn stack's color or -1 if undyed. */
    public int baseColor(int dye) {
        if (!tinted()) {
            return 0xFFFFFF;
        }
        return dye >= 0 ? dye : defaultColor;
    }

    private static RenderType type(String name) {
        // like armor: cutout, both faces (thin parts like the scarf ends are seen from behind)
        ResourceLocation texture = CozyChristmas.id("textures/entity/cosmetic/" + name + ".png");
        return RenderType.armorCutoutNoCull(texture);
    }
}
