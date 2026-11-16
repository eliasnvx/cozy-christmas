package dev.eliasnvx.cozychristmas.snowman;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Thrown like a snowball ({@link net.minecraft.world.item.SnowballItem}). Dispensers fire it too: 1.20.1 has no
 * {@code ProjectileItem}, the dispenser behavior is registered on setup ({@code CozyChristmas}).
 */
public class IceSnowballItem extends Item {

    private static final float POWER = 1.5F;
    private static final float INACCURACY = 1.0F;

    public IceSnowballItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL,
                0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
        if (!level.isClientSide()) {
            IceSnowball ball = new IceSnowball(level, player, stack);
            ball.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, POWER, INACCURACY);
            level.addFreshEntity(ball);
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
