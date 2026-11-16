package dev.eliasnvx.cozychristmas.client;

import dev.architectury.event.events.client.ClientPlayerEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.event.events.client.ClientTooltipEvent;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import dev.architectury.registry.client.particle.ParticleProviderRegistry;
import dev.architectury.registry.client.rendering.ColorHandlerRegistry;
import dev.architectury.registry.client.rendering.RenderTypeRegistry;
import dev.architectury.registry.item.ItemPropertiesRegistry;
import dev.architectury.registry.registries.RegistrySupplier;
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
import dev.eliasnvx.cozychristmas.client.workshop.ElfRenderer;
import dev.eliasnvx.cozychristmas.client.workshop.SantaRenderer;
import dev.eliasnvx.cozychristmas.cosmetic.CosmeticSlot;
import dev.eliasnvx.cozychristmas.cosmetic.CosmeticsManager;
import dev.eliasnvx.cozychristmas.cosmetic.ToastySet;
import dev.eliasnvx.cozychristmas.network.EarlySyncs;
import dev.eliasnvx.cozychristmas.present.PresentBlock;
import dev.eliasnvx.cozychristmas.present.PresentContents;
import dev.eliasnvx.cozychristmas.present.PresentDesign;
import dev.eliasnvx.cozychristmas.registry.CozyBlocks;
import dev.eliasnvx.cozychristmas.registry.CozyComponents;
import dev.eliasnvx.cozychristmas.registry.CozyEntities;
import dev.eliasnvx.cozychristmas.registry.CozyItems;
import dev.eliasnvx.cozychristmas.registry.CozyParticles;
import dev.eliasnvx.cozychristmas.registry.CozyTags;
import dev.eliasnvx.cozychristmas.tree.OrnamentBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Client entry point, called by each loader's client initializer after {@link CozyChristmas#init()}. The loaders add
 * {@code CosmeticLayer} to the player renderers and register the screens and block entity renderers themselves (their
 * events need the registered types).
 */
public final class CozyChristmasClient {

    /** The present item's model override predicate (models/item/present.json): the design's ordinal. */
    private static final ResourceLocation PRESENT_DESIGN = CozyChristmas.id("design");
    /** Item tints are ARGB; dye colors are RGB. */
    private static final int OPAQUE = 0xFF000000;

    private CozyChristmasClient() {
    }

    public static void init() {
        CosmeticShape.registerLayers();
        CosmeticLooks.registerDefaults();
        registerItemTints();
        registerBlockRenderTypes();
        CozyBlocks.PRESENT_ITEM.listen(item -> ItemPropertiesRegistry.register(item, PRESENT_DESIGN, new PresentDesignProperty()));
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
        ClientTickEvent.CLIENT_POST.register(minecraft -> {
            if (minecraft.player != null) {
                EarlySyncs.flush(minecraft.player);
            }
        });
        ClientPlayerEvent.CLIENT_PLAYER_QUIT.register(player -> EarlySyncs.clear());
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

    /**
     * 1.20.1 has no item model tints in data: the dyeable knitwear's grayscale icon layer ({@code layer0}) takes the dye
     * like leather armor ({@code display.color} NBT), the white trims ({@code layer1}) stay as drawn. Sweaters, antlers
     * and horns aren't tinted.
     */
    private static int dyeColor(ItemStack stack, int undyed) {
        return stack.getItem() instanceof DyeableLeatherItem dyeable && dyeable.hasCustomColor(stack)
                ? dyeable.getColor(stack) : undyed;
    }

    private static void registerItemTints() {
        for (RegistrySupplier<Item> item : CozyItems.dyeable()) {
            int undyed = item == CozyItems.ELF_HAT ? CozyItems.ELF_GREEN : CozyItems.KNIT_RED;
            ColorHandlerRegistry.registerItemColors((stack, tintIndex) -> tintIndex != 0 ? -1
                    : OPAQUE | dyeColor(stack, undyed), item);
        }
    }

    /**
     * 1.20.1 picks a block's render layer in code: blocks with see-through pixels (the fireplace's flames, stockings,
     * the tree star, ornaments) are cutout. Registered as soon as each block exists (Fabric has them by now, Forge
     * registers them later).
     */
    private static void registerBlockRenderTypes() {
        cutout(CozyBlocks.FIREPLACE);
        cutout(CozyBlocks.STOCKING);
        cutout(CozyBlocks.TREE_STAR);
        CozyBlocks.ORNAMENTS.values().forEach(CozyChristmasClient::cutout);
    }

    private static void cutout(RegistrySupplier<Block> block) {
        block.listen(registered -> RenderTypeRegistry.register(RenderType.cutout(), registered));
    }

    /**
     * The present's design for its item model overrides. Unclamped: the overrides compare the design's ordinal (1 to
     * 3), and vanilla clamps property values to 0..1 by default.
     */
    private static final class PresentDesignProperty implements ClampedItemPropertyFunction {

        @Override
        @SuppressWarnings("deprecation")
        public float call(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
            return unclampedCall(stack, level, entity, seed);
        }

        @Override
        public float unclampedCall(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
            // the block state the item places, in vanilla's BlockStateTag NBT (1.20.1 has no block state component)
            CompoundTag tag = stack.getTag();
            if (tag == null || !tag.contains(BlockItem.BLOCK_STATE_TAG)) {
                return 0.0F;
            }
            String name = tag.getCompound(BlockItem.BLOCK_STATE_TAG).getString(PresentBlock.DESIGN.getName());
            return PresentBlock.DESIGN.getValue(name).map(PresentDesign::ordinal).orElse(0);
        }
    }

    private static void appendTooltip(ItemStack stack, List<Component> lines, TooltipFlag flag) {
        CosmeticSlot slot = CosmeticsManager.slotOf(stack);
        if (slot != null) {
            lines.add(Component.translatable("tooltip.cozychristmas.slot", Component.translatable(slot.translationKey()))
                    .withStyle(ChatFormatting.GRAY));
        }
        if (stack.is(CozyTags.TOASTY_SET)) {
            appendToastySet(lines);
        }
        PresentContents present = CozyComponents.PRESENT_CONTENTS.get(stack);
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
