package dev.eliasnvx.cozychristmas.client.present;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.eliasnvx.cozychristmas.present.PresentBlock;
import dev.eliasnvx.cozychristmas.present.PresentBlockEntity;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaternionf;

/**
 * Draws a ticking present shaking: while {@link PresentBlock#TICKING} is on, its block model is empty and this draws
 * the still model (the same state with ticking off) with a small jitter and wobble, the way vanilla draws blocks a
 * piston moves. Quiet presents are plain chunk geometry and cost nothing here.
 */
public class PresentRenderer implements BlockEntityRenderer<PresentBlockEntity> {

    /** Sideways jitter in blocks and wobble in degrees: enough to notice from a few blocks, never a blur. */
    private static final float JITTER = 0.025F;
    private static final float WOBBLE_DEGREES = 3.0F;

    private final BlockRenderDispatcher blockRenderer;
    /** Reused every frame: the model renderer reseeds it per quad list, the pose stack copies the rotation. */
    private final RandomSource random = RandomSource.create();
    private final Quaternionf rotation = new Quaternionf();

    public PresentRenderer(BlockEntityRendererProvider.Context context) {
        blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(PresentBlockEntity present, float partialTick, PoseStack poseStack, MultiBufferSource buffers,
                       int light, int overlay) {
        BlockState blockState = present.getBlockState();
        if (!blockState.getValue(PresentBlock.TICKING) || !(present.getLevel() instanceof ClientLevel level)) {
            return;
        }
        BlockState still = blockState.setValue(PresentBlock.TICKING, false);
        BlockPos pos = present.getBlockPos();
        // each present shakes on its own beat
        float time = level.getGameTime() + partialTick + (pos.hashCode() & 0xFF);
        float offsetX = Mth.sin(time * 2.9F) * JITTER;
        float offsetZ = Mth.cos(time * 3.7F) * JITTER;
        float wobble = Mth.sin(time * 4.3F) * WOBBLE_DEGREES;
        poseStack.pushPose();
        poseStack.translate(0.5F + offsetX, 0.0F, 0.5F + offsetZ);
        poseStack.mulPose(rotation.rotationZ(wobble * Mth.DEG_TO_RAD));
        poseStack.translate(-0.5F, 0.0F, -0.5F);
        blockRenderer.getModelRenderer().tesselateBlock(level, blockRenderer.getBlockModel(still), still, pos, poseStack,
                buffers.getBuffer(ItemBlockRenderTypes.getMovingBlockRenderType(still)), false, random,
                still.getSeed(pos), overlay);
        poseStack.popPose();
    }
}
