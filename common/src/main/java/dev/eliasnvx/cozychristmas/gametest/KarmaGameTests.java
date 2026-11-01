package dev.eliasnvx.cozychristmas.gametest;

import dev.eliasnvx.cozychristmas.config.CozyConfig;
import dev.eliasnvx.cozychristmas.karma.Karma;
import dev.eliasnvx.cozychristmas.karma.KarmaManager;
import dev.eliasnvx.cozychristmas.karma.KarmaSettings;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.protocol.game.ServerboundPlayerLoadedPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

/**
 * Karma on a real server, shared by both loaders (Fabric: {@code fabric/src/gametest}, NeoForge:
 * {@code neoforge/.../dev/CozyGameTestsNeoForge}): storage, the hurt/kill rules through real damage, and keeping karma
 * through death. The pure math (drift, tiers, odds) is in JUnit. Dev only: not in the released jars.
 */
public final class KarmaGameTests {

    /** A test: its name and body. The loader glue registers every entry of {@link #ALL}. */
    public record Entry(String name, Consumer<GameTestHelper> body) {
    }

    public static final List<Entry> ALL = List.of(
            new Entry("karma_stays_in_range", KarmaGameTests::karmaStaysInRange),
            new Entry("hurting_a_villager_is_naughty", KarmaGameTests::hurtingAVillagerIsNaughty),
            new Entry("hurting_a_zombie_is_fine", KarmaGameTests::hurtingAZombieIsFine),
            new Entry("killing_a_villager_costs_more", KarmaGameTests::killingAVillagerCostsMore),
            new Entry("karma_survives_death", KarmaGameTests::karmaSurvivesDeath));

    private static final Vec3 TEST_AREA_CENTER = new Vec3(1.5, 1.0, 1.5);

    private KarmaGameTests() {
    }

    public static void karmaStaysInRange(GameTestHelper helper) {
        withPlayer(helper, player -> {
            helper.assertValueEqual(KarmaManager.get(player), 0, "karma of a new player");
            KarmaManager.set(player, 90);
            helper.assertValueEqual(KarmaManager.add(player, 50), Karma.MAX, "karma after +50 from 90");
            helper.assertValueEqual(KarmaManager.add(player, -500), Karma.MIN, "karma after -500");
        });
    }

    public static void hurtingAVillagerIsNaughty(GameTestHelper helper) {
        withPlayer(helper, player -> {
            LivingEntity villager = helper.spawn(EntityTypes.VILLAGER, TEST_AREA_CENTER);
            hit(player, villager, 1.0F);
            int afterOne = settings().hurtProtected();
            helper.assertValueEqual(KarmaManager.get(player), afterOne, "karma after one hit");
            hit(player, villager, 1.0F); // same tick: inside the cooldown, a fast clicker pays once
            helper.assertValueEqual(KarmaManager.get(player), afterOne, "karma after a second hit in the cooldown");
            villager.discard();
        });
    }

    public static void hurtingAZombieIsFine(GameTestHelper helper) {
        withPlayer(helper, player -> {
            LivingEntity zombie = helper.spawn(EntityTypes.ZOMBIE, TEST_AREA_CENTER);
            hit(player, zombie, 1.0F);
            helper.assertValueEqual(KarmaManager.get(player), 0, "karma after hitting a zombie");
            zombie.discard();
        });
    }

    public static void killingAVillagerCostsMore(GameTestHelper helper) {
        withPlayer(helper, player -> {
            LivingEntity villager = helper.spawn(EntityTypes.VILLAGER, TEST_AREA_CENTER);
            hit(player, villager, 1000.0F);
            helper.assertTrue(villager.isDeadOrDying(), "the villager should be dead");
            helper.assertValueEqual(KarmaManager.get(player), settings().hurtProtected() + settings().killProtected(),
                    "karma after a killing blow");
        });
    }

    public static void karmaSurvivesDeath(GameTestHelper helper) {
        withReplacedPlayer(helper, player -> {
            KarmaManager.set(player, -50);
            player.kill(helper.getLevel());
            ServerPlayer respawned = helper.getLevel().getServer().getPlayerList()
                    .respawn(player, false, Entity.RemovalReason.KILLED);
            helper.assertValueEqual(KarmaManager.get(respawned), -50, "karma after death and respawn");
            return respawned;
        });
    }

    private static void hit(ServerPlayer player, LivingEntity victim, float damage) {
        victim.hurtServer(player.level(), player.damageSources().playerAttack(player), damage);
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
    @SuppressWarnings("removal") // vanilla's only way to get a mock player into the level; no replacement in 26.3
    private static void withReplacedPlayer(GameTestHelper helper, UnaryOperator<ServerPlayer> body) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel(); // joins at world spawn, in creative
        player.snapTo(helper.absoluteVec(TEST_AREA_CENTER));
        player.setGameMode(GameType.SURVIVAL);
        // what a real client sends once the world is shown; until then the server keeps the player invulnerable
        player.connection.handleAcceptPlayerLoad(new ServerboundPlayerLoadedPacket());
        try {
            player = body.apply(player);
            helper.succeed();
        } finally {
            helper.getLevel().getServer().getPlayerList().remove(player);
        }
    }
}
