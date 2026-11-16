package dev.eliasnvx.cozychristmas.client.present;

import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.present.PresentDesign;
import dev.eliasnvx.cozychristmas.present.WrappingTableMenu;
import dev.eliasnvx.cozychristmas.registry.CozyItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * The wrapping table: gift and paper on the left, the four designs as buttons in the middle (vanilla's stonecutter
 * button sprites, referenced), the present on the right.
 */
public class WrappingTableScreen extends AbstractContainerScreen<WrappingTableMenu> {

    private static final ResourceLocation BACKGROUND = CozyChristmas.id("textures/gui/container/wrapping_table.png");
    private static final ResourceLocation BUTTON = ResourceLocation.withDefaultNamespace("container/stonecutter/recipe");
    private static final ResourceLocation BUTTON_SELECTED = ResourceLocation.withDefaultNamespace("container/stonecutter/recipe_selected");
    private static final ResourceLocation BUTTON_HIGHLIGHTED = ResourceLocation.withDefaultNamespace("container/stonecutter/recipe_highlighted");
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
        super(menu, inventory, title);
        this.imageWidth = WIDTH;
        this.imageHeight = HEIGHT;
        this.inventoryLabelY = this.imageHeight - 94;
        for (PresentDesign design : PresentDesign.values()) {
            icons[design.ordinal()] = CozyItems.present(design);
            names[design.ordinal()] = Component.translatable("present_design.cozychristmas." + design.getSerializedName());
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(BACKGROUND, leftPos, topPos, 0.0F, 0.0F, imageWidth, imageHeight, TEXTURE_SIZE, TEXTURE_SIZE);
        int hovered = hoveredButton(mouseX, mouseY);
        for (int i = 0; i < icons.length; i++) {
            int x = leftPos + BUTTONS_X + i * BUTTON_WIDTH;
            int y = topPos + BUTTONS_Y;
            ResourceLocation sprite = i == menu.design().ordinal() ? BUTTON_SELECTED : i == hovered ? BUTTON_HIGHLIGHTED : BUTTON;
            graphics.blitSprite(sprite, x, y, BUTTON_WIDTH, BUTTON_HEIGHT);
            graphics.renderItem(icons[i], x, y + 1);
        }
    }

    /** The hovered design's name, drawn over the slots and items like vanilla's tooltips. */
    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderTooltip(graphics, mouseX, mouseY);
        int hovered = hoveredButton(mouseX, mouseY);
        if (hovered >= 0) {
            graphics.renderTooltip(font, names[hovered], mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        int button = hoveredButton(mouseX, mouseY);
        Minecraft minecraft = Minecraft.getInstance();
        if (button >= 0 && minecraft.player != null && minecraft.gameMode != null
                && menu.clickMenuButton(minecraft.player, button)) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F));
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, button);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
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
