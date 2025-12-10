package ace.actually.pirates.blocks.entity;

import org.joml.Quaternionf;

import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import static java.lang.Math.*;

import com.mojang.blaze3d.vertex.PoseStack;

public class CannonPrimingBlockEntityRenderer implements BlockEntityRenderer<CannonPrimingBlockEntity> {

    public CannonPrimingBlockEntityRenderer(BlockEntityRendererProvider.Context context){}



    @Override
    public void render(CannonPrimingBlockEntity entity, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {
        if (entity.isRemoved()) return;

        BlockState state = Objects.requireNonNull(entity.getLevel()).getBlockState(entity.getBlockPos());

        if (state.getValue(BlockStateProperties.DISARMED)) return;

        BlockRenderDispatcher blockRenderManager = Minecraft.getInstance().getBlockRenderer();

        matrices.pushPose();

        matrices.translate(0.5, 1.2501, 0.5);

        double rotationValue = (state.getValue(BlockStateProperties.FACING).toYRot() * PI / 180) + entity.randomRotation + PI / 8;
        matrices.mulPose(new Quaternionf(cos(rotationValue / 2), 0, sin(rotationValue / 2), 0));

        matrices.mulPose(new Quaternionf(cos(PI / 4), sin(PI / 4), 0, 0));
        matrices.translate(-0.5 + 0.1875, -0.25, -0.5);

        blockRenderManager.renderSingleBlock(Blocks.TORCH.defaultBlockState(), matrices, vertexConsumers, 255, overlay);

        matrices.popPose();

    }


}
