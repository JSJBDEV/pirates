package ace.actually.pirates.blocks.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class ShipIdBlockEntityRenderer implements BlockEntityRenderer<ShipIdBlockEntity> {
    public ShipIdBlockEntityRenderer(BlockEntityRendererProvider.Context context){}
    @Override
    public void render(ShipIdBlockEntity entity, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {
        matrices.pushPose();

        matrices.translate(0,2,0);
        switch (entity.getBlockState().getValue(BlockStateProperties.FACING))
        {
            case NORTH -> {
                matrices.mulPose(Axis.YP.rotationDegrees(180));
                matrices.translate(-1,0,-0.9);
            }
            case EAST -> {
                matrices.mulPose(Axis.YP.rotationDegrees(90));
                matrices.translate(-1,0,0.1);
            }
            case WEST -> {
                matrices.mulPose(Axis.YP.rotationDegrees(270));
                matrices.translate(0,0,-0.9);
            }
            case SOUTH -> matrices.translate(0,0,0.1);
        }
        matrices.scale(-0.1f,0.1f,0.1f);

        matrices.mulPose(Axis.ZN.rotationDegrees(180));

        Minecraft.getInstance().font.drawInBatch(entity.getShipName().replace("\\","§"),0,1,0,false,matrices.last().pose(),vertexConsumers, Font.DisplayMode.NORMAL,1,light);
        matrices.popPose();
    }
}
