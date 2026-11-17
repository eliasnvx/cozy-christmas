package dev.eliasnvx.cozychristmas.wood;

import dev.architectury.registry.registries.RegistrySupplier;
import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.mixin.BlockSetTypeInvoker;
import dev.eliasnvx.cozychristmas.mixin.WoodTypeInvoker;
import dev.eliasnvx.cozychristmas.registry.CozyBlocks;
import dev.eliasnvx.cozychristmas.registry.CozyItems;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * One wood, with the whole set vanilla woods have: logs, planks, stairs, slabs, fence and gate, door, trapdoor, button,
 * pressure plate, both signs, plus leaves that bear fruit, a sapling and the potted sapling. {@code name} is the wood
 * ({@code tangerine}); block and item ids are {@code <name>_log} and so on, like vanilla's.
 */
public final class WoodSet {

    public final String name;
    public final WoodType woodType;
    public final BlockSetType setType;
    public final MapColor planksColor;
    public final MapColor barkColor;

    public final RegistrySupplier<Block> log;
    public final RegistrySupplier<Block> strippedLog;
    public final RegistrySupplier<Block> wood;
    public final RegistrySupplier<Block> strippedWood;
    public final RegistrySupplier<Block> planks;
    public final RegistrySupplier<Block> stairs;
    public final RegistrySupplier<Block> slab;
    public final RegistrySupplier<Block> fence;
    public final RegistrySupplier<Block> fenceGate;
    public final RegistrySupplier<Block> door;
    public final RegistrySupplier<Block> trapdoor;
    public final RegistrySupplier<Block> button;
    public final RegistrySupplier<Block> pressurePlate;
    public final RegistrySupplier<Block> sign;
    public final RegistrySupplier<Block> wallSign;
    public final RegistrySupplier<Block> hangingSign;
    public final RegistrySupplier<Block> wallHangingSign;
    public final RegistrySupplier<Block> leaves;
    public final RegistrySupplier<Block> sapling;
    public final RegistrySupplier<Block> pottedSapling;

    /** Everything that has an item, in the order of the creative tab. */
    private final List<RegistrySupplier<Item>> items = new ArrayList<>();

    WoodSet(String name, MapColor planksColor, MapColor barkColor, Supplier<Item> fruit) {
        this.name = name;
        this.planksColor = planksColor;
        this.barkColor = barkColor;
        String typeName = CozyChristmas.MOD_ID + "_" + name; // sign editors look the name up as a path
        this.setType = BlockSetTypeInvoker.cozychristmas$register(new BlockSetType(typeName));
        this.woodType = WoodTypeInvoker.cozychristmas$register(new WoodType(typeName, setType));

        log = block(name + "_log", props -> new RotatedPillarBlock(props), () -> logProperties());
        strippedLog = block("stripped_" + name + "_log", props -> new RotatedPillarBlock(props), () -> logProperties());
        wood = block(name + "_wood", props -> new RotatedPillarBlock(props), () -> woodProperties());
        strippedWood = block("stripped_" + name + "_wood", props -> new RotatedPillarBlock(props), () -> woodProperties());
        planks = block(name + "_planks", Block::new, () -> planksProperties());
        stairs = block(name + "_stairs", props -> new StairBlock(planks.get().defaultBlockState(), props),
                () -> Properties.ofFullCopy(planks.get()));
        slab = block(name + "_slab", SlabBlock::new, () -> planksProperties());
        fence = block(name + "_fence", FenceBlock::new, () -> planksProperties());
        fenceGate = block(name + "_fence_gate", props -> new FenceGateBlock(woodType, props),
                () -> planksProperties().forceSolidOn());
        door = block(name + "_door", props -> new DoorBlock(setType, props) { },
                () -> Properties.of().mapColor(planksColor).instrument(NoteBlockInstrument.BASS).strength(3.0F)
                        .noOcclusion().ignitedByLava().pushReaction(PushReaction.POPPED));
        trapdoor = block(name + "_trapdoor", props -> new TrapDoorBlock(setType, props) { },
                () -> Properties.of().mapColor(planksColor).instrument(NoteBlockInstrument.BASS).strength(3.0F)
                        .noOcclusion().isValidSpawn((state, level, pos, type) -> false).ignitedByLava());
        button = block(name + "_button", props -> new ButtonBlock(setType, 30, props) { },
                () -> Properties.of().noCollision().strength(0.5F).pushReaction(PushReaction.POPPED));
        pressurePlate = block(name + "_pressure_plate", props -> new PressurePlateBlock(setType, props) { },
                () -> Properties.of().mapColor(planksColor).forceSolidOn().instrument(NoteBlockInstrument.BASS)
                        .noCollision().strength(0.5F).ignitedByLava().pushReaction(PushReaction.POPPED));

        ResourceKey<net.minecraft.world.level.storage.loot.LootTable> signLoot =
                ResourceKey.create(Registries.LOOT_TABLE, CozyChristmas.id("blocks/" + name + "_sign"));
        ResourceKey<net.minecraft.world.level.storage.loot.LootTable> hangingLoot =
                ResourceKey.create(Registries.LOOT_TABLE, CozyChristmas.id("blocks/" + name + "_hanging_sign"));
        String signDescription = "block." + CozyChristmas.MOD_ID + "." + name + "_sign";
        String hangingDescription = "block." + CozyChristmas.MOD_ID + "." + name + "_hanging_sign";
        sign = block(name + "_sign", props -> new StandingSignBlock(woodType, props), () -> signProperties());
        wallSign = block(name + "_wall_sign", props -> new WallSignBlock(woodType, props),
                () -> signProperties().overrideLootTable(Optional.of(signLoot)).overrideDescription(signDescription));
        hangingSign = block(name + "_hanging_sign", props -> new CeilingHangingSignBlock(woodType, props),
                () -> signProperties());
        wallHangingSign = block(name + "_wall_hanging_sign", props -> new WallHangingSignBlock(woodType, props),
                () -> signProperties().overrideLootTable(Optional.of(hangingLoot)).overrideDescription(hangingDescription));

        leaves = block(name + "_leaves", props -> new FruitLeavesBlock(fruit, props), () -> leavesProperties());
        ResourceKey<Feature> treeKey =
                ResourceKey.create(Registries.FEATURE, CozyChristmas.id(name + "_tree"));
        WeightedList<ResourceKey<Feature>> trees = WeightedList.<ResourceKey<Feature>>of(new Weighted<>(treeKey, 1));
        WeightedList<ResourceKey<Feature>> none = WeightedList.<ResourceKey<Feature>>of();
        TreeGrower grower = new TreeGrower(typeName, trees, none, none, treeKey);
        sapling = block(name + "_sapling", props -> new SaplingBlock(grower, props) { },
                () -> Properties.of().mapColor(MapColor.PLANT).noCollision().randomTicks().instabreak()
                        .sound(SoundType.GRASS).pushReaction(PushReaction.POPPED));
        pottedSapling = block("potted_" + name + "_sapling", props -> new FlowerPotBlock(sapling.get(), props),
                () -> Properties.of().instabreak().noOcclusion().pushReaction(PushReaction.POPPED));

        blockItem(log, ContextIntProviders.COOKING_TIME_WOOD_BLOCKS);
        blockItem(strippedLog, ContextIntProviders.COOKING_TIME_WOOD_BLOCKS);
        blockItem(this.wood, ContextIntProviders.COOKING_TIME_WOOD_BLOCKS);
        blockItem(strippedWood, ContextIntProviders.COOKING_TIME_WOOD_BLOCKS);
        blockItem(planks, ContextIntProviders.COOKING_TIME_WOOD_BLOCKS);
        blockItem(stairs, ContextIntProviders.COOKING_TIME_WOOD_BLOCKS);
        blockItem(slab, ContextIntProviders.COOKING_TIME_WOOD_SLABS);
        blockItem(fence, ContextIntProviders.COOKING_TIME_WOOD_BLOCKS);
        blockItem(fenceGate, ContextIntProviders.COOKING_TIME_WOOD_BLOCKS);
        item(name + "_door", props -> new DoubleHighBlockItem(door.get(), props.useBlockDescriptionPrefix()
                .cookingFuel(ContextIntProviders.COOKING_TIME_WOOD_ITEMS_LARGE)));
        blockItem(trapdoor, ContextIntProviders.COOKING_TIME_WOOD_BLOCKS);
        blockItem(button, ContextIntProviders.COOKING_TIME_WOOD_ITEMS_SMALL);
        blockItem(pressurePlate, ContextIntProviders.COOKING_TIME_WOOD_BLOCKS);
        item(name + "_sign", props -> new StandingAndWallBlockItem(sign.get(), wallSign.get(), Direction.DOWN,
                props.useBlockDescriptionPrefix().cookingFuel(ContextIntProviders.COOKING_TIME_WOOD_ITEMS_LARGE)
                        .stacksTo(16).signText()));
        item(name + "_hanging_sign", props -> new HangingSignItem(hangingSign.get(), wallHangingSign.get(),
                props.useBlockDescriptionPrefix().cookingFuel(ContextIntProviders.COOKING_TIME_HANGING_SIGNS)
                        .stacksTo(16).signText()));
        item(name + "_leaves", props -> new BlockItem(leaves.get(), props.useBlockDescriptionPrefix()
                .compostable(ContextIntProviders.COMPOSTABLE_LOW)));
        item(name + "_sapling", props -> new BlockItem(sapling.get(), props.useBlockDescriptionPrefix()
                .compostable(ContextIntProviders.COMPOSTABLE_LOW)
                .cookingFuel(ContextIntProviders.COOKING_TIME_DRY_PLANTS)));
    }

    public List<RegistrySupplier<Item>> items() {
        return List.copyOf(items);
    }

    /** Every block of the set that is part of it (not the wall variants and the pot, which have no item). */
    public List<RegistrySupplier<Block>> all() {
        return List.of(log, strippedLog, wood, strippedWood, planks, stairs, slab, fence, fenceGate, door, trapdoor,
                button, pressurePlate, sign, wallSign, hangingSign, wallHangingSign, leaves, sapling, pottedSapling);
    }

    private Properties logProperties() {
        return Properties.of().mapColor(state -> state.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y
                        ? planksColor : barkColor)
                .instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava();
    }

    private Properties woodProperties() {
        return Properties.of().mapColor(barkColor).instrument(NoteBlockInstrument.BASS).strength(2.0F)
                .sound(SoundType.WOOD).ignitedByLava();
    }

    private Properties planksProperties() {
        return Properties.of().mapColor(planksColor).instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F)
                .sound(SoundType.WOOD).ignitedByLava();
    }

    private Properties signProperties() {
        return Properties.of().mapColor(planksColor).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollision()
                .strength(1.0F).ignitedByLava();
    }

    private static Properties leavesProperties() {
        return Properties.of().mapColor(MapColor.PLANT).strength(0.2F).randomTicks().sound(SoundType.GRASS)
                .noOcclusion().isValidSpawn((state, level, pos, type) -> false).isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos, box) -> false).ignitedByLava().pushReaction(PushReaction.POPPED)
                .isRedstoneConductor((state, level, pos) -> false);
    }

    /** Properties are built when the block is registered: some of them look other blocks of the set up. */
    private static RegistrySupplier<Block> block(String name, Function<BlockBehaviour.Properties, Block> factory,
                                                 Supplier<BlockBehaviour.Properties> properties) {
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, CozyChristmas.id(name));
        return CozyBlocks.REGISTER.register(name, () -> factory.apply(properties.get().setId(key)));
    }

    private void blockItem(RegistrySupplier<Block> block, ResourceKey<net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider> fuel) {
        String id = block.getId().getPath();
        item(id, props -> new BlockItem(block.get(), props.useBlockDescriptionPrefix().cookingFuel(fuel)));
    }

    private void item(String name, Function<Item.Properties, Item> factory) {
        items.add(CozyItems.register(name, factory));
    }
}
