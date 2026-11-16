package dev.eliasnvx.cozychristmas.present;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * What's inside a present: the {@code cozychristmas:present_contents} item data (stack NBT) and the block entity's
 * data, so it survives breaking and placing the present again. Immutable. Only saved, never synced on its own: the
 * client sees it in the item's NBT.
 * <ul>
 *     <li>{@link Surprise}: found in the world; karma decides loot, coal or boom ({@link #decided} once someone
 *     came close)</li>
 *     <li>{@link Gift}: items a player wrapped at the wrapping table</li>
 *     <li>{@link Prank}: TNT a player wrapped: a boom with a name tag on it</li>
 * </ul>
 */
public sealed interface PresentContents permits PresentContents.Surprise, PresentContents.Gift, PresentContents.Prank {

    Surprise SURPRISE = new Surprise(Optional.empty());

    Codec<PresentContents> CODEC = Kind.CODEC.dispatch("type", PresentContents::kind, Kind::codec);

    Kind kind();

    /** Whether it goes boom when opened, and ticks while someone stands close. */
    default boolean isBoom() {
        return false;
    }

    /** Who wrapped it, shown on the tooltip ("From Steve"); empty for presents found in the world. */
    default Optional<Giver> from() {
        return Optional.empty();
    }

    record Surprise(Optional<PresentOutcome> decided) implements PresentContents {

        static final MapCodec<Surprise> MAP_CODEC = PresentOutcome.CODEC.optionalFieldOf("decided")
                .xmap(Surprise::new, Surprise::decided);

        public Surprise(PresentOutcome outcome) {
            this(Optional.of(outcome));
        }

        @Override
        public Kind kind() {
            return Kind.SURPRISE;
        }

        @Override
        public boolean isBoom() {
            return decided.orElse(null) == PresentOutcome.BOOM;
        }
    }

    /**
     * The wrapped stack. {@link ItemStack} is mutable and compares by identity, so the record keeps its own copy,
     * never hands it out for changes ({@link #create()} copies) and compares by item, count and NBT like
     * vanilla's container contents: the item data must stay equal after a round trip through the network or a save.
     */
    record Gift(ItemStack item, Optional<Giver> from) implements PresentContents {

        /** An empty gift can't be loaded (1.20.1's {@link ItemStack#CODEC} reads air and a count of 0 as empty). */
        private static final Codec<ItemStack> NON_EMPTY_STACK = ExtraCodecs.validate(ItemStack.CODEC, stack -> stack.isEmpty()
                ? DataResult.error(() -> "A gift can't be empty")
                : DataResult.success(stack));

        static final MapCodec<Gift> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                NON_EMPTY_STACK.fieldOf("item").forGetter(Gift::item),
                Giver.CODEC.optionalFieldOf("from").forGetter(Gift::from)
        ).apply(i, Gift::new));

        public Gift {
            if (item.isEmpty()) {
                throw new IllegalArgumentException("A gift can't be empty");
            }
            item = item.copy();
        }

        /** A fresh copy of the wrapped stack. */
        public ItemStack create() {
            return item.copy();
        }

        @Override
        public Kind kind() {
            return Kind.GIFT;
        }

        @Override
        public boolean equals(Object other) {
            return this == other || other instanceof Gift gift && ItemStack.matches(item, gift.item) && from.equals(gift.from);
        }

        @Override
        public int hashCode() {
            return 31 * (31 * (31 * item.getItem().hashCode() + Objects.hashCode(item.getTag())) + item.getCount())
                    + from.hashCode();
        }
    }

    record Prank(Optional<Giver> from) implements PresentContents {

        static final MapCodec<Prank> MAP_CODEC = Giver.CODEC.optionalFieldOf("from").xmap(Prank::new, Prank::from);

        @Override
        public Kind kind() {
            return Kind.PRANK;
        }

        @Override
        public boolean isBoom() {
            return true;
        }
    }

    /** The player who wrapped a present; the name is kept for the tooltip, so it shows while they're offline. */
    record Giver(UUID id, String name) {

        /** Vanilla's limit for player names. */
        private static final int MAX_NAME_LENGTH = 16;

        public static final Codec<Giver> CODEC = RecordCodecBuilder.create(i -> i.group(
                UUIDUtil.CODEC.fieldOf("id").forGetter(Giver::id),
                ExtraCodecs.sizeLimitedString(0, MAX_NAME_LENGTH).fieldOf("name").forGetter(Giver::name)
        ).apply(i, Giver::new));

        public static Giver of(Player player) {
            return new Giver(player.getUUID(), player.getScoreboardName());
        }
    }

    /**
     * The {@code type} field. Codecs are looked up when used: the records' static fields may not be initialized yet
     * while this interface is (a record's class init initializes this interface first).
     */
    enum Kind implements StringRepresentable {
        SURPRISE(() -> Surprise.MAP_CODEC),
        GIFT(() -> Gift.MAP_CODEC),
        PRANK(() -> Prank.MAP_CODEC);

        static final Codec<Kind> CODEC = StringRepresentable.fromEnum(Kind::values);

        private final Supplier<MapCodec<? extends PresentContents>> codec;
        private final String name = name().toLowerCase(Locale.ROOT);

        Kind(Supplier<MapCodec<? extends PresentContents>> codec) {
            this.codec = codec;
        }

        /** DFU 6 dispatches to codecs; a map codec's own codec keeps its fields next to {@code type}. */
        Codec<? extends PresentContents> codec() {
            return codec.get().codec();
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
