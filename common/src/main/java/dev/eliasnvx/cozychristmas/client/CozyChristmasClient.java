package dev.eliasnvx.cozychristmas.client;

import dev.architectury.event.events.client.ClientTooltipEvent;
import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.client.cosmetic.CosmeticLooks;
import dev.eliasnvx.cozychristmas.client.cosmetic.CosmeticShape;
import dev.eliasnvx.cozychristmas.cosmetic.CosmeticSlot;
import dev.eliasnvx.cozychristmas.cosmetic.CosmeticsManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Client entry point, called by each loader's client initializer after {@link CozyChristmas#init()}. The loaders add
 * {@code CosmeticLayer} to the player renderers themselves (no common hook for that).
 */
public final class CozyChristmasClient {

    private CozyChristmasClient() {
    }

    public static void init() {
        CosmeticShape.registerLayers();
        CosmeticLooks.registerDefaults();
        ClientTooltipEvent.ITEM.register(CozyChristmasClient::appendTooltip);

        CozyChristmas.LOGGER.info("{} client initialized", CozyChristmas.MOD_ID);
    }

    private static void appendTooltip(ItemStack stack, List<Component> lines, Item.TooltipContext context, TooltipFlag flag) {
        CosmeticSlot slot = CosmeticsManager.slotOf(stack);
        if (slot != null) {
            lines.add(Component.translatable("tooltip.cozychristmas.slot", Component.translatable(slot.translationKey()))
                    .withStyle(ChatFormatting.GRAY));
        }
    }
}
