package dev.eliasnvx.cozychristmas.gametest;

import dev.eliasnvx.cozychristmas.config.CozyConfig;
import dev.eliasnvx.cozychristmas.karma.Karma;
import dev.eliasnvx.cozychristmas.karma.KarmaManager;
import dev.eliasnvx.cozychristmas.karma.KarmaSettings;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

/**
 * Karma on a real server, shared by both loaders (Fabric: {@code fabric/src/gametest}, Forge:
 * {@code forge/.../dev/CozyGameTestsForge}): storage, the hurt/kill rules through real damage, and keeping karma
 * through death. The pure math (drift, tiers, odds) is in JUnit. Dev only: not in the released jars.
 */
public final class KarmaGameTests {

    public static final List<GameTestEntry> ALL = List.of(
            new GameTestEntry("karma_stays_in_range", KarmaGameTests::karmaStaysInRange),
            new GameTestEntry("hurting_a_villager_is_naughty", KarmaGameTests::hurtingAVillagerIsNaughty),
            new GameTestEntry("two_players_hurting_one_villager_both_pay", KarmaGameTests::twoPlayersBothPay),
            new GameTestEntry("hurting_a_zombie_is_fine", KarmaGameTests::hurtingAZombieIsFine),
            new GameTestEntry("killing_a_villager_costs_more", KarmaGameTests::killingAVillagerCostsMore),
            new GameTestEntry("karma_survives_death", KarmaGameTests::karmaSurvivesDeath));

    private static final Vec3 TEST_AREA_CENTER = new Vec3(1.5, 1.0, 1.5);

    private KarmaGameTests() {
    }

    public static void karmaStaysInRange(GameTestHelper helper) {
        withPlayer(helper, player -> {
            GameTestAsserts.assertValueEqual(helper, KarmaManager.get(player), 0, "karma of a new player");
            KarmaManager.set(player, 90);
            GameTestAsserts.assertValueEqual(helper, KarmaManager.add(player, 50), Karma.MAX, "karma after +50 from 90");
            GameTestAsserts.assertValueEqual(helper, KarmaManager.add(player, -500), Karma.MIN, "karma after -500");
        });
    }

    public static void hurtingAVillagerIsNaughty(GameTestHelper helper) {
        withPlayer(helper, player -> {
            LivingEntity villager = helper.spawn(EntityType.VILLAGER, TEST_AREA_CENTER);
            hit(player, villager, 1.0F);
            int afterOne = settings().hurtProtected();
            GameTestAsserts.assertValueEqual(helper, KarmaManager.get(player), afterOne, "karma after one hit");
            hit(player, villager, 1.0F); // same tick: inside the cooldown, a fast clicker pays once
            GameTestAsserts.assertValueEqual(helper, KarmaManager.get(player), afterOne, "karma after a second hit in the cooldown");
            villager.discard();
        });
    }

    /** The cooldown is per player and victim: a second player's blow after the first one's isn't free. */
    public static void twoPlayersBothPay(GameTestHelper helper) {
        ServerPlayer first = TestPlayers.survival(helper, TEST_AREA_CENTER);
        ServerPlayer second = TestPlayers.survival(helper, TEST_AREA_CENTER.add(2.0, 0.0, 0.0));
        LivingEntity villager = helper.spawn(EntityType.VILLAGER, TEST_AREA_CENTER.add(1.0, 0.0, 2.0));
        hit(first, villager, 1.0F);
        helper.runAfterDelay(15, () -> { // past the villager's invulnerability, inside the cooldown
            try {
                hit(second, villager, 1.0F);
                GameTestAsserts.assertValueEqual(helper, KarmaManager.get(second), settings().hurtProtected(), "the second player's karma");
                helper.succeed();
            } finally {
                villager.discard();
                TestPlayers.remove(helper, first);
                TestPlayers.remove(helper, second);
            }
        });
    }

    public static void hurtingAZombieIsFine(GameTestHelper helper) {
        withPlayer(helper, player -> {
            LivingEntity zombie = helper.spawn(EntityType.ZOMBIE, TEST_AREA_CENTER);
            hit(player, zombie, 1.0F);
            GameTestAsserts.assertValueEqual(helper, KarmaManager.get(player), 0, "karma after hitting a zombie");
            zombie.discard();
        });
    }

    public static void killingAVillagerCostsMore(GameTestHelper helper) {
        withPlayer(helper, player -> {
            LivingEntity villager = helper.spawn(EntityType.VILLAGER, TEST_AREA_CENTER);
            hit(player, villager, 1000.0F);
            helper.assertTrue(villager.isDeadOrDying(), "the villager should be dead");
            GameTestAsserts.assertValueEqual(helper, KarmaManager.get(player), settings().hurtProtected() + settings().killProtected(),
                    "karma after a killing blow");
        });
    }

    public static void karmaSurvivesDeath(GameTestHelper helper) {
        withReplacedPlayer(helper, player -> {
            KarmaManager.set(player, -50);
            player.kill();
            ServerPlayer respawned = helper.getLevel().getServer().getPlayerList()
                    .respawn(player, false);
            GameTestAsserts.assertValueEqual(helper, KarmaManager.get(respawned), -50, "karma after death and respawn");
            return respawned;
        });
    }

    private static void hit(ServerPlayer player, LivingEntity victim, float damage) {
        victim.hurt(player.damageSources().playerAttack(player), damage);
    }

    private static KarmaSettings settings() {
        return CozyConfig.common().karma();
    }

    private static void withPlayer(GameTestHelper helper, Consumer<ServerPlayer> body) {
        withReplacedPlayer(helper, player -> {
            body.accept(player);
            return player;
        });
    }

    /** Runs {@code body} with a survival mock player, then removes the player the body returns (a respawn replaces it). */
    private static void withReplacedPlayer(GameTestHelper helper, UnaryOperator<ServerPlayer> body) {
        ServerPlayer player = TestPlayers.survival(helper, TEST_AREA_CENTER);
        try {
            player = body.apply(player);
            helper.succeed();
        } finally {
            TestPlayers.remove(helper, player);
        }
    }
}
