package dev.eliasnvx.cozychristmas.mixin;

import dev.eliasnvx.cozychristmas.cosmetic.CosmeticsManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Worn cosmetics drop on death where the inventory does, right after it: so only when the player's death drops
 * anything (not for spectators, not with keepInventory), after any mod that cancels the death had its say, inside
 * Forge's drop capture (grave mods collect them like the rest), and with Curse of Vanishing destroyed like armor.
 */
@Mixin(Player.class)
public abstract class PlayerMixin {

    @Inject(method = "dropEquipment", at = @At("TAIL"))
    private void cozychristmas$dropCosmetics(CallbackInfo ci) {
        if (!((Object) this instanceof ServerPlayer player)
                || player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
            return; // with keepInventory the stored cosmetics are copied to the respawned player
        }
        for (ItemStack stack : CosmeticsManager.clear(player)) {
            if (!EnchantmentHelper.hasVanishingCurse(stack)) {
                player.spawnAtLocation(stack);
            }
        }
    }
}
