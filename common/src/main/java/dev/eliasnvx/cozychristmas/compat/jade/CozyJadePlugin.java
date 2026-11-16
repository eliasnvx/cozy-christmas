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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

import java.util.Optional;
import java.util.function.Function;

/**
 * Jade (SPEC compat): whose stocking it is, what it holds and whether it hangs over a fireplace; whose tree a star
 * crowns and how the tree is doing; who wrapped a present. Loaded only by Jade: the {@code jade} entrypoint on Fabric,
 * the annotation on Forge. Each block has a pair: a server data provider that writes its {@link LookInfo} into Jade's
 * server data tag (Jade 11 on 1.20.1 has no stream providers) under its namespaced uid, and the client lines.
 */
@WailaPlugin(CozyChristmas.MOD_ID)
public final class CozyJadePlugin implements IWailaPlugin {

    private static final ResourceLocation STOCKING = CozyChristmas.id("stocking");
    private static final ResourceLocation TREE_STAR = CozyChristmas.id("tree_star");
    private static final ResourceLocation PRESENT = CozyChristmas.id("present");

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

    /** What the server sent for this provider, if it did (it has Cozy Christmas and Jade). */
    private static <T> Optional<T> read(BlockAccessor accessor, ResourceLocation uid, Function<CompoundTag, T> reader) {
        CompoundTag data = accessor.getServerData();
        String key = uid.toString();
        return data.contains(key, Tag.TAG_COMPOUND) ? Optional.of(reader.apply(data.getCompound(key))) : Optional.empty();
    }

    private static Component owner(String name) {
        return Component.translatable("jade.cozychristmas.owner", name).withStyle(ChatFormatting.GRAY);
    }

    enum StockingData implements IServerDataProvider<BlockAccessor> {
        INSTANCE;

        @Override
        public ResourceLocation getUid() {
            return STOCKING;
        }

        @Override
        public void appendServerData(CompoundTag data, BlockAccessor accessor) {
            if (accessor.getBlockEntity() instanceof StockingBlockEntity stocking) {
                data.put(STOCKING.toString(), LookInfo.Stocking.of(stocking).write());
            }
        }
    }

    enum StockingLines implements IBlockComponentProvider {
        INSTANCE;

        @Override
        public ResourceLocation getUid() {
            return STOCKING;
        }

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            read(accessor, STOCKING, LookInfo.Stocking::read)
                    .filter(info -> !info.owner().isEmpty())
                    .ifPresent(info -> tooltip.add(owner(info.owner())));
            StockingFill fill = accessor.getBlockState().getValue(StockingBlock.FILL);
            tooltip.add(Component.translatable("jade.cozychristmas.stocking." + fill.getSerializedName()));
            if (!StockingBlockEntity.hangsOverFireplace(accessor.getLevel(), accessor.getPosition(), accessor.getBlockState())) {
                tooltip.add(Component.translatable("message.cozychristmas.stocking.no_fireplace").withStyle(ChatFormatting.RED));
            }
        }
    }

    enum TreeData implements IServerDataProvider<BlockAccessor> {
        INSTANCE;

        @Override
        public ResourceLocation getUid() {
            return TREE_STAR;
        }

        @Override
        public void appendServerData(CompoundTag data, BlockAccessor accessor) {
            if (accessor.getLevel() instanceof ServerLevel level && accessor.getBlockEntity() instanceof TreeStarBlockEntity star) {
                data.put(TREE_STAR.toString(), LookInfo.Tree.of(level, accessor.getPosition(), star).write());
            }
        }
    }

    enum TreeLines implements IBlockComponentProvider {
        INSTANCE;

        @Override
        public ResourceLocation getUid() {
            return TREE_STAR;
        }

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            read(accessor, TREE_STAR, LookInfo.Tree::read).ifPresent(info -> {
                if (!info.owner().isEmpty()) {
                    tooltip.add(owner(info.owner()));
                }
                tooltip.add(info.status());
            });
        }
    }

    enum PresentData implements IServerDataProvider<BlockAccessor> {
        INSTANCE;

        @Override
        public ResourceLocation getUid() {
            return PRESENT;
        }

        @Override
        public void appendServerData(CompoundTag data, BlockAccessor accessor) {
            if (accessor.getBlockEntity() instanceof PresentBlockEntity present) {
                data.put(PRESENT.toString(), LookInfo.Present.of(present).write());
            }
        }
    }

    enum PresentLines implements IBlockComponentProvider {
        INSTANCE;

        @Override
        public ResourceLocation getUid() {
            return PRESENT;
        }

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            read(accessor, PRESENT, LookInfo.Present::read).ifPresent(info -> tooltip.add(info.from()
                    .map(name -> Component.translatable("tooltip.cozychristmas.present.from", name))
                    .orElseGet(() -> Component.translatable("tooltip.cozychristmas.present.mystery"))
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)));
        }
    }
}
