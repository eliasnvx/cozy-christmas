package dev.eliasnvx.cozychristmas.compat.jade;

import dev.eliasnvx.cozychristmas.present.PresentBlockEntity;
import dev.eliasnvx.cozychristmas.stocking.StockingBlockEntity;
import dev.eliasnvx.cozychristmas.tree.TreeStarBlock;
import dev.eliasnvx.cozychristmas.tree.TreeStarBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

import java.util.Optional;

/**
 * What the look-at tooltip needs from the server: block entities aren't synced to clients, so names and the tree's
 * status are sent with the tooltip request, in Jade's server data tag (Jade 11 on 1.20.1 has no stream providers).
 * Karma never is (it stays hidden, as everywhere else).
 */
final class LookInfo {

    private static final String OWNER = "owner";
    private static final String STATUS = "status";
    private static final String FROM = "from";

    private LookInfo() {
    }

    /** Whose stocking it is; empty for one hung before names were kept. */
    record Stocking(String owner) {

        static Stocking of(StockingBlockEntity stocking) {
            return new Stocking(stocking.ownerName());
        }

        CompoundTag write() {
            CompoundTag tag = new CompoundTag();
            tag.putString(OWNER, owner);
            return tag;
        }

        static Stocking read(CompoundTag tag) {
            return new Stocking(tag.getString(OWNER));
        }
    }

    /** Whose tree it is (empty: a structure's) and what its star would tell the owner. */
    record Tree(String owner, Component status) {

        static Tree of(ServerLevel level, BlockPos pos, TreeStarBlockEntity star) {
            return new Tree(star.owner() == null ? "" : star.ownerName(), TreeStarBlock.statusMessage(level, pos));
        }

        CompoundTag write() {
            CompoundTag tag = new CompoundTag();
            tag.putString(OWNER, owner);
            tag.putString(STATUS, Component.Serializer.toJson(status));
            return tag;
        }

        static Tree read(CompoundTag tag) {
            Component status = tag.contains(STATUS, Tag.TAG_STRING) ? Component.Serializer.fromJson(tag.getString(STATUS)) : null;
            return new Tree(tag.getString(OWNER), status == null ? Component.empty() : status);
        }
    }

    /** Who wrapped the present, as its item tooltip says; never what's inside. */
    record Present(Optional<String> from) {

        static Present of(PresentBlockEntity present) {
            return new Present(present.contents().from().map(giver -> giver.name()));
        }

        CompoundTag write() {
            CompoundTag tag = new CompoundTag();
            from.ifPresent(name -> tag.putString(FROM, name));
            return tag;
        }

        static Present read(CompoundTag tag) {
            return new Present(tag.contains(FROM, Tag.TAG_STRING) ? Optional.of(tag.getString(FROM)) : Optional.empty());
        }
    }
}
