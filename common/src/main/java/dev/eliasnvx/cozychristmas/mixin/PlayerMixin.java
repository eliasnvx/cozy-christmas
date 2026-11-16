package dev.eliasnvx.cozychristmas.mixin;

import dev.eliasnvx.cozychristmas.cosmetic.CosmeticsManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.gamerules.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Worn cosmetics drop on death where the inventory does, right after it: so only when the player's death drops
 * anything (not for spectators, not with keepInventory), after any mod that cancels the death had its say, inside
 * NeoForge's drop capture (grave mods collect them like the rest), and with Curse of Vanishing destroyed like armor.
 */
@Mixin(Player.class)
public abstract class PlayerMixin {

    @Inject(method = "dropEquipment", at = @At("TAIL"))
    private void cozychristmas$dropCosmetics(ServerLevel level, CallbackInfo ci) {
        if (!((Object) this instanceof ServerPlayer player) || level.getGameRules().get(GameRules.KEEP_INVENTORY)) {
            return; // with keepInventory the attachment is copied to the respawned player
        }
        for (ItemStack stack : CosmeticsManager.clear(player)) {
            if (!EnchantmentHelper.has(stack, EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP)) {
                player.spawnAtLocation(level, stack);
            }
        }
    }
}
