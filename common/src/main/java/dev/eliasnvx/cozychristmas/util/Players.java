package dev.eliasnvx.cozychristmas.util;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/** Player checks shared by features. */
public final class Players {

    private Players() {
    }

    /**
     * In survival or adventure: a player the game can touch (blame, rob). A server player's mode is read from its game
     * mode manager, because GameTest mock players report creative from {@code isCreative()} whatever their mode is.
     */
    public static boolean inPlay(Player player) {
        if (player instanceof ServerPlayer server) {
            return server.gameMode.getGameModeForPlayer().isSurvival();
        }
        return !player.isCreative() && !player.isSpectator();
    }
}
