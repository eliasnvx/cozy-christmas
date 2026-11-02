package dev.eliasnvx.cozychristmas.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.eliasnvx.cozychristmas.cosmetic.CosmeticInventory;
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

    /** Reads the player's cosmetics attachment (persistent, copied on death). Never null. */
    @ExpectPlatform
    public static CosmeticInventory getCosmetics(Player player) {
        throw new AssertionError();
    }

    /** Replaces the player's cosmetics attachment. Does not sync; use {@code CosmeticsManager}, which does. */
    @ExpectPlatform
    public static void setCosmetics(Player player, CosmeticInventory cosmetics) {
        throw new AssertionError();
    }
}
