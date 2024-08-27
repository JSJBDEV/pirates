package ace.actually.pirates.entities;

import ace.actually.pirates.Pirates;
import ace.actually.pirates.blocks.MotionInvokingBlockEntity;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Quaternionf;

import java.util.Objects;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class MotionInvokingBERenderer  implements BlockEntityRenderer<MotionInvokingBlockEntity> {

    public MotionInvokingBERenderer(BlockEntityRendererFactory.Context context){}



    @Override
    public void render(MotionInvokingBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        BlockRenderManager blockRenderManager = MinecraftClient.getInstance().getBlockRenderManager();

        float worldTime = Objects.requireNonNull(entity.getWorld()).getTime();


        matrices.push();

        matrices.translate(0.5, 0.25, 0.5);
        matrices.scale(0.8f, 0.8f, 0.8f);

        double rotationValue = worldTime / 360;
        matrices.multiply(new Quaternionf(cos(rotationValue), 0, sin(rotationValue), 0));
        matrices.multiply(new Quaternionf(0, 0, 1, 0));

        matrices.translate(-0.5, 0, -0.5);

        blockRenderManager.renderBlockAsEntity(Pirates.CAPTAIN_HEAD_BLOCK.getDefaultState(),  matrices, vertexConsumers, light, overlay);

        matrices.pop();


    }


}
