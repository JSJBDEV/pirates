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

import java.util.Objects;

import static java.lang.Math.*;

public class CannonPrimingBlockEntityRenderer implements BlockEntityRenderer<CannonPrimingBlockEntity> {

    public CannonPrimingBlockEntityRenderer(BlockEntityRendererFactory.Context context){}



    @Override
    public void render(CannonPrimingBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (entity.isRemoved()) return;


        BlockRenderManager blockRenderManager = MinecraftClient.getInstance().getBlockRenderManager();

        BlockState state = Objects.requireNonNull(entity.getWorld()).getBlockState(entity.getPos());


        matrices.push();

        matrices.translate(0.5, 1.25, 0.5);
//        matrices.scale(0.8f, 0.8f, 0.8f);

        double rotationValue = (state.get(Properties.FACING).asRotation() * PI / 180) + entity.randomRotation + PI / 8;
        matrices.multiply(new Quaternionf(cos(rotationValue / 2), 0, sin(rotationValue / 2), 0));
        //matrices.multiply(new Quaternionf(0, 0, 1, 0));

        matrices.multiply(new Quaternionf(cos(PI / 4), sin(PI / 4), 0, 0));


        matrices.translate(-0.5 + 0.1875, -0.25, -0.5);

        if (!state.get(Properties.DISARMED)) {
            blockRenderManager.renderBlockAsEntity(Blocks.TORCH.getDefaultState(), matrices, vertexConsumers, 255, overlay);
        }

        matrices.pop();


    }


}
