package dev.eliasnvx.cozychristmas.client.krampus;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.architectury.event.events.client.ClientGuiEvent;
import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.registry.CozyEffects;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/** Inside Krampus's sack you see burlap, with light through the weave (like the carved pumpkin's view). */
public final class SackOverlay {

    private static final ResourceLocation TEXTURE = CozyChristmas.id("textures/misc/sack_overlay.png");
    /** The z vanilla draws its texture overlays at. */
    private static final int BLIT_OFFSET = -90;

    private SackOverlay() {
    }

    public static void register() {
        ClientGuiEvent.RENDER_HUD.register(SackOverlay::render);
    }

    /** Drawn like vanilla's carved pumpkin overlay ({@code Gui#renderTextureOverlay}): blended, no depth. */
    private static void render(GuiGraphics graphics, DeltaTracker delta) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || !minecraft.options.getCameraType().isFirstPerson()
                || !minecraft.player.hasEffect(CozyEffects.holder(CozyEffects.SACKED))) {
            return;
        }
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        graphics.blit(TEXTURE, 0, 0, BLIT_OFFSET, 0.0F, 0.0F, width, height, width, height);
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }
}
