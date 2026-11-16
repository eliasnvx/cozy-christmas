package dev.eliasnvx.cozychristmas.krampus;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import dev.eliasnvx.cozychristmas.registry.CozyItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Krampus's chain (SPEC: his drop). A slow iron weapon that reaches further than a sword and, like his lash, drags
 * what it hits a little closer. Like an iron sword it wears by one per hit, cuts cobwebs, takes sword enchantments and
 * is repaired with iron (26.3's {@code Item.Properties#sword(IRON)}): on 1.20.1 an iron {@link SwordItem} with
 * {@link CozyItems#CHAIN_DAMAGE} and {@link CozyItems#CHAIN_SPEED}.
 *
 * <p>The reach bonus ({@link CozyItems#CHAIN_REACH_BONUS}, 26.3's entity interaction range) needs an entity reach
 * attribute, which vanilla 1.20.1 doesn't have: it's Forge's {@code forge:entity_reach} where that exists (looked up by
 * id, so nothing of Forge is needed to compile), and none on Fabric.
 */
public class KrampusChainItem extends SwordItem {

    private static final double PULL = 0.45;
    private static final double PULL_UP = 0.1;
    private static final ResourceLocation ENTITY_REACH = new ResourceLocation("forge", "entity_reach");
    private static final UUID REACH_MODIFIER = UUID.nameUUIDFromBytes("cozychristmas:krampus_chain_reach"
            .getBytes(StandardCharsets.UTF_8));

    /** The sword's modifiers plus the reach bonus, once the attribute registry can be asked. */
    private @Nullable Multimap<Attribute, AttributeModifier> mainHandModifiers;

    public KrampusChainItem(Properties properties) {
        super(Tiers.IRON, CozyItems.CHAIN_DAMAGE, CozyItems.CHAIN_SPEED, properties);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        if (slot != EquipmentSlot.MAINHAND) {
            return super.getDefaultAttributeModifiers(slot);
        }
        if (mainHandModifiers == null) {
            ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
            builder.putAll(super.getDefaultAttributeModifiers(slot));
            Attribute reach = BuiltInRegistries.ATTRIBUTE.get(ENTITY_REACH);
            if (reach != null) {
                builder.put(reach, new AttributeModifier(REACH_MODIFIER, "Krampus chain reach", CozyItems.CHAIN_REACH_BONUS,
                        AttributeModifier.Operation.ADDITION));
            }
            mainHandModifiers = builder.build();
        }
        return mainHandModifiers;
    }

    /** Wears like a sword (and counts as a use for stats), and drags the target in. */
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.hurtEnemy(stack, target, attacker);
        Vec3 toward = new Vec3(attacker.getX() - target.getX(), 0.0, attacker.getZ() - target.getZ());
        if (toward.lengthSqr() < 1.0E-4) {
            return true;
        }
        toward = toward.normalize();
        double resist = 1.0 - target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
        target.setDeltaMovement(target.getDeltaMovement().add(toward.x * PULL * resist, PULL_UP * resist, toward.z * PULL * resist));
        target.hurtMarked = true;
        return true;
    }
}
