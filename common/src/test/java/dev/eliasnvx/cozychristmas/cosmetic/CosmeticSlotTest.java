package dev.eliasnvx.cozychristmas.cosmetic;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class CosmeticSlotTest {

    /** A client can send any id in a creative slot packet: an unknown one mustn't throw while decoding. */
    @Test
    void anUnknownSlotIdDecodesSafely() {
        for (int id : new int[]{-1, 99}) {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeVarInt(id);
            assertDoesNotThrow(() -> CosmeticSlot.STREAM_CODEC.decode(buf));
        }
    }
}
