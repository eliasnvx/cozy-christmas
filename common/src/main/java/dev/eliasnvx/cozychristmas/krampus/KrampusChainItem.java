package dev.eliasnvx.cozychristmas.krampus;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.phys.Vec3;

/**
 * Krampus's chain (SPEC: his drop). A slow iron weapon that reaches further than a sword (an entity interaction range
 * bonus while held, see {@code CozyItems}) and, like his lash, drags what it hits a little closer. Like an iron sword it
 * wears by one per hit, enchants and is repaired with iron (26.3's {@code Item.Properties#sword(IRON)}).
 */
public class KrampusChainItem extends Item {

    private static final double PULL = 0.45;
    private static final double PULL_UP = 0.1;

    public KrampusChainItem(Properties properties) {
        super(properties);
    }

    /** A hit counts as using it (stats) and wears it in {@link #postHurtEnemy}, like a sword. */
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        return true;
    }

    @Override
    public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.postHurtEnemy(stack, target, attacker);
        stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);
        Vec3 toward = new Vec3(attacker.getX() - target.getX(), 0.0, attacker.getZ() - target.getZ());
        if (toward.lengthSqr() < 1.0E-4) {
            return;
        }
        toward = toward.normalize();
        double resist = 1.0 - target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
        target.setDeltaMovement(target.getDeltaMovement().add(toward.x * PULL * resist, PULL_UP * resist, toward.z * PULL * resist));
        target.hurtMarked = true;
    }

    @Override
    public int getEnchantmentValue() {
        return Tiers.IRON.getEnchantmentValue();
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repair) {
        return Tiers.IRON.getRepairIngredient().test(repair) || super.isValidRepairItem(stack, repair);
    }
}
