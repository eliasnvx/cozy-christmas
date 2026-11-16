package dev.eliasnvx.cozychristmas.food;

import dev.architectury.event.events.common.LootEvent;
import dev.eliasnvx.cozychristmas.config.CozyConfig;
import dev.eliasnvx.cozychristmas.registry.CozyItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;

/**
 * Chestnuts fall from the leaves in {@code food.chestnut_leaves} (dark oak by default), like apples from oak leaves:
 * a pool added to each one's built-in loot table. Unlike apples they don't care about shears or fortune: a nut just
 * drops out. Roasted on a campfire (data recipe) they become roasted chestnuts.
 */
public final class Chestnuts {

    private Chestnuts() {
    }

    public static void register() {
        LootEvent.MODIFY_LOOT_TABLE.register((lootData, id, context, builtin) -> {
            FoodSettings settings = CozyConfig.common().food();
            if (!builtin || settings.chestnutChance() <= 0.0F) {
                return;
            }
            for (ResourceLocation leaves : settings.chestnutLeaves()) {
                boolean match = BuiltInRegistries.BLOCK.getOptional(leaves).map(Block::getLootTable)
                        .filter(id::equals).isPresent();
                if (match) {
                    context.addPool(LootPool.lootPool()
                            .add(LootItem.lootTableItem(CozyItems.CHESTNUTS.get()))
                            .when(ExplosionCondition.survivesExplosion())
                            .when(LootItemRandomChanceCondition.randomChance(settings.chestnutChance())));
                    return;
                }
            }
        });
    }
}
