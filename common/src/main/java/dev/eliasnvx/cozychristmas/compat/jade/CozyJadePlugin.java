package dev.eliasnvx.cozychristmas.compat.jade;

import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.present.PresentBlock;
import dev.eliasnvx.cozychristmas.present.PresentBlockEntity;
import dev.eliasnvx.cozychristmas.stocking.StockingBlock;
import dev.eliasnvx.cozychristmas.stocking.StockingBlockEntity;
import dev.eliasnvx.cozychristmas.stocking.StockingFill;
import dev.eliasnvx.cozychristmas.tree.TreeStarBlock;
import dev.eliasnvx.cozychristmas.tree.TreeStarBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.StreamServerDataProvider;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

/**
 * Jade (SPEC compat): whose stocking it is, what it holds and whether it hangs over a fireplace; whose tree a star
 * crowns and how the tree is doing; who wrapped a present. Loaded only by Jade: the {@code jade} entrypoint on Fabric,
 * the annotation on NeoForge. Jade 26.x rejects a provider that is both a data and a component provider, so each
 * block has a pair.
 */
@WailaPlugin(CozyChristmas.MOD_ID)
public final class CozyJadePlugin implements IWailaPlugin {

    private static final Identifier STOCKING = CozyChristmas.id("stocking");
    private static final Identifier TREE_STAR = CozyChristmas.id("tree_star");
    private static final Identifier PRESENT = CozyChristmas.id("present");

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(StockingData.INSTANCE, StockingBlockEntity.class);
        registration.registerBlockDataProvider(TreeData.INSTANCE, TreeStarBlockEntity.class);
        registration.registerBlockDataProvider(PresentData.INSTANCE, PresentBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(StockingLines.INSTANCE, StockingBlock.class);
        registration.registerBlockComponent(TreeLines.INSTANCE, TreeStarBlock.class);
        registration.registerBlockComponent(PresentLines.INSTANCE, PresentBlock.class);
    }

    private static Component owner(String name) {
        return Component.translatable("jade.cozychristmas.owner", name).withStyle(ChatFormatting.GRAY);
    }

    enum StockingData implements StreamServerDataProvider<BlockAccessor, LookInfo.Stocking> {
        INSTANCE;

        @Override
        public Identifier getUid() {
            return STOCKING;
        }

        @Override
        public LookInfo.Stocking streamData(BlockAccessor accessor) {
            return LookInfo.Stocking.of((StockingBlockEntity) accessor.getBlockEntity());
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, LookInfo.Stocking> streamCodec() {
            return LookInfo.Stocking.STREAM_CODEC.cast();
        }
    }

    enum StockingLines implements IBlockComponentProvider {
        INSTANCE;

        @Override
        public Identifier getUid() {
            return STOCKING;
        }

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            StockingData.INSTANCE.decodeFromData(accessor)
                    .filter(info -> !info.owner().isEmpty())
                    .ifPresent(info -> tooltip.add(owner(info.owner())));
            StockingFill fill = accessor.getBlockState().getValue(StockingBlock.FILL);
            tooltip.add(Component.translatable("jade.cozychristmas.stocking." + fill.getSerializedName()));
            if (!StockingBlockEntity.hangsOverFireplace(accessor.getLevel(), accessor.getPosition(), accessor.getBlockState())) {
                tooltip.add(Component.translatable("message.cozychristmas.stocking.no_fireplace").withStyle(ChatFormatting.RED));
            }
        }
    }

    enum TreeData implements StreamServerDataProvider<BlockAccessor, LookInfo.Tree> {
        INSTANCE;

        @Override
        public Identifier getUid() {
            return TREE_STAR;
        }

        @Override
        public LookInfo.Tree streamData(BlockAccessor accessor) {
            return LookInfo.Tree.of((ServerLevel) accessor.getLevel(), accessor.getPosition(),
                    (TreeStarBlockEntity) accessor.getBlockEntity());
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, LookInfo.Tree> streamCodec() {
            return LookInfo.Tree.STREAM_CODEC;
        }
    }

    enum TreeLines implements IBlockComponentProvider {
        INSTANCE;

        @Override
        public Identifier getUid() {
            return TREE_STAR;
        }

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            TreeData.INSTANCE.decodeFromData(accessor).ifPresent(info -> {
                if (!info.owner().isEmpty()) {
                    tooltip.add(owner(info.owner()));
                }
                tooltip.add(info.status());
            });
        }
    }

    enum PresentData implements StreamServerDataProvider<BlockAccessor, LookInfo.Present> {
        INSTANCE;

        @Override
        public Identifier getUid() {
            return PRESENT;
        }

        @Override
        public LookInfo.Present streamData(BlockAccessor accessor) {
            return LookInfo.Present.of((PresentBlockEntity) accessor.getBlockEntity());
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, LookInfo.Present> streamCodec() {
            return LookInfo.Present.STREAM_CODEC.cast();
        }
    }

    enum PresentLines implements IBlockComponentProvider {
        INSTANCE;

        @Override
        public Identifier getUid() {
            return PRESENT;
        }

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            PresentData.INSTANCE.decodeFromData(accessor).ifPresent(info -> tooltip.add(info.from()
                    .map(name -> Component.translatable("tooltip.cozychristmas.present.from", name))
                    .orElseGet(() -> Component.translatable("tooltip.cozychristmas.present.mystery"))
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)));
        }
    }
}
