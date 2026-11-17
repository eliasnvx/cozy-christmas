package dev.eliasnvx.cozychristmas.client;

import dev.architectury.event.events.client.ClientTooltipEvent;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import dev.architectury.registry.client.particle.ParticleProviderRegistry;
import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.client.cosmetic.CosmeticLooks;
import dev.eliasnvx.cozychristmas.client.cosmetic.CosmeticShape;
import dev.eliasnvx.cozychristmas.client.present.ConfettiParticle;
import dev.eliasnvx.cozychristmas.client.goblin.GingerbreadManRenderer;
import dev.eliasnvx.cozychristmas.client.goblin.PresentGoblinRenderer;
import dev.eliasnvx.cozychristmas.client.krampus.KrampusRenderer;
import dev.eliasnvx.cozychristmas.client.krampus.SackOverlay;
import dev.eliasnvx.cozychristmas.client.krampus.SackRenderer;
import dev.eliasnvx.cozychristmas.client.reindeer.ReindeerRenderer;
import dev.eliasnvx.cozychristmas.client.sleigh.SleighRenderer;
import dev.eliasnvx.cozychristmas.client.snowman.EvilSnowmanRenderer;
import dev.eliasnvx.cozychristmas.client.wood.WoodBoatRenderers;
import dev.eliasnvx.cozychristmas.client.workshop.ElfRenderer;
import dev.eliasnvx.cozychristmas.client.workshop.SantaRenderer;
import dev.eliasnvx.cozychristmas.cosmetic.CosmeticSlot;
import dev.eliasnvx.cozychristmas.cosmetic.CosmeticsManager;
import dev.eliasnvx.cozychristmas.cosmetic.ToastySet;
import dev.eliasnvx.cozychristmas.present.PresentContents;
import dev.eliasnvx.cozychristmas.registry.CozyBlocks;
import dev.eliasnvx.cozychristmas.registry.CozyComponents;
import dev.eliasnvx.cozychristmas.registry.CozyEntities;
import dev.eliasnvx.cozychristmas.registry.CozyItems;
import dev.eliasnvx.cozychristmas.registry.CozyParticles;
import dev.eliasnvx.cozychristmas.registry.CozyTags;
import dev.eliasnvx.cozychristmas.tree.OrnamentBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Client entry point, called by each loader's client initializer after {@link CozyChristmas#init()}. The loaders add
 * {@code CosmeticLayer} to the player renderers and register the screens and block entity renderers themselves (their
 * events need the registered types).
 */
public final class CozyChristmasClient {

    private CozyChristmasClient() {
    }

    public static void init() {
        CosmeticShape.registerLayers();
        CosmeticLooks.registerDefaults();
        ClientTooltipEvent.ITEM.register(CozyChristmasClient::appendTooltip);
        ParticleProviderRegistry.register(CozyParticles.CONFETTI, sprites -> new ConfettiParticle.Provider(sprites));
        ElfRenderer.registerLayers();
        SantaRenderer.registerLayers();
        ReindeerRenderer.registerLayers();
        PresentGoblinRenderer.registerLayers();
        GingerbreadManRenderer.registerLayers();
        EvilSnowmanRenderer.registerLayers();
        KrampusRenderer.registerLayers();
        SackRenderer.registerLayers();
        SleighRenderer.registerLayers();
        SackOverlay.register();
        WoodBoatRenderers.register();
        EntityRendererRegistry.register(CozyEntities.ELF, ElfRenderer::new);
        EntityRendererRegistry.register(CozyEntities.SANTA, SantaRenderer::new);
        EntityRendererRegistry.register(CozyEntities.REINDEER, ReindeerRenderer::new);
        EntityRendererRegistry.register(CozyEntities.PRESENT_GOBLIN, PresentGoblinRenderer::new);
        EntityRendererRegistry.register(CozyEntities.GINGERBREAD_MAN, GingerbreadManRenderer::new);
        EntityRendererRegistry.register(CozyEntities.EVIL_SNOWMAN, EvilSnowmanRenderer::new);
        EntityRendererRegistry.register(CozyEntities.ICE_SNOWBALL, ThrownItemRenderer::new);
        EntityRendererRegistry.register(CozyEntities.KRAMPUS, KrampusRenderer::new);
        EntityRendererRegistry.register(CozyEntities.KRAMPUS_SACK, SackRenderer::new);
        EntityRendererRegistry.register(CozyEntities.SANTAS_SLEIGH, SleighRenderer::new);

        CozyChristmas.LOGGER.info("{} client initialized", CozyChristmas.MOD_ID);
    }

    private static void appendTooltip(ItemStack stack, List<Component> lines, Item.TooltipContext context, TooltipFlag flag) {
        CosmeticSlot slot = CosmeticsManager.slotOf(stack);
        if (slot != null) {
            lines.add(Component.translatable("tooltip.cozychristmas.slot", Component.translatable(slot.translationKey()))
                    .withStyle(ChatFormatting.GRAY));
        }
        if (stack.is(CozyTags.TOASTY_SET)) {
            appendToastySet(lines);
        }
        PresentContents present = stack.get(CozyComponents.PRESENT_CONTENTS.get());
        if (present != null) {
            appendPresent(present, lines);
        }
        if (stack.is(CozyBlocks.STOCKING_ITEM.get())) {
            lines.add(Component.translatable("tooltip.cozychristmas.stocking").withStyle(ChatFormatting.GRAY));
        } else if (stack.is(CozyTags.HANGS_AS_STOCKING)) {
            lines.add(Component.translatable("tooltip.cozychristmas.hangs_as_stocking").withStyle(ChatFormatting.GRAY));
        }
        if (stack.is(CozyBlocks.TREE_STAR_ITEM.get())) {
            lines.add(Component.translatable("tooltip.cozychristmas.tree_star").withStyle(ChatFormatting.GRAY));
        } else if (stack.getItem() instanceof BlockItem item && item.getBlock() instanceof OrnamentBlock) {
            lines.add(Component.translatable("tooltip.cozychristmas.ornament").withStyle(ChatFormatting.GRAY));
        }
        if (stack.is(CozyItems.KRAMPUS_HORNS.get())) {
            lines.add(Component.translatable("tooltip.cozychristmas.krampus_horns").withStyle(ChatFormatting.GRAY));
        } else if (stack.is(CozyItems.KRAMPUS_CHAIN.get())) {
            lines.add(Component.translatable("tooltip.cozychristmas.krampus_chain").withStyle(ChatFormatting.GRAY));
        }
        if (stack.is(CozyItems.ICE_SNOWBALL.get())) {
            lines.add(Component.translatable("tooltip.cozychristmas.ice_snowball").withStyle(ChatFormatting.GRAY));
        } else if (stack.is(CozyItems.HOT_COCOA.get())) {
            lines.add(Component.translatable("tooltip.cozychristmas.hot_cocoa").withStyle(ChatFormatting.GRAY));
        } else if (stack.is(CozyItems.CHESTNUTS.get())) {
            lines.add(Component.translatable("tooltip.cozychristmas.chestnuts").withStyle(ChatFormatting.GRAY));
        }
    }

    /** Who it's from, never what's inside: a prank and a gift read the same. */
    private static void appendPresent(PresentContents present, List<Component> lines) {
        lines.add(present.from()
                .map(giver -> Component.translatable("tooltip.cozychristmas.present.from", giver.name()))
                .orElseGet(() -> Component.translatable("tooltip.cozychristmas.present.mystery"))
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }

    /** "Toasty set (2/4)" for the local player, then the bonuses, lit while the set is complete. */
    private static void appendToastySet(List<Component> lines) {
        Player player = Minecraft.getInstance().player;
        int worn = player == null ? 0 : ToastySet.piecesWorn(CosmeticsManager.worn(player));
        boolean complete = worn == ToastySet.SLOTS.size();
        lines.add(Component.translatable("tooltip.cozychristmas.toasty_set", worn, ToastySet.SLOTS.size())
                .withStyle(complete ? ChatFormatting.GOLD : ChatFormatting.GRAY));
        ChatFormatting bonus = complete ? ChatFormatting.BLUE : ChatFormatting.DARK_GRAY;
        lines.add(Component.translatable("tooltip.cozychristmas.toasty_set.warm").withStyle(bonus));
        lines.add(Component.translatable("tooltip.cozychristmas.toasty_set.snow_speed").withStyle(bonus));
    }
}
