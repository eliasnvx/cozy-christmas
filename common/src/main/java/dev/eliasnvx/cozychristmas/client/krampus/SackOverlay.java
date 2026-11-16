package dev.eliasnvx.cozychristmas.client.krampus;

import dev.architectury.event.events.client.ClientGuiEvent;
import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.registry.CozyEffects;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

/** Inside Krampus's sack you see burlap, with light through the weave (like the carved pumpkin's view). */
public final class SackOverlay {

    private static final Identifier TEXTURE = CozyChristmas.id("textures/misc/sack_overlay.png");

    private SackOverlay() {
    }

    public static void register() {
        ClientGuiEvent.RENDER_HUD.register(SackOverlay::render);
    }

    private static void render(GuiGraphicsExtractor graphics, DeltaTracker delta) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || !minecraft.options.getCameraType().isFirstPerson()
                || !minecraft.player.hasEffect(CozyEffects.SACKED.asHolder())) {
            return;
        }
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, 0, 0, 0.0F, 0.0F, width, height, width, height, ARGB.white(1.0F));
    }
}
