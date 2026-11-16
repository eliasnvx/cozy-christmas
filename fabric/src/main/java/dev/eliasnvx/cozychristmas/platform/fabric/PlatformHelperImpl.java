package dev.eliasnvx.cozychristmas.platform.fabric;

import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.cosmetic.CosmeticInventory;
import dev.eliasnvx.cozychristmas.karma.Karma;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.world.entity.player.Player;

import java.util.Objects;

public final class PlatformHelperImpl {

    // Fabric API 0.92 ships the (experimental) Data Attachment API v1.0.x: builder() + buildAndRegister(id)
    private static final AttachmentType<Karma> KARMA = AttachmentRegistry.<Karma>builder()
            .initializer(() -> Karma.ZERO)
            .persistent(Karma.CODEC)
            .copyOnDeath()
            .buildAndRegister(CozyChristmas.id("karma"));

    private static final AttachmentType<CosmeticInventory> COSMETICS = AttachmentRegistry.<CosmeticInventory>builder()
            .initializer(() -> CosmeticInventory.EMPTY)
            .persistent(CosmeticInventory.CODEC)
            .copyOnDeath()
            .buildAndRegister(CozyChristmas.id("cosmetics"));

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
