package dev.eliasnvx.cozychristmas.gametest;

import dev.eliasnvx.cozychristmas.config.CozyConfig;
import dev.eliasnvx.cozychristmas.registry.CozyBlocks;
import dev.eliasnvx.cozychristmas.registry.CozyEntities;
import dev.eliasnvx.cozychristmas.registry.CozyItems;
import dev.eliasnvx.cozychristmas.snowman.EvilSnowman;
import dev.eliasnvx.cozychristmas.snowman.EvilSnowmanSettings;
import dev.eliasnvx.cozychristmas.snowman.IceSnowball;
import dev.eliasnvx.cozychristmas.stocking.FireplaceBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Locale;

/**
 * Evil snowmen and their ice snowballs on a real server, shared by both loaders (Fabric: {@code fabric/src/gametest},
 * NeoForge: {@code neoforge/.../dev/CozyGameTestsNeoForge}). Dev only: not in the released jars.
 */
public final class SnowmanGameTests {

    public static final List<GameTestEntry> ALL = List.of(
            new GameTestEntry("ice_snowballs_slow_and_chill", SnowmanGameTests::iceSnowballsSlowAndChill),
            new GameTestEntry("snowmen_dont_hit_each_other", SnowmanGameTests::snowmenDontHitEachOther),
            new GameTestEntry("evil_snowman_throws_ice_snowballs", SnowmanGameTests::throwsIceSnowballs),
            new GameTestEntry("fire_hurts_snowmen_more", SnowmanGameTests::fireHurtsMore),
            new GameTestEntry("a_lit_fireplace_melts_snowmen", SnowmanGameTests::litFireplaceMelts),
            new GameTestEntry("dispensers_throw_ice_snowballs", SnowmanGameTests::dispensersThrow),
            new GameTestEntry("evil_snowmen_spawn_in_snowy_biomes", SnowmanGameTests::spawnInSnowyBiomes));

    private static final Vec3 COLD_PIG = new Vec3(1.5, 1.0, 1.5);
    private static final Vec3 WARM_PIG = new Vec3(5.5, 1.0, 1.5);
    private static final Vec3 SNOWMAN = new Vec3(2.5, 1.0, 4.5);
    private static final Vec3 OTHER_SNOWMAN = new Vec3(5.5, 1.0, 4.5);
    private static final BlockPos FIREPLACE = new BlockPos(4, 1, 4);
    private static final BlockPos DISPENSER = new BlockPos(1, 1, 1);
    private static final double DROP = 3.0;
    private static final float DOWN_SPEED = 1.0F;
    private static final int SETTLE_TICKS = 25;
    private static final double SEARCH = 6.0;
    private static final BlockPos AREA = new BlockPos(7, 7, 7);
    /** Projectiles' hitboxes grow over their first ticks; a ball dropped this close must already be in full flight. */
    private static final int IN_FLIGHT = 20;

    private SnowmanGameTests() {
    }

    /** Dropped onto two pigs: both hurt and slowed; only the one in leather boots doesn't freeze. */
    public static void iceSnowballsSlowAndChill(GameTestHelper helper) {
        Pig cold = pig(helper, COLD_PIG);
        Pig warm = pig(helper, WARM_PIG);
        warm.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.LEATHER_BOOTS));
        float health = cold.getHealth();
        dropBall(helper, COLD_PIG, null);
        dropBall(helper, WARM_PIG, null);
        var settings = CozyConfig.common().evilSnowman().iceSnowball();
        helper.succeedWhen(() -> {
            helper.assertTrue(cold.hasEffect(MobEffects.SLOWNESS) && warm.hasEffect(MobEffects.SLOWNESS), "both pigs are slowed");
            helper.assertTrue(settings.damage() == 0.0 || cold.getHealth() < health, "the ice hurts");
            helper.assertTrue(settings.chillTicks() == 0 || cold.getTicksFrozen() > 0, "the bare pig is chilled");
            helper.assertValueEqual(warm.getTicksFrozen(), 0, "leather boots keep the cold out");
        });
    }

    /** A snowman's ice flies through another snowman. */
    public static void snowmenDontHitEachOther(GameTestHelper helper) {
        EvilSnowman thrower = snowman(helper, SNOWMAN);
        EvilSnowman other = snowman(helper, OTHER_SNOWMAN);
        IceSnowball ball = dropBall(helper, OTHER_SNOWMAN, thrower);
        helper.runAfterDelay(SETTLE_TICKS, () -> {
            helper.assertTrue(ball.isRemoved(), "the ball landed");
            helper.assertValueEqual(other.getHealth(), other.getMaxHealth(), "the other snowman is unhurt");
            helper.assertFalse(other.hasEffect(MobEffects.SLOWNESS), "and not slowed");
            helper.succeed();
        });
    }

    public static void throwsIceSnowballs(GameTestHelper helper) {
        EvilSnowman snowman = snowman(helper, SNOWMAN);
        Pig target = pig(helper, COLD_PIG);
        snowman.performRangedAttack(target, 1.0F);
        List<IceSnowball> balls = balls(helper);
        helper.assertTrue(balls.size() == 1 && balls.getFirst().getOwner() == snowman, "one ice snowball, thrown by the snowman");
        helper.succeed();
    }

    /** Fire hurts {@code fire_multiplier} times as much as other damage. */
    public static void fireHurtsMore(GameTestHelper helper) {
        EvilSnowman hit = snowman(helper, SNOWMAN);
        EvilSnowman burnt = snowman(helper, OTHER_SNOWMAN);
        ServerLevel level = helper.getLevel();
        hit.hurtServer(level, level.damageSources().generic(), 1.0F);
        burnt.hurtServer(level, level.damageSources().onFire(), 1.0F);
        helper.assertValueEqual(hit.getMaxHealth() - hit.getHealth(), 1.0F, "a plain hit");
        helper.assertValueEqual(burnt.getMaxHealth() - burnt.getHealth(), CozyConfig.common().evilSnowman().fireDamage(1.0F), "fire");
        helper.succeed();
    }

    /** Next to an unlit fireplace a snowman is fine; light it and it melts. */
    public static void litFireplaceMelts(GameTestHelper helper) {
        EvilSnowmanSettings settings = CozyConfig.common().evilSnowman();
        helper.setBlock(FIREPLACE, CozyBlocks.FIREPLACE.get().defaultBlockState());
        EvilSnowman snowman = snowman(helper, SNOWMAN);
        helper.assertFalse(snowman.nearHeat(helper.getLevel()), "a cold fireplace is just bricks");
        helper.setBlock(FIREPLACE, CozyBlocks.FIREPLACE.get().defaultBlockState().setValue(FireplaceBlock.LIT, true));
        helper.assertValueEqual(snowman.nearHeat(helper.getLevel()), settings.heatRadius() > 0, "a lit one is warm");
        if (settings.heatRadius() == 0) {
            helper.succeed();
            return;
        }
        helper.succeedWhen(() -> helper.assertTrue(snowman.getHealth() < snowman.getMaxHealth(), "the snowman melts"));
    }

    public static void dispensersThrow(GameTestHelper helper) {
        helper.setBlock(DISPENSER, Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING, Direction.UP));
        DispenserBlockEntity dispenser = helper.getBlockEntity(DISPENSER, DispenserBlockEntity.class);
        dispenser.setItem(0, new ItemStack(CozyItems.ICE_SNOWBALL.get()));
        helper.setBlock(DISPENSER.east(), Blocks.REDSTONE_BLOCK);
        helper.succeedWhen(() -> helper.assertFalse(balls(helper).isEmpty(), "the dispenser threw an ice snowball"));
    }

    /** Natural spawns sit in the biome's environment attribute on 26.3 (see GoblinGameTests#spawnInSnowyBiomes). */
    public static void spawnInSnowyBiomes(GameTestHelper helper) {
        Biome biome = helper.getLevel().registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.SNOWY_PLAINS).value();
        MobSpawnSettings spawns = biome.getAttributes().applyModifier(EnvironmentAttributes.NATURAL_MOB_SPAWNS,
                EnvironmentAttributes.NATURAL_MOB_SPAWNS.defaultValue());
        boolean snowmen = CozyConfig.common().evilSnowman().spawnWeight() == 0 || spawns.getMobsInCategory(MobCategory.MONSTER)
                .unwrap().stream().anyMatch(weighted -> weighted.value().type() == CozyEntities.EVIL_SNOWMAN.get());
        helper.assertTrue(snowmen, "evil snowmen in the snowy plains' monster spawns");
        helper.succeed();
    }

    private static Pig pig(GameTestHelper helper, Vec3 at) {
        Pig pig = helper.spawn(EntityTypes.PIG, at);
        pig.setNoAi(true);
        return pig;
    }

    /**
     * The GameTest world is a flat desert, where snow melts like vanilla's snow golem; the snowman tests stand on snowy
     * plains instead.
     */
    private static void snowyBiome(GameTestHelper helper) {
        BlockPos from = helper.absolutePos(BlockPos.ZERO);
        BlockPos to = helper.absolutePos(AREA);
        var server = helper.getLevel().getServer();
        server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withSuppressedOutput(), String.format(
                Locale.ROOT, "fillbiome %d %d %d %d %d %d minecraft:snowy_plains", Math.min(from.getX(), to.getX()), from.getY(),
                Math.min(from.getZ(), to.getZ()), Math.max(from.getX(), to.getX()), to.getY(), Math.max(from.getZ(), to.getZ())));
        helper.assertFalse(helper.getLevel().environmentAttributes().getValue(EnvironmentAttributes.SNOW_GOLEM_MELTS,
                helper.absoluteVec(SNOWMAN)), "the snowy plains don't melt snowmen");
    }

    private static EvilSnowman snowman(GameTestHelper helper, Vec3 at) {
        snowyBiome(helper);
        EvilSnowman snowman = helper.spawn(CozyEntities.EVIL_SNOWMAN.get(), at);
        snowman.setNoAi(true);
        return snowman;
    }

    /** An ice snowball thrown straight down onto whatever stands at {@code at}. */
    private static IceSnowball dropBall(GameTestHelper helper, Vec3 at, EvilSnowman owner) {
        Vec3 pos = helper.absoluteVec(at).add(0.0, DROP, 0.0);
        IceSnowball ball = new IceSnowball(helper.getLevel(), pos.x, pos.y, pos.z, new ItemStack(CozyItems.ICE_SNOWBALL.get()));
        if (owner != null) {
            ball.setOwner(owner);
        }
        ball.shoot(0.0, -1.0, 0.0, DOWN_SPEED, 0.0F);
        ball.tickCount = IN_FLIGHT;
        helper.getLevel().addFreshEntity(ball);
        return ball;
    }

    private static List<IceSnowball> balls(GameTestHelper helper) {
        AABB area = new AABB(helper.absolutePos(BlockPos.ZERO)).inflate(SEARCH);
        return helper.getLevel().getEntitiesOfClass(IceSnowball.class, area);
    }
}
