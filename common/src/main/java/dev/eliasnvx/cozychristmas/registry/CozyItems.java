package dev.eliasnvx.cozychristmas.registry;

import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.cosmetic.Cosmetic;
import dev.eliasnvx.cozychristmas.cosmetic.CosmeticSlot;
import dev.eliasnvx.cozychristmas.food.TangerineItem;
import dev.eliasnvx.cozychristmas.present.PresentBlock;
import dev.eliasnvx.cozychristmas.krampus.KrampusChainItem;
import dev.eliasnvx.cozychristmas.present.PresentDesign;
import dev.eliasnvx.cozychristmas.tree.OrnamentColor;
import dev.eliasnvx.cozychristmas.snowman.IceSnowballItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.AttackRange;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;

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

    /** The chain hits like an iron sword plus one, slower, and reaches 5 blocks instead of 3. */
    private static final float CHAIN_DAMAGE = 4.0F;
    private static final float CHAIN_SPEED = -3.0F;
    private static final int CHAIN_DURABILITY = 500;
    private static final float CHAIN_REACH = 5.0F;
    private static final float CHAIN_CREATIVE_REACH = 6.5F;
    private static final float CHAIN_HITBOX_MARGIN = 0.125F;

    /** SPEC food values. Item definitions like vanilla's: the client builds the same items, so not in the config. */
    private static final int DRINK_STACK = 16;
    private static final int WARM_TICKS = 2400;
    private static final int MULLED_WINE_REGENERATION_TICKS = 200;

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
    public static final RegistrySupplier<Item> PRESENT_GOBLIN_SPAWN_EGG = spawnEgg("present_goblin_spawn_egg", CozyEntities.PRESENT_GOBLIN_KEY);
    public static final RegistrySupplier<Item> GINGERBREAD_MAN_SPAWN_EGG = spawnEgg("gingerbread_man_spawn_egg", CozyEntities.GINGERBREAD_MAN_KEY);
    public static final RegistrySupplier<Item> EVIL_SNOWMAN_SPAWN_EGG = spawnEgg("evil_snowman_spawn_egg", CozyEntities.EVIL_SNOWMAN_KEY);

    public static final RegistrySupplier<Item> KRAMPUS_SPAWN_EGG = spawnEgg("krampus_spawn_egg", CozyEntities.KRAMPUS_KEY);
    /** SPEC: Krampus's horns, a trophy only he drops. Worn on the head, no bonus. */
    public static final RegistrySupplier<Item> KRAMPUS_HORNS = register("krampus_horns", props -> new Item(props.stacksTo(1)
            .rarity(Rarity.EPIC).component(CozyComponents.COSMETIC.get(), new Cosmetic(CosmeticSlot.HEAD))));
    /** SPEC: Krampus's chain, his other drop: an iron weapon with a long reach that drags what it hits closer. */
    public static final RegistrySupplier<Item> KRAMPUS_CHAIN = register("krampus_chain", props -> new KrampusChainItem(props
            .sword(ToolMaterial.IRON, CHAIN_DAMAGE, CHAIN_SPEED).durability(CHAIN_DURABILITY).rarity(Rarity.RARE)
            .component(DataComponents.ATTACK_RANGE, new AttackRange(0.0F, CHAIN_REACH, 0.0F, CHAIN_CREATIVE_REACH,
                    CHAIN_HITBOX_MARGIN, 1.0F))));

    /** Evil snowmen drop them; thrown, they hurt, slow and chill. Stacks like snowballs. */
    public static final RegistrySupplier<Item> ICE_SNOWBALL = register("ice_snowball", props -> new IceSnowballItem(props.stacksTo(16)));

    /** SPEC: 4 food, lures elves ({@code #cozychristmas:elf_treats}). Gingerbread men crumble into them. */
    public static final RegistrySupplier<Item> GINGERBREAD_COOKIE = register("gingerbread_cookie", props -> new Item(props.food(
            new FoodProperties.Builder().nutrition(4).saturationModifier(0.3F).build())));

    /** Brick mug; hot cocoa comes in it and leaves it behind. */
    public static final RegistrySupplier<Item> MUG = register("mug", Item::new);
    /** SPEC: cocoa with marshmallows, 6 food, warms: thaws freezing and keeps it off for a while ({@code warm}). */
    public static final RegistrySupplier<Item> HOT_COCOA = register("hot_cocoa", props -> new Item(props.stacksTo(DRINK_STACK)
            .component(DataComponents.FOOD, new FoodProperties.Builder().nutrition(6).saturationModifier(0.5F).alwaysEdible().build())
            // resolved late: our effect may be registered after the items on some loaders
            .delayedComponent(DataComponents.CONSUMABLE, registries -> Consumables.defaultDrink()
                    .onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(
                            registries.lookupOrThrow(Registries.MOB_EFFECT).getOrThrow(CozyEffects.WARM_KEY), WARM_TICKS)))
                    .build())
            .usingConvertsTo(MUG.get())));
    /** SPEC: mulled wine, Regeneration I for 10 s. In a glass bottle, given back. */
    public static final RegistrySupplier<Item> MULLED_WINE = register("mulled_wine", props -> new Item(props.stacksTo(DRINK_STACK)
            .food(new FoodProperties.Builder().nutrition(2).saturationModifier(0.2F).alwaysEdible().build(), Consumables.defaultDrink()
                    .onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.REGENERATION,
                            MULLED_WINE_REGENERATION_TICKS))).build())
            .usingConvertsTo(Items.GLASS_BOTTLE)));
    /** Raw: fall from dark oak leaves ({@code food.chestnut_leaves}), roasted on a campfire. Not food yet. */
    public static final RegistrySupplier<Item> CHESTNUTS = register("chestnuts", Item::new);
    /** SPEC: 5 food, roasted on a campfire. */
    public static final RegistrySupplier<Item> ROASTED_CHESTNUTS = register("roasted_chestnuts", props -> new Item(props.food(
            new FoodProperties.Builder().nutrition(5).saturationModifier(0.6F).build())));
    /** SPEC: 3 food and an easter egg with no explanation ({@link TangerineItem}). */
    public static final RegistrySupplier<Item> TANGERINE = register("tangerine", props -> new TangerineItem(props.food(
            new FoodProperties.Builder().nutrition(3).saturationModifier(0.3F).build())));

    public static final RegistrySupplier<CreativeModeTab> TAB = TABS.register("cozychristmas", () -> CreativeTabRegistry.create(builder -> builder
            .title(Component.translatable("itemGroup.cozychristmas"))
            .icon(() -> new ItemStack(POMPOM_HAT.get()))
            .displayItems((parameters, output) -> {
                output.accept(CozyBlocks.WRAPPING_TABLE_ITEM.get());
                output.accept(CozyBlocks.FIREPLACE_ITEM.get());
                output.accept(CozyBlocks.STOCKING_ITEM.get());
                output.accept(CozyBlocks.TREE_STAR_ITEM.get());
                for (OrnamentColor color : OrnamentColor.values()) {
                    output.accept(CozyBlocks.ORNAMENT_ITEMS.get(color).get());
                }
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
                output.accept(PRESENT_GOBLIN_SPAWN_EGG.get());
                output.accept(GINGERBREAD_MAN_SPAWN_EGG.get());
                output.accept(GINGERBREAD_COOKIE.get());
                output.accept(MUG.get());
                output.accept(HOT_COCOA.get());
                output.accept(MULLED_WINE.get());
                output.accept(CHESTNUTS.get());
                output.accept(ROASTED_CHESTNUTS.get());
                output.accept(TANGERINE.get());
                output.accept(EVIL_SNOWMAN_SPAWN_EGG.get());
                output.accept(ICE_SNOWBALL.get());
                output.accept(KRAMPUS_SPAWN_EGG.get());
                output.accept(KRAMPUS_HORNS.get());
                output.accept(KRAMPUS_CHAIN.get());
                output.accept(CozyBlocks.SACK_ITEM.get());
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
