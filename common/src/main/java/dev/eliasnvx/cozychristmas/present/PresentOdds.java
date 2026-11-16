package dev.eliasnvx.cozychristmas.present;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;

/** Relative weights of the {@link PresentOutcome}s for one karma tier (SPEC's percentages, so they sum to 100). */
public record PresentOdds(int loot, int coal, int boom) {

    private static final Codec<Integer> WEIGHT = Codec.intRange(0, 10000);

    public static final Codec<PresentOdds> CODEC = ExtraCodecs.validate(RecordCodecBuilder.<PresentOdds>create(i -> i.group(
            WEIGHT.fieldOf("loot").forGetter(PresentOdds::loot),
            WEIGHT.fieldOf("coal").forGetter(PresentOdds::coal),
            WEIGHT.fieldOf("boom").forGetter(PresentOdds::boom)
    ).apply(i, PresentOdds::new)), odds -> odds.total() > 0
            ? DataResult.success(odds)
            : DataResult.error(() -> "Present odds need at least one weight above 0"));

    public int total() {
        return loot + coal + boom;
    }

    public PresentOutcome roll(RandomSource random) {
        return pick(random.nextInt(total()));
    }

    /** The outcome at {@code ticket} in {@code [0, total())}: loot first, then coal, then boom. */
    public PresentOutcome pick(int ticket) {
        if (ticket < 0 || ticket >= total()) {
            throw new IllegalArgumentException("Ticket " + ticket + " outside [0, " + total() + ")");
        }
        if (ticket < loot) {
            return PresentOutcome.LOOT;
        }
        return ticket < loot + coal ? PresentOutcome.COAL : PresentOutcome.BOOM;
    }
}
