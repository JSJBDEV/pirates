package ace.actually.pirates.blocks.entity;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.RotationAxis;

public class ShipIdBlockEntityRenderer implements BlockEntityRenderer<ShipIdBlockEntity> {
    public ShipIdBlockEntityRenderer(BlockEntityRendererFactory.Context context){}
    @Override
    public void render(ShipIdBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();

        matrices.translate(0,2,0);
        switch (entity.getCachedState().get(Properties.FACING))
        {
            case NORTH -> {
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
                matrices.translate(-1,0,-0.9);
            }
            case EAST -> {
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90));
                matrices.translate(-1,0,0.1);
            }
            case WEST -> {
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(270));
                matrices.translate(0,0,-0.9);
            }
            case SOUTH -> matrices.translate(0,0,0.1);
        }
        matrices.scale(-0.1f,0.1f,0.1f);

        matrices.multiply(RotationAxis.NEGATIVE_Z.rotationDegrees(180));

        MinecraftClient.getInstance().textRenderer.draw(entity.getShipName().replace("\\","§"),0,1,0,false,matrices.peek().getPositionMatrix(),vertexConsumers, TextRenderer.TextLayerType.NORMAL,1,light);
        matrices.pop();
    }
}
