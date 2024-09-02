package ace.actually.pirates.blocks;

import ace.actually.pirates.Pirates;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.state.property.Properties;
import org.joml.Quaternionf;
import org.valkyrienskies.eureka.EurekaBlocks;
import org.valkyrienskies.eureka.block.ShipHelmBlock;

import java.util.Objects;

import static java.lang.Math.*;

public class MotionInvokingBlockEntityRenderer implements BlockEntityRenderer<MotionInvokingBlockEntity> {

    public MotionInvokingBlockEntityRenderer(BlockEntityRendererFactory.Context context){}



    @Override
    public void render(MotionInvokingBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        BlockRenderManager blockRenderManager = MinecraftClient.getInstance().getBlockRenderManager();

        float size = 0.25f;

        BlockState state = Objects.requireNonNull(entity.getWorld()).getBlockState(entity.getPos().down());

        if (!(state.getBlock() instanceof ShipHelmBlock)) {
            blockRenderManager.renderBlockAsEntity(Blocks.SPAWNER.getDefaultState(),  matrices, vertexConsumers, light, overlay);
            return;
        }

        matrices.push();

        matrices.translate(0.2, -0.7, 0);

        matrices.translate(0.5, 0.18, 0.5);
        matrices.scale(size, size, size);

        double rotationValue = (state.get(Properties.HORIZONTAL_FACING).getOpposite().asRotation() * PI / 180);
        matrices.multiply(new Quaternionf(cos(rotationValue / 2), 0, sin(rotationValue / 2), 0));
        matrices.multiply(new Quaternionf(0, 0, 1, 0));

        matrices.translate(-0.5, 0, -0.5);

        blockRenderManager.renderBlockAsEntity(Blocks.SPAWNER.getDefaultState(),  matrices, vertexConsumers, light, overlay);

        matrices.pop();


    }


}
