package dev.eliasnvx.cozychristmas.cosmetic;

import com.mojang.serialization.DataResult;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CosmeticSlotTest {

    /**
     * A client can send any item NBT in a creative slot packet (1.20.1 has no item components: the cosmetic slot travels
     * as the item's {@code cozychristmas:cosmetic} NBT): an unknown or malformed slot mustn't throw while decoding.
     */
    @Test
    void anUnknownSlotDecodesSafely() {
        CompoundTag unknownName = new CompoundTag();
        unknownName.putString("slot", "tail");
        CompoundTag numeric = new CompoundTag();
        numeric.putInt("slot", 99);
        for (CompoundTag tag : new CompoundTag[]{unknownName, numeric, new CompoundTag()}) {
            AtomicReference<DataResult<Cosmetic>> result = new AtomicReference<>();
            assertDoesNotThrow(() -> result.set(Cosmetic.CODEC.parse(NbtOps.INSTANCE, tag)));
            assertTrue(result.get().error().isPresent(), "an error, not a slot: " + tag);
        }
    }

    @Test
    void aKnownSlotDecodes() {
        CompoundTag tag = new CompoundTag();
        tag.putString("slot", CosmeticSlot.NECK.getSerializedName());
        assertEquals(new Cosmetic(CosmeticSlot.NECK), Cosmetic.CODEC.parse(NbtOps.INSTANCE, tag).getOrThrow(false, e -> { }));
    }
}
