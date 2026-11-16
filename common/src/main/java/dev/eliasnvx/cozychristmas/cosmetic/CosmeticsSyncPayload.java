package dev.eliasnvx.cozychristmas.cosmetic;

import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.platform.PlatformHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/** S2C: full cosmetic state of one player. Sent on change, on tracking start and on join/respawn/dimension change. */
public record CosmeticsSyncPayload(int entityId, CosmeticInventory cosmetics) implements CustomPacketPayload {

    public static final Type<CosmeticsSyncPayload> TYPE = new Type<>(CozyChristmas.id("cosmetics_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CosmeticsSyncPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, CosmeticsSyncPayload::entityId,
            CosmeticInventory.STREAM_CODEC, CosmeticsSyncPayload::cosmetics,
            CosmeticsSyncPayload::new);

    @Override
    public Type<CosmeticsSyncPayload> type() {
        return TYPE;
    }

    public static CosmeticsSyncPayload of(Player player) {
        return new CosmeticsSyncPayload(player.getId(), CosmeticsManager.get(player));
    }

    /**
     * Architectury 13: the receiver is registered on the client; a dedicated server only registers the payload type.
     * The handler uses common classes only, so referencing it on the server is safe.
     */
    public static void register() {
        if (Platform.getEnvironment() == Env.CLIENT) {
            NetworkManager.registerReceiver(NetworkManager.s2c(), TYPE, STREAM_CODEC, CosmeticsSyncPayload::handle);
        } else {
            NetworkManager.registerS2CPayloadType(TYPE, STREAM_CODEC);
        }
    }

    /** Receiver (client side). Uses only common classes, so it is safe to register everywhere. */
    private static void handle(CosmeticsSyncPayload payload, NetworkManager.PacketContext context) {
        context.queue(() -> {
            Entity entity = context.getPlayer().level().getEntity(payload.entityId());
            if (entity instanceof Player player) {
                PlatformHelper.setCosmetics(player, payload.cosmetics());
            }
        });
    }

    /** Clients without the mod cannot decode the payload (and NeoForge throws if we try). */
    private static boolean canReceive(ServerPlayer player) {
        return NetworkManager.canPlayerReceive(player, TYPE);
    }

    public static void sendTo(ServerPlayer receiver, Player about) {
        if (canReceive(receiver)) {
            NetworkManager.sendToPlayer(receiver, of(about));
        }
    }

    @SuppressWarnings("unchecked")
    public static void sendToTrackingAndSelf(ServerPlayer player) {
        Packet<? super ClientGamePacketListener> packet = (Packet<? super ClientGamePacketListener>)
                NetworkManager.toPacket(NetworkManager.s2c(), of(player), player.registryAccess());
        // 1.21.1 has no filtered tracking broadcast; players in view of the chunk are a superset of the trackers, and
        // the receiver ignores entity ids it doesn't know
        for (ServerPlayer watcher : ((ServerLevel) player.level()).getChunkSource().chunkMap
                .getPlayers(player.chunkPosition(), false)) {
            if (watcher != player && canReceive(watcher)) {
                watcher.connection.send(packet);
            }
        }
        if (canReceive(player)) {
            player.connection.send(packet);
        }
    }
}
