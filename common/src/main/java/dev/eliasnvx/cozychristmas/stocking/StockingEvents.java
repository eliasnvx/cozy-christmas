package dev.eliasnvx.cozychristmas.stocking;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.InteractionEvent;
import dev.eliasnvx.cozychristmas.registry.CozyBlocks;
import dev.eliasnvx.cozychristmas.registry.CozyTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Hanging other mods' socks ({@code #cozychristmas:hangs_as_stocking}): sneak-used on the side of a block, the sock
 * becomes a stocking there (the striped look), and comes back when it's taken down. Sneaking only, so a plain click
 * still wears them (Femboy Mod). SPEC: Femboy Mod's socks hang over the fireplace.
 */
public final class StockingEvents {

    private StockingEvents() {
    }

    public static void register() {
        InteractionEvent.RIGHT_CLICK_BLOCK.register(StockingEvents::hangSock);
    }

    private static EventResult hangSock(Player player, InteractionHand hand, BlockPos clicked, Direction face) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown() || !stack.is(CozyTags.HANGS_AS_STOCKING) || !face.getAxis().isHorizontal()) {
            return EventResult.pass();
        }
        Level level = player.level();
        BlockPos pos = clicked.relative(face);
        BlockState state = CozyBlocks.STOCKING.get().defaultBlockState().setValue(StockingBlock.FACING, face)
                .setValue(StockingBlock.STYLE, StockingStyle.SOCK);
        if (!level.getBlockState(pos).canBeReplaced() || !state.canSurvive(level, pos) || !player.mayBuild()) {
            return EventResult.pass();
        }
        if (!level.isClientSide()) {
            level.setBlock(pos, state, Block.UPDATE_ALL);
            if (level.getBlockEntity(pos) instanceof StockingBlockEntity stocking) {
                stocking.hang(player, stack, StockingBlockEntity.morning(level));
            }
            level.playSound(null, pos, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return EventResult.interruptTrue(); // a success: the client swings the hand
    }
}
