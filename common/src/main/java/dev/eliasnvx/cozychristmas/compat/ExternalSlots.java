package dev.eliasnvx.cozychristmas.compat;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Slots of other mods (Curios now; Trinkets and Accessories once they exist for 26.x) that may hold our cosmetics.
 * Each installed slot mod registers a source from its {@code compat.<modid>} package and says what each stack is
 * there ({@link Role}); {@code CosmeticsManager} merges them into the outfit for set bonuses ({@code worn}) and for the
 * looks ({@code looks}). Ported from Catgirl & Catboy Mod, with Curios' hide toggle and cosmetic slots.
 */
public final class ExternalSlots {

    /** What a stack in another mod's slot is to us. */
    public enum Role {
        /** Worn and shown: counts for set bonuses and is drawn. */
        WORN,
        /** Worn with its slot's "show" toggle off: counts, isn't drawn. */
        HIDDEN,
        /** In a cosmetic-only slot: drawn instead of what's worn there, counts for nothing. */
        LOOK
    }

    /** Reports what a player holds in one slot mod's slots. */
    @FunctionalInterface
    public interface Visitor {
        void accept(ItemStack stack, Role role);
    }

    /** Lists every stack a player has in one slot mod's slots (cosmetic or not; empty stacks are fine). */
    @FunctionalInterface
    public interface Source {
        void forEach(Player player, Visitor visitor);
    }

    private static final List<Source> SOURCES = new CopyOnWriteArrayList<>();

    private ExternalSlots() {
    }

    public static void register(Source source) {
        SOURCES.add(source);
    }

    /** For tests. */
    public static void unregister(Source source) {
        SOURCES.remove(source);
    }

    public static boolean any() {
        return !SOURCES.isEmpty();
    }

    public static void forEach(Player player, Visitor visitor) {
        for (Source source : SOURCES) {
            source.forEach(player, visitor);
        }
    }
}
