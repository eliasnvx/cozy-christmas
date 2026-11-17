package dev.eliasnvx.cozychristmas.registry;

import dev.architectury.core.item.ArchitecturySpawnEggItem;
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
import dev.eliasnvx.cozychristmas.wood.CozyWoods;
import dev.eliasnvx.cozychristmas.wood.WoodSet;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

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

    /**
     * The chain hits like an iron sword plus one, slower, and reaches 5 blocks instead of 3. 1.21.1 has no
     * {@code attack_range} component: the reach is an entity interaction range bonus while held (creative's own +2
     * stacks on it, so 7 there instead of 26.x's 6.5).
     */
    private static final int CHAIN_DAMAGE = 4;
    private static final float CHAIN_SPEED = -3.0F;
    private static final int CHAIN_DURABILITY = 500;
    private static final double CHAIN_REACH_BONUS = 2.0;
    /** A sword's tool rules, as vanilla's SwordItem gives them (cobwebs, sword-efficient blocks, two wear per block). */
    private static final float SWORD_COBWEB_SPEED = 15.0F;
    private static final float SWORD_EFFICIENT_SPEED = 1.5F;
    private static final float SWORD_DEFAULT_SPEED = 1.0F;
    private static final int SWORD_WEAR_PER_BLOCK = 2;
    private static final Tool SWORD_TOOL = new Tool(List.of(Tool.Rule.minesAndDrops(List.of(Blocks.COBWEB), SWORD_COBWEB_SPEED),
            Tool.Rule.overrideSpeed(BlockTags.SWORD_EFFICIENT, SWORD_EFFICIENT_SPEED)), SWORD_DEFAULT_SPEED, SWORD_WEAR_PER_BLOCK);
    /** Spawn egg textures show the mob itself, so the egg tint colors are white. */
    private static final int SPAWN_EGG_TINT = 0xFFFFFF;
    /** Dyeing in a crafting grid stores the color without alpha. */
    private static final int RGB_MASK = 0xFFFFFF;

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
    // SPEC 1.1: more patterns
    public static final RegistrySupplier<Item> GINGERBREAD_SWEATER = sweater("gingerbread_sweater");
    public static final RegistrySupplier<Item> CHRISTMAS_TREE_SWEATER = sweater("christmas_tree_sweater");
    public static final RegistrySupplier<Item> FOX_SWEATER = sweater("fox_sweater");
    public static final RegistrySupplier<Item> SNOWMAN_SWEATER = sweater("snowman_sweater");
    public static final RegistrySupplier<Item> CANDY_CANE_SWEATER = sweater("candy_cane_sweater");

    // dyed like leather armor (vanilla crafting_dye recipes); undyed they are KNIT_RED unless said otherwise
    public static final RegistrySupplier<Item> POMPOM_HAT = dyeable("pompom_hat", CosmeticSlot.HEAD);
    public static final RegistrySupplier<Item> SCARF = dyeable("scarf", CosmeticSlot.NECK);
    public static final RegistrySupplier<Item> MITTENS = dyeable("mittens", CosmeticSlot.HANDS);
    /** Fluffy muffs: on the sides of the head, or over the cat ears of Catgirl & Catboy Mod. */
    public static final RegistrySupplier<Item> EAR_WARMERS = dyeable("ear_warmers", CosmeticSlot.HEAD);
    /** Undyed it is {@link #ELF_GREEN}. */
    public static final RegistrySupplier<Item> ELF_HAT = dyeable("elf_hat", CosmeticSlot.HEAD);
    public static final RegistrySupplier<Item> ANTLER_HEADBAND = wearable("antler_headband", CosmeticSlot.HEAD);

    public static final RegistrySupplier<Item> SANTA_SPAWN_EGG = spawnEgg("santa_spawn_egg", CozyEntities.SANTA);
    public static final RegistrySupplier<Item> ELF_SPAWN_EGG = spawnEgg("elf_spawn_egg", CozyEntities.ELF);
    public static final RegistrySupplier<Item> REINDEER_SPAWN_EGG = spawnEgg("reindeer_spawn_egg", CozyEntities.REINDEER);
    public static final RegistrySupplier<Item> PRESENT_GOBLIN_SPAWN_EGG = spawnEgg("present_goblin_spawn_egg", CozyEntities.PRESENT_GOBLIN);
    public static final RegistrySupplier<Item> GINGERBREAD_MAN_SPAWN_EGG = spawnEgg("gingerbread_man_spawn_egg", CozyEntities.GINGERBREAD_MAN);
    public static final RegistrySupplier<Item> EVIL_SNOWMAN_SPAWN_EGG = spawnEgg("evil_snowman_spawn_egg", CozyEntities.EVIL_SNOWMAN);

    public static final RegistrySupplier<Item> KRAMPUS_SPAWN_EGG = spawnEgg("krampus_spawn_egg", CozyEntities.KRAMPUS);
    /** SPEC: Krampus's horns, a trophy only he drops. Worn on the head, no bonus. */
    public static final RegistrySupplier<Item> KRAMPUS_HORNS = register("krampus_horns", props -> new Item(props.stacksTo(1)
            .rarity(Rarity.EPIC).component(CozyComponents.COSMETIC.get(), new Cosmetic(CosmeticSlot.HEAD))));
    /** SPEC: Krampus's chain, his other drop: an iron weapon with a long reach that drags what it hits closer. */
    public static final RegistrySupplier<Item> KRAMPUS_CHAIN = register("krampus_chain", props -> new KrampusChainItem(props
            .durability(CHAIN_DURABILITY).rarity(Rarity.RARE).component(DataComponents.TOOL, SWORD_TOOL).attributes(SwordItem.createAttributes(Tiers.IRON, CHAIN_DAMAGE, CHAIN_SPEED)
                    .withModifierAdded(Attributes.ENTITY_INTERACTION_RANGE, new AttributeModifier(
                            CozyChristmas.id("krampus_chain_reach"), CHAIN_REACH_BONUS, AttributeModifier.Operation.ADD_VALUE),
                            EquipmentSlotGroup.MAINHAND))));

    /** Evil snowmen drop them; thrown, they hurt, slow and chill. Stacks like snowballs. */
    public static final RegistrySupplier<Item> ICE_SNOWBALL = register("ice_snowball", props -> new IceSnowballItem(props.stacksTo(16)));

    /** SPEC: 4 food, lures elves ({@code #cozychristmas:elf_treats}). Gingerbread men crumble into them. */
    public static final RegistrySupplier<Item> GINGERBREAD_COOKIE = register("gingerbread_cookie", props -> new Item(props.food(
            new FoodProperties.Builder().nutrition(4).saturationModifier(0.3F).build())));

    /** Brick mug; hot cocoa comes in it and leaves it behind. */
    public static final RegistrySupplier<Item> MUG = register("mug", Item::new);
    /** SPEC: cocoa with marshmallows, 6 food, warms: thaws freezing and keeps it off for a while ({@code warm}). */
    // effects are registered before items on both loaders, so the warm effect's holder is there when this is built
    public static final RegistrySupplier<Item> HOT_COCOA = register("hot_cocoa", props -> new Drink(props.stacksTo(DRINK_STACK)
            .food(new FoodProperties.Builder().nutrition(6).saturationModifier(0.5F).alwaysEdible()
                    .effect(new MobEffectInstance(CozyEffects.holder(CozyEffects.WARM), WARM_TICKS), 1.0F)
                    .usingConvertsTo(MUG.get()).build())));
    /** SPEC: mulled wine, Regeneration I for 10 s. In a glass bottle, given back. */
    public static final RegistrySupplier<Item> MULLED_WINE = register("mulled_wine", props -> new Drink(props.stacksTo(DRINK_STACK)
            .food(new FoodProperties.Builder().nutrition(2).saturationModifier(0.2F).alwaysEdible()
                    .effect(new MobEffectInstance(MobEffects.REGENERATION, MULLED_WINE_REGENERATION_TICKS), 1.0F)
                    .usingConvertsTo(Items.GLASS_BOTTLE).build())));
    /** Raw: fall from dark oak leaves ({@code food.chestnut_leaves}), roasted on a campfire. Not food yet. */
    public static final RegistrySupplier<Item> CHESTNUTS = register("chestnuts", Item::new);
    /** SPEC: 5 food, roasted on a campfire. */
    public static final RegistrySupplier<Item> ROASTED_CHESTNUTS = register("roasted_chestnuts", props -> new Item(props.food(
            new FoodProperties.Builder().nutrition(5).saturationModifier(0.6F).build())));
    /** SPEC: 3 food and an easter egg with no explanation ({@link TangerineItem}). */
    public static final RegistrySupplier<Item> TANGERINE = register("tangerine", props -> new TangerineItem(props.food(
            new FoodProperties.Builder().nutrition(3).saturationModifier(0.3F).build())));
    /** An orange: the other fruit of the citrus trees. A little more filling than a tangerine, no secret. */
    public static final RegistrySupplier<Item> ORANGE = register("orange", props -> new Item(props.food(
            new FoodProperties.Builder().nutrition(4).saturationModifier(0.3F).build())));
    static {
        TANGERINE.listen(i -> ComposterBlock.COMPOSTABLES.put(i, 0.5F));
        ORANGE.listen(i -> ComposterBlock.COMPOSTABLES.put(i, 0.5F));
    }

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
                output.accept(ORANGE.get());
                for (WoodSet set : CozyWoods.ALL) {
                    for (RegistrySupplier<Item> item : set.items()) {
                        output.accept(item.get());
                    }
                }
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
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(dye.getTextureDiffuseColor() & RGB_MASK, true));
        return stack;
    }

    /** An unopened surprise present: karma decides what's inside. */
    public static ItemStack present(PresentDesign design) {
        ItemStack stack = new ItemStack(CozyBlocks.PRESENT_ITEM.get());
        stack.set(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY.with(PresentBlock.DESIGN, design));
        return stack;
    }

    /** Architectury's egg looks its entity type up late (NeoForge builds items in its own registry event). */
    private static RegistrySupplier<Item> spawnEgg(String name, RegistrySupplier<? extends EntityType<? extends Mob>> entity) {
        return register(name, props -> new ArchitecturySpawnEggItem(entity, SPAWN_EGG_TINT, SPAWN_EGG_TINT, props));
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
        return REGISTER.register(name, () -> factory.apply(new Item.Properties()));
    }

    /** A food that is drunk: 1.21.1 has no consumable component, the item says how it's used and heard. */
    private static final class Drink extends Item {

        private Drink(Properties properties) {
            super(properties);
        }

        @Override
        public UseAnim getUseAnimation(ItemStack stack) {
            return UseAnim.DRINK;
        }

        /**
         * 1.21.1 hands the mug or bottle back to players only ({@code Player#eat}); 26.3's {@code use_remainder} leaves it
         * for whatever drank, so do that for everything else.
         */
        @Override
        public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity drinker) {
            FoodProperties food = stack.get(DataComponents.FOOD);
            ItemStack left = super.finishUsingItem(stack, level, drinker);
            if (food != null && !(drinker instanceof Player) && left.isEmpty()) {
                return food.usingConvertsTo().map(ItemStack::copy).orElse(left);
            }
            return left;
        }

        @Override
        public SoundEvent getDrinkingSound() {
            return SoundEvents.GENERIC_DRINK;
        }

        @Override
        public SoundEvent getEatingSound() {
            return SoundEvents.GENERIC_DRINK;
        }
    }
}
