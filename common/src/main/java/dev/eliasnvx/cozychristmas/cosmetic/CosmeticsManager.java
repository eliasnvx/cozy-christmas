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

    private record WornCache(int tick, CosmeticInventory own, Map<CosmeticSlot, ItemStack> extra, CosmeticInventory merged) {
    }

    private static final Map<Player, WornCache> WORN = new WeakHashMap<>();

    /**
     * The outfit as it is worn: {@link #get} plus cosmetics in other mods' slots ({@link ExternalSlots}, e.g. Curios)
     * for slots that are empty here. Edit the outfit with {@link #get}/{@link #set}; read it for looks and set bonuses
     * with this. Evaluated at most once per tick per player, and the same instance while nothing changes. Ported from
     * Catgirl & Catboy Mod.
     */
    public static synchronized CosmeticInventory worn(LivingEntity entity) {
        CosmeticInventory own = get(entity);
        if (!(entity instanceof Player player) || !ExternalSlots.any()) {
            return own;
        }
        WornCache cached = WORN.get(player);
        if (cached != null && cached.tick() == player.tickCount && cached.own() == own) {
            return cached.merged();
        }
        Map<CosmeticSlot, ItemStack> extra = new EnumMap<>(CosmeticSlot.class);
        ExternalSlots.forEach(player, stack -> {
            CosmeticSlot slot = stack.isEmpty() ? null : slotOf(stack);
            if (slot != null && own.get(slot).isEmpty() && !extra.containsKey(slot)) {
                extra.put(slot, stack);
            }
        });
        CosmeticInventory merged;
        if (cached != null && cached.own() == own && unchanged(cached, extra)) {
            merged = cached.merged();
        } else {
            merged = own;
            for (Map.Entry<CosmeticSlot, ItemStack> entry : extra.entrySet()) {
                merged = merged.with(entry.getKey(), entry.getValue().copy());
            }
        }
        WORN.put(player, new WornCache(player.tickCount, own, extra, merged));
        return merged;
    }

    /** Whether the other mods' slots still hold what the cached merge copied; by value, their stacks change in place. */
    private static boolean unchanged(WornCache cached, Map<CosmeticSlot, ItemStack> extra) {
        if (!cached.extra().keySet().equals(extra.keySet())) {
            return false;
        }
        for (Map.Entry<CosmeticSlot, ItemStack> entry : extra.entrySet()) {
            if (!ItemStack.matches(cached.merged().get(entry.getKey()), entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    /** The slot an item is worn in, or null if it is not a cosmetic. */
    public static @Nullable CosmeticSlot slotOf(ItemStack stack) {
        Cosmetic cosmetic = stack.get(CozyComponents.COSMETIC.get());
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
