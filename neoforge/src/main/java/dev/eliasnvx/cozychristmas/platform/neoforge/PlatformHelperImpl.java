package dev.eliasnvx.cozychristmas.platform.neoforge;

import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.karma.Karma;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public final class PlatformHelperImpl {

    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, CozyChristmas.MOD_ID);

    private static final Supplier<AttachmentType<Karma>> KARMA = ATTACHMENTS.register("karma",
            () -> AttachmentType.builder(() -> Karma.ZERO)
                    .serialize(Karma.CODEC.fieldOf("karma"))
                    .copyOnDeath()
                    .build());

    private PlatformHelperImpl() {
    }

    /** Registers loader-side content; must run before {@link CozyChristmas#init()}. */
    public static void init(IEventBus modBus) {
        ATTACHMENTS.register(modBus);
    }

    public static Karma getKarma(Player player) {
        return player.getData(KARMA);
    }

    public static void setKarma(Player player, Karma karma) {
        player.setData(KARMA, karma);
    }
}
