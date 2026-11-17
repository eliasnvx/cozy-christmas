package dev.eliasnvx.cozychristmas.wood;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.ChestBoat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Chest boat of one of our woods: drops its own chest boat item and picks as itself. */
public class CozyChestBoat extends ChestBoat {

    public CozyChestBoat(EntityType<? extends Boat> type, Level level) {
        super(type, level);
    }

    @Override
    public Item getDropItem() {
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(getType());
        return BuiltInRegistries.ITEM.get(id);
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(getDropItem());
    }
}
