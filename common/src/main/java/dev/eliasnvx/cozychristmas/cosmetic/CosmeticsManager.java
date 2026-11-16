package dev.eliasnvx.cozychristmas.cosmetic;

import dev.eliasnvx.cozychristmas.compat.ExternalSlots;
import dev.eliasnvx.cozychristmas.platform.PlatformHelper;
import dev.eliasnvx.cozychristmas.registry.CozyComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/** Server-authoritative access to cosmetic slots. The client only mirrors synced state. */
public final class CosmeticsManager {

    private static final float EQUIP_SOUND_VOLUME = 1.0F;
    private static final float EQUIP_SOUND_PITCH = 1.0F;

    private CosmeticsManager() {
    }

    /** What the entity wears; empty for anything but a player. */
    public static CosmeticInventory get(LivingEntity entity) {
        return entity instanceof Player player ? PlatformHelper.getCosmetics(player) : CosmeticInventory.EMPTY;
    }

    /** One player's merge for one tick: the other mods' stacks seen and the two views built from them. */
    private record WornCache(int tick, CosmeticInventory own, Map<CosmeticSlot, ItemStack> worn,
                             Map<CosmeticSlot, ItemStack> looks, CosmeticInventory wornMerged, CosmeticInventory looksMerged) {
    }

    private static final Map<Player, WornCache> WORN = new WeakHashMap<>();

    /**
     * The outfit as it counts (set bonuses): {@link #get} plus pieces worn in other mods' slots ({@link ExternalSlots},
     * e.g. Curios), hidden ones included, for slots that are empty here. Edit the outfit with {@link #get}/{@link #set}.
     * Evaluated at most once per tick per player, and the same instance while nothing changes. Ported from Catgirl &
     * Catboy Mod.
     */
    public static CosmeticInventory worn(LivingEntity entity) {
        return merge(entity, false);
    }

    /**
     * The outfit as it is drawn: {@link #get}, then for slots empty here a piece in another mod's cosmetic-only slot,
     * else one worn there with its show toggle on. Same evaluation as {@link #worn}.
     */
    public static CosmeticInventory looks(LivingEntity entity) {
        return merge(entity, true);
    }

    private static synchronized CosmeticInventory merge(LivingEntity entity, boolean forLooks) {
        CosmeticInventory own = get(entity);
        if (!(entity instanceof Player player) || !ExternalSlots.any()) {
            return own;
        }
        WornCache cached = WORN.get(player);
        if (cached == null || cached.tick() != player.tickCount || cached.own() != own) {
            cached = rebuild(player, own, cached);
            WORN.put(player, cached);
        }
        return forLooks ? cached.looksMerged() : cached.wornMerged();
    }

    private static WornCache rebuild(Player player, CosmeticInventory own, @Nullable WornCache before) {
        Map<CosmeticSlot, ItemStack> worn = new EnumMap<>(CosmeticSlot.class);
        Map<CosmeticSlot, ItemStack> shown = new EnumMap<>(CosmeticSlot.class);
        Map<CosmeticSlot, ItemStack> lookOnly = new EnumMap<>(CosmeticSlot.class);
        ExternalSlots.forEach(player, (stack, role) -> {
            CosmeticSlot slot = stack.isEmpty() ? null : slotOf(stack);
            if (slot == null || !own.get(slot).isEmpty()) {
                return;
            }
            switch (role) {
                case WORN -> {
                    worn.putIfAbsent(slot, stack);
                    shown.putIfAbsent(slot, stack);
                }
                case HIDDEN -> worn.putIfAbsent(slot, stack);
                case LOOK -> lookOnly.putIfAbsent(slot, stack);
            }
        });
        Map<CosmeticSlot, ItemStack> looks = new EnumMap<>(shown);
        looks.putAll(lookOnly); // a cosmetic-only slot is drawn over what's worn there
        boolean same = before != null && before.own() == own;
        CosmeticInventory wornMerged = same && unchanged(before.wornMerged(), before.worn(), worn)
                ? before.wornMerged() : with(own, worn);
        CosmeticInventory looksMerged = same && unchanged(before.looksMerged(), before.looks(), looks)
                ? before.looksMerged() : with(own, looks);
        return new WornCache(player.tickCount, own, worn, looks, wornMerged, looksMerged);
    }

    private static CosmeticInventory with(CosmeticInventory own, Map<CosmeticSlot, ItemStack> extra) {
        CosmeticInventory merged = own;
        for (Map.Entry<CosmeticSlot, ItemStack> entry : extra.entrySet()) {
            merged = merged.with(entry.getKey(), entry.getValue().copy());
        }
        return merged;
    }

    /** Whether the other mods' slots still hold what a cached merge copied; by value, their stacks change in place. */
    private static boolean unchanged(CosmeticInventory merged, Map<CosmeticSlot, ItemStack> before,
                                     Map<CosmeticSlot, ItemStack> now) {
        if (!before.keySet().equals(now.keySet())) {
            return false;
        }
        for (Map.Entry<CosmeticSlot, ItemStack> entry : now.entrySet()) {
            if (!ItemStack.matches(merged.get(entry.getKey()), entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    /** The slot an item is worn in, or null if it is not a cosmetic. */
    public static @Nullable CosmeticSlot slotOf(ItemStack stack) {
        Cosmetic cosmetic = CozyComponents.COSMETIC.get(stack);
        return cosmetic == null ? null : cosmetic.slot();
    }

    /** Side-effect free check used by menu slots (both sides) and right-click equip. */
    public static boolean canEquip(CosmeticSlot slot, ItemStack stack) {
        return !stack.isEmpty() && slot == slotOf(stack);
    }

    /**
     * Puts {@code stack} into the slot (ownership passes to the slot; pass a copy if you keep it).
     * On the server this syncs to the player and everyone tracking them.
     */
    public static void set(Player player, CosmeticSlot slot, ItemStack stack) {
        CosmeticInventory before = PlatformHelper.getCosmetics(player);
        ItemStack previous = before.get(slot);
        PlatformHelper.setCosmetics(player, before.with(slot, stack));

        if (player instanceof ServerPlayer serverPlayer) {
            if (!stack.isEmpty() && !ItemStack.matches(previous, stack)) {
                // heard by the wearer and players nearby, like putting on leather armor
                player.level().playSound(null, player, SoundEvents.ARMOR_EQUIP_LEATHER, SoundSource.PLAYERS,
                        EQUIP_SOUND_VOLUME, EQUIP_SOUND_PITCH);
            }
            CosmeticsSyncPayload.sendToTrackingAndSelf(serverPlayer);
        }
    }

    /** Server: empties all slots and returns what was worn (death drops). */
    public static List<ItemStack> clear(ServerPlayer player) {
        List<ItemStack> removed = new ArrayList<>();
        CosmeticInventory worn = get(player);
        for (CosmeticSlot slot : CosmeticSlot.values()) {
            ItemStack stack = worn.get(slot);
            if (!stack.isEmpty()) {
                removed.add(stack);
                set(player, slot, ItemStack.EMPTY);
            }
        }
        return removed;
    }
}
