package dev.eliasnvx.cozychristmas.gametest;

import dev.eliasnvx.cozychristmas.config.CozyConfig;
import dev.eliasnvx.cozychristmas.cosmetic.CosmeticSlot;
import dev.eliasnvx.cozychristmas.cosmetic.CosmeticsManager;
import dev.eliasnvx.cozychristmas.cosmetic.ToastySet;
import dev.eliasnvx.cozychristmas.registry.CozyItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/**
 * The Toasty set's bonuses on a real server, shared by both loaders (Fabric: {@code fabric/src/gametest}, NeoForge:
 * {@code neoforge/.../dev/CozyGameTestsNeoForge}). Dev only: not in the released jars.
 */
public final class ToastySetGameTests {

    public static final List<GameTestEntry> ALL = List.of(
            new GameTestEntry("full_set_keeps_warm", ToastySetGameTests::fullSetKeepsWarm),
            new GameTestEntry("faster_on_snow_only", ToastySetGameTests::fasterOnSnowOnly));

    private static final Vec3 TEST_AREA_CENTER = new Vec3(1.5, 1.0, 1.5);
    private static final BlockPos FLOOR = new BlockPos(1, 0, 1);
    private static final BlockPos FEET = FLOOR.above();

    private ToastySetGameTests() {
    }

    public static void fullSetKeepsWarm(GameTestHelper helper) {
        withPlayer(helper, player -> {
            helper.assertTrue(player.canFreeze(), "a player without the set freezes");
            wearSetBut(player, CosmeticSlot.HANDS);
            helper.assertTrue(player.canFreeze(), "three pieces are not the set");
            wear(player, CosmeticSlot.HANDS, CozyItems.MITTENS.get().getDefaultInstance());
            helper.assertValueEqual(player.canFreeze(), !CozyConfig.common().toastySet().keepsWarm(),
                    "freezing with the whole set");
        });
    }

    public static void fasterOnSnowOnly(GameTestHelper helper) {
        withPlayer(helper, player -> {
            double bonus = CozyConfig.common().toastySet().snowSpeedBonus();
            player.setOnGround(true);
            stand(helper, player, Blocks.SNOW_BLOCK, Blocks.AIR);
            helper.assertValueEqual(bonus(player), 0.0, "bonus on snow without the set");

            wearSet(player);
            stand(helper, player, Blocks.SNOW_BLOCK, Blocks.AIR);
            helper.assertValueEqual(bonus(player), bonus, "bonus on a snow block");
            stand(helper, player, Blocks.STONE, Blocks.AIR);
            helper.assertValueEqual(bonus(player), 0.0, "bonus on stone");
            stand(helper, player, Blocks.STONE, Blocks.SNOW);
            helper.assertValueEqual(bonus(player), bonus, "bonus in a snow layer");

            // airborne the last decision holds (no flicker while hopping), but taking a piece off ends it at once
            player.setOnGround(false);
            stand(helper, player, Blocks.STONE, Blocks.AIR);
            helper.assertValueEqual(bonus(player), bonus, "bonus in the air after leaving snow");
            wear(player, CosmeticSlot.HEAD, ItemStack.EMPTY);
            ToastySet.updateSnowSpeed(player);
            helper.assertValueEqual(bonus(player), 0.0, "bonus in the air without the hat");
        });
    }

    private static void wearSet(ServerPlayer player) {
        wearSetBut(player, null);
    }

    private static void wearSetBut(ServerPlayer player, @Nullable CosmeticSlot missing) {
        List<ItemStack> pieces = List.of(CozyItems.POMPOM_HAT.get().getDefaultInstance(),
                CozyItems.SCARF.get().getDefaultInstance(), CozyItems.SNOWFLAKE_SWEATER.get().getDefaultInstance(),
                CozyItems.MITTENS.get().getDefaultInstance());
        for (ItemStack piece : pieces) {
            CosmeticSlot slot = CosmeticsManager.slotOf(piece);
            if (slot != missing) {
                wear(player, slot, piece);
            }
        }
    }

    private static void wear(ServerPlayer player, CosmeticSlot slot, ItemStack stack) {
        CosmeticsManager.set(player, slot, stack);
    }

    /** Puts {@code floor} under the player and {@code feet} where it stands, then runs the per-tick update. */
    private static void stand(GameTestHelper helper, ServerPlayer player, Block floor, Block feet) {
        helper.setBlock(FLOOR, floor);
        helper.setBlock(FEET, feet);
        ToastySet.updateSnowSpeed(player);
    }

    private static double bonus(ServerPlayer player) {
        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        AttributeModifier modifier = speed == null ? null : speed.getModifier(ToastySet.SNOW_SPEED_ID);
        return modifier == null ? 0.0 : modifier.amount();
    }

    private static void withPlayer(GameTestHelper helper, Consumer<ServerPlayer> body) {
        ServerPlayer player = TestPlayers.survival(helper, TEST_AREA_CENTER);
        try {
            body.accept(player);
            helper.succeed();
        } finally {
            TestPlayers.remove(helper, player);
        }
    }
}
