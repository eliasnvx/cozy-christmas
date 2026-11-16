package dev.eliasnvx.cozychristmas.platform.forge;

import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.cosmetic.CosmeticInventory;
import dev.eliasnvx.cozychristmas.karma.Karma;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public final class PlatformHelperImpl {

    private PlatformHelperImpl() {
    }

    /** Registers loader-side content; must run before {@link CozyChristmas#init()}. */
    public static void init(IEventBus modBus) {
        modBus.addListener((RegisterCapabilitiesEvent event) -> event.register(CozyPlayerData.class));
        MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, (AttachCapabilitiesEvent<Entity> event) -> {
            if (event.getObject() instanceof Player) {
                event.addCapability(CozyPlayerData.ID, new CozyPlayerData.Provider());
            }
        });
        MinecraftForge.EVENT_BUS.addListener(PlatformHelperImpl::onClone);
    }

    /**
     * Copies karma and cosmetics to the new player on respawn and on return from the End. The old player's caps are
     * already invalidated here, so they are revived for the copy and invalidated again afterwards.
     */
    private static void onClone(PlayerEvent.Clone event) {
        Player original = event.getOriginal();
        original.reviveCaps();
        try {
            original.getCapability(CozyPlayerData.CAPABILITY).ifPresent(old -> event.getEntity()
                    .getCapability(CozyPlayerData.CAPABILITY)
                    .ifPresent(data -> data.copyFrom(old)));
        } finally {
            original.invalidateCaps();
        }
    }

    public static Karma getKarma(Player player) {
        CozyPlayerData data = data(player);
        return data == null ? Karma.ZERO : data.karma;
    }

    public static void setKarma(Player player, Karma karma) {
        CozyPlayerData data = data(player);
        if (data != null) {
            data.karma = karma;
        }
    }

    public static CosmeticInventory getCosmetics(Player player) {
        CozyPlayerData data = data(player);
        return data == null ? CosmeticInventory.EMPTY : data.cosmetics;
    }

    public static void setCosmetics(Player player, CosmeticInventory cosmetics) {
        CozyPlayerData data = data(player);
        if (data != null) {
            data.cosmetics = cosmetics;
        }
    }

    /** orElse instead of map: read every frame by the cosmetic layer, no Optional allocation. */
    private static CozyPlayerData data(Player player) {
        return player.getCapability(CozyPlayerData.CAPABILITY).orElse(null);
    }
}
