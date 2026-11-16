package dev.eliasnvx.cozychristmas.gametest;

import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Field;
import java.util.UUID;

/** Mock players for the shared GameTests. */
public final class TestPlayers {

    /** Mojang name of ServerPlayer's private join protection counter (GameTests only run in dev, with Mojang names). */
    private static final String SPAWN_INVULNERABLE_TIME = "spawnInvulnerableTime";

    private TestPlayers() {
    }

    /**
     * A logged-in survival player standing at {@code relative} in the test area. Remove it with {@link #remove}.
     * Not {@link GameTestHelper#makeMockServerPlayerInLevel()}: its player answers {@code isCreative()} with true whatever
     * its game mode, so mobs ignore it and items aren't used up, and on 1.20.1 its connection has no netty channel, so
     * Forge's login hooks crash sending to it. This is the same login with a plain player over an {@link EmbeddedChannel}
     * (what vanilla itself does from 1.20.5 on); Forge's network filters skip the embedded pipeline.
     */
    static ServerPlayer survival(GameTestHelper helper, Vec3 relative) {
        MinecraftServer server = helper.getLevel().getServer();
        ServerPlayer player = new ServerPlayer(server, helper.getLevel(), new GameProfile(UUID.randomUUID(), "test-mock-player"));
        Connection connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection); // activates the connection: it now has a channel
        server.getPlayerList().placeNewPlayer(connection, player); // joins at world spawn
        Vec3 pos = helper.absoluteVec(relative);
        player.moveTo(pos.x, pos.y, pos.z);
        player.setGameMode(GameType.SURVIVAL);
        loaded(player);
        return player;
    }

    /** Ends the join protection (3 seconds on 1.20.1), during which the server keeps the player invulnerable. */
    static void loaded(ServerPlayer player) {
        try {
            Field field = ServerPlayer.class.getDeclaredField(SPAWN_INVULNERABLE_TIME);
            field.setAccessible(true);
            field.setInt(player, 0);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("ServerPlayer#" + SPAWN_INVULNERABLE_TIME + " not found", e);
        }
    }

    static void remove(GameTestHelper helper, ServerPlayer player) {
        helper.getLevel().getServer().getPlayerList().remove(player);
    }
}
