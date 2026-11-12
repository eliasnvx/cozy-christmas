package dev.eliasnvx.cozychristmas.registry;

import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.cosmetic.Cosmetic;
import dev.eliasnvx.cozychristmas.cosmetic.CosmeticSlot;
import dev.eliasnvx.cozychristmas.present.PresentBlock;
import dev.eliasnvx.cozychristmas.present.PresentDesign;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.TypedEntityData;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class CozyItems {

    public static final DeferredRegister<Item> REGISTER = DeferredRegister.create(CozyChristmas.MOD_ID, Registries.ITEM);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(CozyChristmas.MOD_ID, Registries.CREATIVE_MODE_TAB);

    /** Undyed color of the knitwear; the item models' tint default and the renderer use the same one. */
    public static final int KNIT_RED = 0xB3262E;
    /** Undyed color of the elf hat (felt green, like the workshop elves'). */
    public static final int ELF_GREEN = 0x2F8A3C;
    /** Besides undyed red, the creative tab shows the knitwear in these dyes (any dye works in crafting). */
    private static final List<DyeColor> CREATIVE_DYES =
            List.of(DyeColor.GREEN, DyeColor.WHITE, DyeColor.LIGHT_BLUE, DyeColor.YELLOW, DyeColor.BLACK);

    private static final List<RegistrySupplier<Item>> SWEATERS = new ArrayList<>();
    private static final List<RegistrySupplier<Item>> DYEABLE = new ArrayList<>();

    // knitted sweaters: one item per pattern, drawn from their own colored textures
    public static final RegistrySupplier<Item> SNOWFLAKE_SWEATER = sweater("snowflake_sweater");
    public static final RegistrySupplier<Item> REINDEER_SWEATER = sweater("reindeer_sweater");
    public static final RegistrySupplier<Item> CAT_SWEATER = sweater("cat_sweater");
    public static final RegistrySupplier<Item> BEE_SWEATER = sweater("bee_sweater");
    public static final RegistrySupplier<Item> PRESENT_SWEATER = sweater("present_sweater");

    // dyed like leather armor (vanilla crafting_dye recipes); undyed they are KNIT_RED unless said otherwise
    public static final RegistrySupplier<Item> POMPOM_HAT = dyeable("pompom_hat", CosmeticSlot.HEAD);
    public static final RegistrySupplier<Item> SCARF = dyeable("scarf", CosmeticSlot.NECK);
    public static final RegistrySupplier<Item> MITTENS = dyeable("mittens", CosmeticSlot.HANDS);
    /** Fluffy muffs: on the sides of the head, or over the cat ears of Catgirl & Catboy Mod. */
    public static final RegistrySupplier<Item> EAR_WARMERS = dyeable("ear_warmers", CosmeticSlot.HEAD);
    /** Undyed it is {@link #ELF_GREEN}. */
    public static final RegistrySupplier<Item> ELF_HAT = dyeable("elf_hat", CosmeticSlot.HEAD);
    public static final RegistrySupplier<Item> ANTLER_HEADBAND = wearable("antler_headband", CosmeticSlot.HEAD);

    public static final RegistrySupplier<Item> SANTA_SPAWN_EGG = spawnEgg("santa_spawn_egg", CozyEntities.SANTA_KEY);
    public static final RegistrySupplier<Item> ELF_SPAWN_EGG = spawnEgg("elf_spawn_egg", CozyEntities.ELF_KEY);
    public static final RegistrySupplier<Item> REINDEER_SPAWN_EGG = spawnEgg("reindeer_spawn_egg", CozyEntities.REINDEER_KEY);

    public static final RegistrySupplier<CreativeModeTab> TAB = TABS.register("cozychristmas", () -> CreativeTabRegistry.create(builder -> builder
            .title(Component.translatable("itemGroup.cozychristmas"))
            .icon(() -> new ItemStack(POMPOM_HAT.get()))
            .displayItems((parameters, output) -> {
                output.accept(CozyBlocks.WRAPPING_TABLE_ITEM.get());
                for (PresentDesign design : PresentDesign.values()) {
                    output.accept(present(design));
                }
                for (RegistrySupplier<Item> item : SWEATERS) {
                    output.accept(item.get());
                }
                for (RegistrySupplier<Item> item : DYEABLE) {
                    output.accept(item.get());
                    for (DyeColor dye : CREATIVE_DYES) {
                        output.accept(dyed(item.get(), dye));
                    }
                }
                output.accept(ANTLER_HEADBAND.get());
                output.accept(SANTA_SPAWN_EGG.get());
                output.accept(ELF_SPAWN_EGG.get());
                output.accept(REINDEER_SPAWN_EGG.get());
            })));

    private CozyItems() {
    }

    public static List<RegistrySupplier<Item>> sweaters() {
        return List.copyOf(SWEATERS);
    }

    public static List<RegistrySupplier<Item>> dyeable() {
        return List.copyOf(DYEABLE);
    }

    /** The item as one dye in a crafting grid makes it. */
    public static ItemStack dyed(Item item, DyeColor dye) {
        ItemStack stack = new ItemStack(item);
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(dye.getTextureDiffuseColor()));
        return stack;
    }

    /** An unopened surprise present: karma decides what's inside. */
    public static ItemStack present(PresentDesign design) {
        ItemStack stack = new ItemStack(CozyBlocks.PRESENT_ITEM.get());
        stack.set(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY.with(PresentBlock.DESIGN, design));
        return stack;
    }

    private static RegistrySupplier<Item> spawnEgg(String name, ResourceKey<EntityType<?>> entity) {
        return register(name, props -> new SpawnEggItem(
                // resolved late: on NeoForge items may be built before entity types are registered
                props.delayedComponent(DataComponents.ENTITY_DATA, context -> TypedEntityData.of(
                        context.lookupOrThrow(Registries.ENTITY_TYPE).getOrThrow(entity).value(), new CompoundTag()))));
    }

    private static RegistrySupplier<Item> sweater(String name) {
        RegistrySupplier<Item> item = wearable(name, CosmeticSlot.BODY);
        SWEATERS.add(item);
        return item;
    }

    private static RegistrySupplier<Item> dyeable(String name, CosmeticSlot slot) {
        RegistrySupplier<Item> item = wearable(name, slot);
        DYEABLE.add(item);
        return item;
    }

    private static RegistrySupplier<Item> wearable(String name, CosmeticSlot slot) {
        return register(name, props -> new Item(props.stacksTo(1).component(CozyComponents.COSMETIC.get(), new Cosmetic(slot))));
    }

    public static RegistrySupplier<Item> register(String name, Function<Item.Properties, Item> factory) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, CozyChristmas.id(name));
        return REGISTER.register(name, () -> factory.apply(new Item.Properties().setId(key)));
    }
}
