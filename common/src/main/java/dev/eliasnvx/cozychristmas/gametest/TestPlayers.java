package dev.eliasnvx.cozychristmas.gametest;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.protocol.game.ServerboundPlayerLoadedPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

/** Mock players for the shared GameTests. */
final class TestPlayers {

    private TestPlayers() {
    }

    /** A survival mock player standing at {@code relative} in the test area. Remove it with {@link #remove}. */
    @SuppressWarnings("removal") // vanilla's only way to get a mock player into the level; no replacement in 26.3
    static ServerPlayer survival(GameTestHelper helper, Vec3 relative) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel(); // joins at world spawn, in creative
        player.snapTo(helper.absoluteVec(relative));
        player.setGameMode(GameType.SURVIVAL);
        loaded(player);
        return player;
    }

    /** What a real client sends once the world is shown; until then the server keeps the player invulnerable. */
    static void loaded(ServerPlayer player) {
        player.connection.handleAcceptPlayerLoad(new ServerboundPlayerLoadedPacket());
    }

    static void remove(GameTestHelper helper, ServerPlayer player) {
        helper.getLevel().getServer().getPlayerList().remove(player);
    }
}
