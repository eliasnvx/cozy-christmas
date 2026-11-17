package dev.eliasnvx.cozychristmas.wood;

import dev.architectury.registry.fuel.FuelRegistry;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.mixin.BlockSetTypeInvoker;
import dev.eliasnvx.cozychristmas.mixin.WoodTypeInvoker;
import dev.eliasnvx.cozychristmas.registry.CozyBlocks;
import dev.eliasnvx.cozychristmas.registry.CozyEntities;
import dev.eliasnvx.cozychristmas.registry.CozyItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SignItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.DispenserBlock;
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
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * One wood on 1.21.1, with the vanilla set (no shelves in 1.21.1): logs, planks, stairs, slabs, fence and gate, door,
 * trapdoor, button, pressure plate, both signs, boat and chest boat, plus fruit-bearing leaves, sapling and pot.
 */
public final class WoodSet {

    private static final float GRAND_CHANCE = 0.15F;

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
    public final RegistrySupplier<EntityType<CozyBoat>> boat;
    public final RegistrySupplier<EntityType<CozyChestBoat>> chestBoat;

    private final List<RegistrySupplier<Item>> items = new ArrayList<>();

    WoodSet(String name, MapColor planksColor, MapColor barkColor, Supplier<Item> fruit) {
        this.name = name;
        this.planksColor = planksColor;
        this.barkColor = barkColor;
        String typeName = CozyChristmas.MOD_ID + "_" + name;
        this.setType = BlockSetTypeInvoker.cozychristmas$register(new BlockSetType(typeName));
        this.woodType = WoodTypeInvoker.cozychristmas$register(new WoodType(typeName, setType));

        log = block(name + "_log", () -> new RotatedPillarBlock(logProperties()));
        strippedLog = block("stripped_" + name + "_log", () -> new RotatedPillarBlock(logProperties()));
        wood = block(name + "_wood", () -> new RotatedPillarBlock(woodProperties()));
        strippedWood = block("stripped_" + name + "_wood", () -> new RotatedPillarBlock(woodProperties()));
        planks = block(name + "_planks", () -> new Block(planksProperties()));
        stairs = block(name + "_stairs", () -> new StairBlock(planks.get().defaultBlockState(), Properties.ofFullCopy(planks.get())));
        slab = block(name + "_slab", () -> new SlabBlock(planksProperties()));
        fence = block(name + "_fence", () -> new FenceBlock(planksProperties()));
        fenceGate = block(name + "_fence_gate", () -> new FenceGateBlock(woodType, planksProperties().forceSolidOn()));
        door = block(name + "_door", () -> new DoorBlock(setType, Properties.of().mapColor(planksColor)
                .instrument(NoteBlockInstrument.BASS).strength(3.0F).noOcclusion().ignitedByLava()
                .pushReaction(PushReaction.DESTROY)));
        trapdoor = block(name + "_trapdoor", () -> new TrapDoorBlock(setType, Properties.of().mapColor(planksColor)
                .instrument(NoteBlockInstrument.BASS).strength(3.0F).noOcclusion()
                .isValidSpawn((state, level, pos, type) -> false).ignitedByLava()));
        button = block(name + "_button", () -> new ButtonBlock(setType, 30, Properties.of().noCollission().strength(0.5F)
                .pushReaction(PushReaction.DESTROY)));
        pressurePlate = block(name + "_pressure_plate", () -> new PressurePlateBlock(setType, Properties.of()
                .mapColor(planksColor).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollission().strength(0.5F)
                .ignitedByLava().pushReaction(PushReaction.DESTROY)));

        sign = block(name + "_sign", () -> new StandingSignBlock(woodType, signProperties()));
        wallSign = block(name + "_wall_sign", () -> new WallSignBlock(woodType, signProperties().dropsLike(sign.get())));
        hangingSign = block(name + "_hanging_sign", () -> new CeilingHangingSignBlock(woodType, signProperties()));
        wallHangingSign = block(name + "_wall_hanging_sign", () -> new WallHangingSignBlock(woodType,
                signProperties().dropsLike(hangingSign.get())));

        leaves = block(name + "_leaves", () -> new FruitLeavesBlock(fruit, leavesProperties()));
        ResourceKey<ConfiguredFeature<?, ?>> treeKey = ResourceKey.create(Registries.CONFIGURED_FEATURE,
                CozyChristmas.id(name + "_tree"));
        ResourceKey<ConfiguredFeature<?, ?>> grandKey = ResourceKey.create(Registries.CONFIGURED_FEATURE,
                CozyChristmas.id(name + "_tree_grand"));
        TreeGrower grower = new TreeGrower(typeName, GRAND_CHANCE, Optional.empty(), Optional.empty(),
                Optional.of(treeKey), Optional.of(grandKey), Optional.empty(), Optional.empty());
        sapling = block(name + "_sapling", () -> new SaplingBlock(grower, Properties.of().mapColor(MapColor.PLANT)
                .noCollission().randomTicks().instabreak().sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY)));
        pottedSapling = block("potted_" + name + "_sapling", () -> new FlowerPotBlock(sapling.get(),
                Properties.of().instabreak().noOcclusion().pushReaction(PushReaction.DESTROY)));

        blockItem(log, 300);
        blockItem(strippedLog, 300);
        blockItem(wood, 300);
        blockItem(strippedWood, 300);
        blockItem(planks, 300);
        blockItem(stairs, 300);
        blockItem(slab, 150);
        blockItem(fence, 300);
        blockItem(fenceGate, 300);
        item(name + "_door", props -> new DoubleHighBlockItem(door.get(), props), 200);
        blockItem(trapdoor, 300);
        blockItem(button, 100);
        blockItem(pressurePlate, 300);
        item(name + "_sign", props -> new SignItem(props.stacksTo(16), sign.get(), wallSign.get()), 200);
        item(name + "_hanging_sign", props -> new HangingSignItem(hangingSign.get(), wallHangingSign.get(), props.stacksTo(16)), 800);

        boat = boatType(name + "_boat", CozyBoat::new);
        chestBoat = boatType(name + "_chest_boat", CozyChestBoat::new);
        var boatItem = item(name + "_boat", props -> new CozyBoatItem(boat, props.stacksTo(1)), 1200);
        var chestBoatItem = item(name + "_chest_boat", props -> new CozyBoatItem(chestBoat, props.stacksTo(1)), 1200);
        boatItem.listen(i -> DispenserBlock.registerBehavior(i, dispenseBoat(boat)));
        chestBoatItem.listen(i -> DispenserBlock.registerBehavior(i, dispenseBoat(chestBoat)));

        var leavesItem = item(name + "_leaves", props -> new BlockItem(leaves.get(), props), 0);
        var saplingItem = item(name + "_sapling", props -> new BlockItem(sapling.get(), props), 100);
        leavesItem.listen(i -> ComposterBlock.COMPOSTABLES.put(i, 0.3F));
        saplingItem.listen(i -> ComposterBlock.COMPOSTABLES.put(i, 0.3F));
    }

    public List<RegistrySupplier<Item>> items() {
        return List.copyOf(items);
    }

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
        return Properties.of().mapColor(planksColor).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollission()
                .strength(1.0F).ignitedByLava();
    }

    private static Properties leavesProperties() {
        return Properties.of().mapColor(MapColor.PLANT).strength(0.2F).randomTicks().sound(SoundType.GRASS)
                .noOcclusion().isValidSpawn((state, level, pos, type) -> false).isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false).ignitedByLava().pushReaction(PushReaction.DESTROY)
                .isRedstoneConductor((state, level, pos) -> false);
    }

    private static RegistrySupplier<Block> block(String name, Supplier<Block> factory) {
        return CozyBlocks.REGISTER.register(name, factory);
    }

    private static <T extends Boat> RegistrySupplier<EntityType<T>> boatType(String name, EntityType.EntityFactory<T> factory) {
        return CozyEntities.REGISTER.register(name, () -> EntityType.Builder.<T>of(factory, MobCategory.MISC)
                .sized(1.375F, 0.5625F).eyeHeight(0.5625F).clientTrackingRange(10).build(name));
    }

    private void blockItem(RegistrySupplier<Block> block, int burnTicks) {
        item(block.getId().getPath(), props -> new BlockItem(block.get(), props), burnTicks);
    }

    private RegistrySupplier<Item> item(String name, Function<Item.Properties, Item> factory, int burnTicks) {
        RegistrySupplier<Item> registered = CozyItems.register(name, factory);
        items.add(registered);
        if (burnTicks > 0) {
            registered.listen(i -> FuelRegistry.register(burnTicks, i));
        }
        return registered;
    }

    private static DefaultDispenseItemBehavior dispenseBoat(Supplier<? extends EntityType<? extends Boat>> type) {
        return new DefaultDispenseItemBehavior() {
            @Override
            protected ItemStack execute(BlockSource source, ItemStack stack) {
                Direction dir = source.state().getValue(DispenserBlock.FACING);
                ServerLevel level = source.level();
                Vec3 center = source.center();
                double yOffset = level.getFluidState(source.pos().relative(dir)).is(FluidTags.WATER) ? 1.0 : 0.0;
                Boat boat = type.get().create(level);
                if (boat != null) {
                    boat.setPos(center.x + dir.getStepX() * 1.125, center.y + dir.getStepY() * 1.125 + yOffset,
                            center.z + dir.getStepZ() * 1.125);
                    boat.setYRot(dir.toYRot());
                    level.addFreshEntity(boat);
                    stack.shrink(1);
                }
                return stack;
            }
        };
    }
}
