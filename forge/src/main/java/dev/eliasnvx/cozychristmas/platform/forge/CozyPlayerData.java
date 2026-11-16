package dev.eliasnvx.cozychristmas.platform.forge;

import com.mojang.serialization.Codec;
import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.cosmetic.CosmeticInventory;
import dev.eliasnvx.cozychristmas.karma.Karma;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The mod's data on a player (karma and cosmetics), as a Forge capability: Forge 47 has no data attachments. Saved with
 * the player; copied to the new player on respawn and on return from the End, like the later versions' copyOnDeath
 * attachments. Adapted from Catgirl & Catboy Mod.
 */
public final class CozyPlayerData {

    /** Capability id on the player ({@code cozychristmas:player_data}). */
    public static final ResourceLocation ID = CozyChristmas.id("player_data");

    public static final Capability<CozyPlayerData> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });

    private static final String KARMA_KEY = "karma";
    private static final String COSMETICS_KEY = "cosmetics";

    Karma karma = Karma.ZERO;
    CosmeticInventory cosmetics = CosmeticInventory.EMPTY;

    void copyFrom(CozyPlayerData old) {
        karma = old.karma;
        cosmetics = old.cosmetics;
    }

    CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        write(tag, KARMA_KEY, Karma.CODEC, karma);
        write(tag, COSMETICS_KEY, CosmeticInventory.CODEC, cosmetics);
        return tag;
    }

    void load(CompoundTag tag) {
        karma = read(tag, KARMA_KEY, Karma.CODEC, Karma.ZERO);
        cosmetics = read(tag, COSMETICS_KEY, CosmeticInventory.CODEC, CosmeticInventory.EMPTY);
    }

    private static <T> void write(CompoundTag tag, String key, Codec<T> codec, T value) {
        codec.encodeStart(NbtOps.INSTANCE, value)
                .resultOrPartial(error -> CozyChristmas.LOGGER.error("Failed to save player {}: {}", key, error))
                .ifPresent(encoded -> tag.put(key, encoded));
    }

    private static <T> T read(CompoundTag tag, String key, Codec<T> codec, T fallback) {
        return !tag.contains(key) ? fallback
                : codec.parse(NbtOps.INSTANCE, tag.get(key))
                        .resultOrPartial(error -> CozyChristmas.LOGGER.error("Failed to load player {}: {}", key, error))
                        .orElse(fallback);
    }

    /** Attached to every player (both logical sides); saved with the player NBT. */
    static final class Provider implements ICapabilitySerializable<CompoundTag> {

        private final CozyPlayerData data = new CozyPlayerData();
        // Not invalidated on removal: PlayerEvent.Clone revives the old player's caps to read them
        private final LazyOptional<CozyPlayerData> optional = LazyOptional.of(() -> data);

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            return CAPABILITY.orEmpty(cap, optional);
        }

        @Override
        public CompoundTag serializeNBT() {
            return data.save();
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            data.load(tag);
        }
    }
}
