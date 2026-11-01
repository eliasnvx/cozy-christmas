package dev.eliasnvx.cozychristmas.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.eliasnvx.cozychristmas.karma.Karma;
import net.minecraft.world.entity.player.Player;

/** Loader-specific services. Implementations: {@code platform.fabric/neoforge.PlatformHelperImpl}. */
public final class PlatformHelper {

    private PlatformHelper() {
    }

    /** Reads the player's karma attachment (persistent, copied on death). Never null. */
    @ExpectPlatform
    public static Karma getKarma(Player player) {
        throw new AssertionError();
    }

    /** Replaces the player's karma attachment. Use {@code KarmaManager}, which settles the drift first. */
    @ExpectPlatform
    public static void setKarma(Player player, Karma karma) {
        throw new AssertionError();
    }
}
