package dev.eliasnvx.cozychristmas.client.present;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.eliasnvx.cozychristmas.present.PresentBlock;
import dev.eliasnvx.cozychristmas.present.PresentBlockEntity;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Draws a ticking present shaking: while {@link PresentBlock#TICKING} is on, its block model is empty and this draws
 * the still model (the same state with ticking off) with a small jitter and wobble, the way vanilla draws blocks a
 * piston moves. Quiet presents are plain chunk geometry and cost nothing here.
 */
public class PresentRenderer implements BlockEntityRenderer<PresentBlockEntity, PresentRenderer.State> {

    /** Sideways jitter in blocks and wobble in degrees: enough to notice from a few blocks, never a blur. */
    private static final float JITTER = 0.025F;
    private static final float WOBBLE_DEGREES = 3.0F;

    public PresentRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(PresentBlockEntity present, State state, float partialTicks, Vec3 cameraPosition,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(present, state, partialTicks, cameraPosition, breakProgress);
        BlockState blockState = present.getBlockState();
        state.ticking = blockState.getValue(PresentBlock.TICKING) && present.getLevel() instanceof ClientLevel;
        if (!state.ticking) {
            return;
        }
        ClientLevel level = (ClientLevel) present.getLevel();
        MovingBlockRenderState block = state.block;
        block.blockState = blockState.setValue(PresentBlock.TICKING, false);
        block.blockPos = present.getBlockPos();
        block.randomSeedPos = present.getBlockPos();
        block.biome = level.getBiome(present.getBlockPos());
        block.cardinalLighting = level.cardinalLighting();
        block.lightEngine = level.getLightEngine();
        // each present shakes on its own beat
        float time = level.getGameTime() + partialTicks + (present.getBlockPos().hashCode() & 0xFF);
        state.offsetX = Mth.sin(time * 2.9F) * JITTER;
        state.offsetZ = Mth.cos(time * 3.7F) * JITTER;
        state.wobble = Mth.sin(time * 4.3F) * WOBBLE_DEGREES;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!state.ticking) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.5F + state.offsetX, 0.0F, 0.5F + state.offsetZ);
        poseStack.rotateDegrees(Axis.ZP, state.wobble);
        poseStack.translate(-0.5F, 0.0F, -0.5F);
        collector.submitMovingBlock(poseStack, state.block, 0);
        poseStack.popPose();
    }

    public static final class State extends BlockEntityRenderState {
        final MovingBlockRenderState block = new MovingBlockRenderState();
        boolean ticking;
        float offsetX;
        float offsetZ;
        float wobble;
    }
}
