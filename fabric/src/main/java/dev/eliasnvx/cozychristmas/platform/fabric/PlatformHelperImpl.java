package dev.eliasnvx.cozychristmas.platform.fabric;

import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.cosmetic.CosmeticInventory;
import dev.eliasnvx.cozychristmas.karma.Karma;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.world.entity.player.Player;

import java.util.Objects;

public final class PlatformHelperImpl {

    private static final AttachmentType<Karma> KARMA = AttachmentRegistry.create(
            CozyChristmas.id("karma"),
            builder -> builder
                    .initializer(() -> Karma.ZERO)
                    .persistent(Karma.CODEC)
                    .copyOnDeath());

    private static final AttachmentType<CosmeticInventory> COSMETICS = AttachmentRegistry.create(
            CozyChristmas.id("cosmetics"),
            builder -> builder
                    .initializer(() -> CosmeticInventory.EMPTY)
                    .persistent(CosmeticInventory.CODEC)
                    .copyOnDeath());

    private PlatformHelperImpl() {
    }

    /** Class-loads the attachment types; must run before {@link CozyChristmas#init()}. */
    public static void init() {
        Objects.requireNonNull(KARMA);
        Objects.requireNonNull(COSMETICS);
    }

    public static Karma getKarma(Player player) {
        return player.getAttachedOrElse(KARMA, Karma.ZERO);
    }

    public static void setKarma(Player player, Karma karma) {
        player.setAttached(KARMA, karma);
    }

    public static CosmeticInventory getCosmetics(Player player) {
        return player.getAttachedOrElse(COSMETICS, CosmeticInventory.EMPTY);
    }

    public static void setCosmetics(Player player, CosmeticInventory cosmetics) {
        player.setAttached(COSMETICS, cosmetics);
    }
}
