package dev.eliasnvx.cozychristmas.cosmetic;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.InteractionEvent;
import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Game hooks for cosmetic slots: networking, sync, right-click equip, death drops. */
public final class CosmeticsEvents {

    private CosmeticsEvents() {
    }

    public static void register() {
        CosmeticsSyncPayload.register();
        PlayerEvent.PLAYER_JOIN.register(CosmeticsSyncPayload::sendToTrackingAndSelf);
        PlayerEvent.PLAYER_RESPAWN.register((player, conqueredEnd, reason) -> CosmeticsSyncPayload.sendToTrackingAndSelf(player));
        PlayerEvent.CHANGE_DIMENSION.register((player, from, to) -> CosmeticsSyncPayload.sendToTrackingAndSelf(player));
        EntityEvent.START_TRACKING.register((entity, watcher) -> {
            if (entity instanceof Player tracked) {
                CosmeticsSyncPayload.sendTo(watcher, tracked);
            }
        });
        InteractionEvent.USE_ITEM.register((level, player, hand) -> equipFromHand(player, hand));
    }

    /** Right-click a cosmetic in hand to wear it; swaps with whatever was in the slot. */
    private static EventResult equipFromHand(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        CosmeticSlot slot = CosmeticsManager.slotOf(held);
        if (slot == null || !CosmeticsManager.canEquip(slot, held)) {
            return EventResult.pass();
        }
        if (!(player instanceof ServerPlayer)) {
            return EventResult.fromMinecraft(InteractionResult.SUCCESS);
        }
        ItemStack worn = CosmeticsManager.get(player).get(slot).copy();
        ItemStack toWear = held.split(1);
        CosmeticsManager.set(player, slot, toWear);
        if (!worn.isEmpty()) {
            if (held.isEmpty()) {
                player.setItemInHand(hand, worn);
            } else if (!player.getInventory().add(worn)) {
                player.spawnAtLocation((ServerLevel) player.level(), worn);
            }
        }
        // Tell vanilla what the hand holds now: without it, the use code sees the emptied stack and clears the hand,
        // deleting the swapped-out cosmetic (Fabric fires this event inside Item#use).
        return EventResult.fromMinecraft(InteractionResult.SUCCESS.heldItemTransformedTo(player.getItemInHand(hand)));
    }
}
