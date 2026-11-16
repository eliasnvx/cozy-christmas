package dev.eliasnvx.cozychristmas.compat.jade;

import dev.eliasnvx.cozychristmas.present.PresentBlockEntity;
import dev.eliasnvx.cozychristmas.stocking.StockingBlockEntity;
import dev.eliasnvx.cozychristmas.tree.TreeStarBlock;
import dev.eliasnvx.cozychristmas.tree.TreeStarBlockEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;

import java.util.Optional;

/**
 * What the look-at tooltip needs from the server: block entities aren't synced to clients, so names and the tree's
 * status are sent with the tooltip request. Karma never is (it stays hidden, as everywhere else).
 */
final class LookInfo {

    private LookInfo() {
    }

    /** Whose stocking it is; empty for one hung before names were kept. */
    record Stocking(String owner) {

        static final StreamCodec<ByteBuf, Stocking> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(Stocking::new, Stocking::owner);

        static Stocking of(StockingBlockEntity stocking) {
            return new Stocking(stocking.ownerName());
        }
    }

    /** Whose tree it is (empty: a structure's) and what its star would tell the owner. */
    record Tree(String owner, Component status) {

        static final StreamCodec<RegistryFriendlyByteBuf, Tree> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, Tree::owner,
                ComponentSerialization.STREAM_CODEC, Tree::status,
                Tree::new);

        static Tree of(ServerLevel level, BlockPos pos, TreeStarBlockEntity star) {
            return new Tree(star.owner() == null ? "" : star.ownerName(), TreeStarBlock.statusMessage(level, pos));
        }
    }

    /** Who wrapped the present, as its item tooltip says; never what's inside. */
    record Present(Optional<String> from) {

        static final StreamCodec<ByteBuf, Present> STREAM_CODEC = ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8)
                .map(Present::new, Present::from);

        static Present of(PresentBlockEntity present) {
            return new Present(present.contents().from().map(giver -> giver.name()));
        }
    }
}
