package dev.eliasnvx.cozychristmas.client.present;

import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.present.PresentDesign;
import dev.eliasnvx.cozychristmas.present.WrappingTableMenu;
import dev.eliasnvx.cozychristmas.registry.CozyItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * The wrapping table: gift and paper on the left, the four designs as buttons in the middle (vanilla's stonecutter
 * button sprites, referenced), the present on the right.
 */
public class WrappingTableScreen extends AbstractContainerScreen<WrappingTableMenu> {

    private static final Identifier BACKGROUND = CozyChristmas.id("textures/gui/container/wrapping_table.png");
    private static final Identifier BUTTON = Identifier.withDefaultNamespace("container/stonecutter/recipe");
    private static final Identifier BUTTON_SELECTED = Identifier.withDefaultNamespace("container/stonecutter/recipe_selected");
    private static final Identifier BUTTON_HIGHLIGHTED = Identifier.withDefaultNamespace("container/stonecutter/recipe_highlighted");
    private static final int TEXTURE_SIZE = 256;
    private static final int WIDTH = 176;
    private static final int HEIGHT = 166;
    public static final int BUTTONS_X = 52;
    public static final int BUTTONS_Y = 33;
    private static final int BUTTON_WIDTH = 16;
    private static final int BUTTON_HEIGHT = 18;

    /** The button icons, one present per design; built once, nothing allocated per frame. */
    private final ItemStack[] icons = new ItemStack[PresentDesign.values().length];
    private final Component[] names = new Component[PresentDesign.values().length];

    public WrappingTableScreen(WrappingTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, WIDTH, HEIGHT);
        this.inventoryLabelY = this.imageHeight - 94;
        for (PresentDesign design : PresentDesign.values()) {
            icons[design.ordinal()] = CozyItems.present(design);
            names[design.ordinal()] = Component.translatable("present_design.cozychristmas." + design.getSerializedName());
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, leftPos, topPos, 0.0F, 0.0F, imageWidth, imageHeight,
                TEXTURE_SIZE, TEXTURE_SIZE);
        int hovered = hoveredButton(mouseX, mouseY);
        for (int i = 0; i < icons.length; i++) {
            int x = leftPos + BUTTONS_X + i * BUTTON_WIDTH;
            int y = topPos + BUTTONS_Y;
            Identifier sprite = i == menu.design().ordinal() ? BUTTON_SELECTED : i == hovered ? BUTTON_HIGHLIGHTED : BUTTON;
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, BUTTON_WIDTH, BUTTON_HEIGHT);
            graphics.item(icons[i], x, y + 1);
        }
        if (hovered >= 0) {
            graphics.setTooltipForNextFrame(font, names[hovered], mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int button = hoveredButton(event.x(), event.y());
        Minecraft minecraft = Minecraft.getInstance();
        if (button >= 0 && minecraft.player != null && minecraft.gameMode != null
                && menu.clickMenuButton(minecraft.player, button)) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F));
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, button);
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    private int hoveredButton(double mouseX, double mouseY) {
        double x = mouseX - leftPos - BUTTONS_X;
        double y = mouseY - topPos - BUTTONS_Y;
        if (x < 0 || y < 0 || y >= BUTTON_HEIGHT || x >= icons.length * BUTTON_WIDTH) {
            return -1;
        }
        return (int) (x / BUTTON_WIDTH);
    }
}
