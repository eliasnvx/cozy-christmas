package dev.eliasnvx.cozychristmas.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.krampus.SackBlock;
import dev.eliasnvx.cozychristmas.present.PresentBlock;
import dev.eliasnvx.cozychristmas.present.PresentBlockEntity;
import dev.eliasnvx.cozychristmas.present.PresentContents;
import dev.eliasnvx.cozychristmas.present.PresentItem;
import dev.eliasnvx.cozychristmas.present.WrappingTableBlock;
import dev.eliasnvx.cozychristmas.stocking.FireplaceBlock;
import dev.eliasnvx.cozychristmas.stocking.StockingBlock;
import dev.eliasnvx.cozychristmas.stocking.StockingBlockEntity;
import dev.eliasnvx.cozychristmas.tree.OrnamentBlock;
import dev.eliasnvx.cozychristmas.tree.OrnamentColor;
import dev.eliasnvx.cozychristmas.tree.TreeStarBlock;
import dev.eliasnvx.cozychristmas.tree.TreeStarBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/** Blocks, their items and block entities. Registered before {@link CozyItems}: block items look their block up. */
public final class CozyBlocks {

    public static final DeferredRegister<Block> REGISTER = DeferredRegister.create(CozyChristmas.MOD_ID, Registries.BLOCK);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(CozyChristmas.MOD_ID, Registries.BLOCK_ENTITY_TYPE);

    /** Presents stack while they're alike (unopened surprises of one design); wrapped ones never are. */
    private static final int PRESENT_STACK = 16;

    public static final RegistrySupplier<Block> PRESENT = block("present", PresentBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(0.5F).sound(SoundType.WOOL).noOcclusion()
                    .ignitedByLava());
    public static final RegistrySupplier<Item> PRESENT_ITEM = CozyItems.register("present", props -> new PresentItem(PRESENT.get(),
            props.useBlockDescriptionPrefix().stacksTo(PRESENT_STACK)
                    .component(CozyComponents.PRESENT_CONTENTS.get(), PresentContents.SURPRISE)));
    public static final RegistrySupplier<BlockEntityType<PresentBlockEntity>> PRESENT_ENTITY = BLOCK_ENTITIES.register("present",
            () -> new BlockEntityType<>(PresentBlockEntity::new, Set.of(PRESENT.get())));

    public static final RegistrySupplier<Block> WRAPPING_TABLE = block("wrapping_table", WrappingTableBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.5F).sound(SoundType.WOOD).ignitedByLava());
    public static final RegistrySupplier<Item> WRAPPING_TABLE_ITEM = CozyItems.register("wrapping_table",
            props -> new BlockItem(WRAPPING_TABLE.get(), props.useBlockDescriptionPrefix()));

    public static final RegistrySupplier<Block> FIREPLACE = block("fireplace", FireplaceBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(2.0F, 6.0F).requiresCorrectToolForDrops()
                    .sound(SoundType.STONE).lightLevel(state -> state.getValue(FireplaceBlock.LIT) ? FireplaceBlock.LIGHT : 0)
                    .noOcclusion());
    public static final RegistrySupplier<Item> FIREPLACE_ITEM = CozyItems.register("fireplace",
            props -> new BlockItem(FIREPLACE.get(), props.useBlockDescriptionPrefix()));

    /** Hangs on a wall; doesn't stack (each one is somebody's). */
    public static final RegistrySupplier<Block> STOCKING = block("stocking", StockingBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(0.2F).sound(SoundType.WOOL).noCollision()
                    .noOcclusion().ignitedByLava());
    public static final RegistrySupplier<Item> STOCKING_ITEM = CozyItems.register("stocking",
            props -> new BlockItem(STOCKING.get(), props.useBlockDescriptionPrefix().stacksTo(16)));
    public static final RegistrySupplier<BlockEntityType<StockingBlockEntity>> STOCKING_ENTITY = BLOCK_ENTITIES.register("stocking",
            () -> new BlockEntityType<>(StockingBlockEntity::new, Set.of(STOCKING.get())));

    /** Goes on top of a Christmas tree: a present under it every morning in the season. */
    public static final RegistrySupplier<Block> TREE_STAR = block("tree_star", TreeStarBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).strength(0.3F).sound(SoundType.METAL).noCollision()
                    .noOcclusion().lightLevel(state -> TreeStarBlock.LIGHT).pushReaction(PushReaction.POPPED));
    public static final RegistrySupplier<Item> TREE_STAR_ITEM = CozyItems.register("tree_star",
            props -> new BlockItem(TREE_STAR.get(), props.useBlockDescriptionPrefix().stacksTo(16)));
    public static final RegistrySupplier<BlockEntityType<TreeStarBlockEntity>> TREE_STAR_ENTITY = BLOCK_ENTITIES.register("tree_star",
            () -> new BlockEntityType<>(TreeStarBlockEntity::new, Set.of(TREE_STAR.get())));

    /** Glass ornaments, one block (and item) per color. */
    public static final Map<OrnamentColor, RegistrySupplier<Block>> ORNAMENTS;
    public static final Map<OrnamentColor, RegistrySupplier<Item>> ORNAMENT_ITEMS;

    static {
        Map<OrnamentColor, RegistrySupplier<Block>> blocks = new EnumMap<>(OrnamentColor.class);
        Map<OrnamentColor, RegistrySupplier<Item>> items = new EnumMap<>(OrnamentColor.class);
        for (OrnamentColor color : OrnamentColor.values()) {
            RegistrySupplier<Block> block = block(color.blockName(), props -> new OrnamentBlock(color, props),
                    BlockBehaviour.Properties.of().mapColor(color.mapColor()).strength(0.1F).sound(SoundType.GLASS)
                            .noCollision().noOcclusion().pushReaction(PushReaction.POPPED));
            blocks.put(color, block);
            items.put(color, CozyItems.register(color.blockName(), props -> new BlockItem(block.get(), props.useBlockDescriptionPrefix())));
        }
        ORNAMENTS = Collections.unmodifiableMap(blocks);
        ORNAMENT_ITEMS = Collections.unmodifiableMap(items);
    }

    private CozyBlocks() {
    }

    /** Krampus's sacks, piled round his lair. */
    public static final RegistrySupplier<Block> SACK = block("sack", SackBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).strength(0.6F).sound(SoundType.WOOL).noOcclusion()
                    .ignitedByLava());
    public static final RegistrySupplier<Item> SACK_ITEM = CozyItems.register("sack",
            props -> new BlockItem(SACK.get(), props.useBlockDescriptionPrefix()));

    private static RegistrySupplier<Block> block(String name, Function<BlockBehaviour.Properties, Block> factory,
                                                 BlockBehaviour.Properties props) {
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, CozyChristmas.id(name));
        return REGISTER.register(name, () -> factory.apply(props.setId(key)));
    }
}
