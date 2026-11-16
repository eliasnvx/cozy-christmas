package dev.eliasnvx.cozychristmas.krampus;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * Krampus's chain (SPEC: his drop). A slow iron weapon that reaches further than a sword (the item's
 * {@code attack_range}) and, like his lash, drags what it hits a little closer.
 */
public class KrampusChainItem extends Item {

    private static final double PULL = 0.45;
    private static final double PULL_UP = 0.1;

    public KrampusChainItem(Properties properties) {
        super(properties);
    }

    @Override
    public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.postHurtEnemy(stack, target, attacker);
        Vec3 toward = attacker.position().subtract(target.position()).horizontal();
        if (toward.lengthSqr() < 1.0E-4) {
            return;
        }
        toward = toward.normalize();
        double resist = 1.0 - target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
        target.setDeltaMovement(target.getDeltaMovement().add(toward.x * PULL * resist, PULL_UP * resist, toward.z * PULL * resist));
        target.syncVelocity = true;
    }
}
