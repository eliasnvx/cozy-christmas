package dev.eliasnvx.cozychristmas.network;

import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import dev.eliasnvx.cozychristmas.cosmetic.CosmeticsSyncPayload;
import dev.eliasnvx.cozychristmas.mixin.ServerGamePacketListenerAccessor;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.BiConsumer;
import java.util.function.Function;

/** Packets of the mod over Architectury's NetworkManager (adapted from Catgirl & Catboy Mod's 1.20.1 port). */
public final class CozyNetwork {

    private CozyNetwork() {
    }

    public static void register() {
        registerS2C(CosmeticsSyncPayload.ID, CosmeticsSyncPayload::read, CosmeticsSyncPayload::handle);
    }

    /** C2S receivers are registered on both sides (the client only needs to know the id). */
    public static <T> void registerC2S(ResourceLocation id, Function<FriendlyByteBuf, T> reader,
                                       BiConsumer<T, NetworkManager.PacketContext> handler) {
        NetworkManager.registerReceiver(NetworkManager.c2s(), id, (buf, context) -> handler.accept(reader.apply(buf), context));
    }

    /** S2C receivers exist on the client only; the handlers use common classes, so referencing them on a server is safe. */
    public static <T> void registerS2C(ResourceLocation id, Function<FriendlyByteBuf, T> reader,
                                       BiConsumer<T, NetworkManager.PacketContext> handler) {
        if (Platform.getEnvironment() == Env.CLIENT) {
            NetworkManager.registerReceiver(NetworkManager.s2c(), id, (buf, context) -> handler.accept(reader.apply(buf), context));
        }
    }

    public static void sendToServer(CozyPacket packet) {
        NetworkManager.sendToServer(packet.id(), encode(packet));
    }

    public static void sendToPlayer(ServerPlayer player, CozyPacket packet) {
        if (isConnected(player)) {
            NetworkManager.sendToPlayer(player, packet.id(), encode(packet));
        }
    }

    /** Clients without the mod cannot decode the packet (and Forge throws if we try). */
    public static boolean canReceive(ServerPlayer player, ResourceLocation id) {
        return isConnected(player) && NetworkManager.canPlayerReceive(player, id);
    }

    /** GameTest mock players may have a connection without a channel; Forge throws when a packet is sent to them. */
    private static boolean isConnected(ServerPlayer player) {
        return player.connection != null
                && ((ServerGamePacketListenerAccessor) player.connection).cozychristmas$connection().isConnected();
    }

    /** The packet, encoded once, to send to several players. */
    @SuppressWarnings("unchecked")
    public static Packet<ClientGamePacketListener> toClientPacket(CozyPacket packet) {
        return (Packet<ClientGamePacketListener>) NetworkManager.toPacket(NetworkManager.s2c(), packet.id(), encode(packet));
    }

    private static FriendlyByteBuf encode(CozyPacket packet) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        packet.write(buf);
        return buf;
    }
}
