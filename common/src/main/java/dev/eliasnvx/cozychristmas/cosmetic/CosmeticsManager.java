package dev.eliasnvx.cozychristmas.cosmetic;

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
import java.util.List;

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
