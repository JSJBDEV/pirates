package ace.actually.pirates.blocks;

import ace.actually.pirates.blocks.entity.ShipIdBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class ShipIdBlock extends BaseEntityBlock {
    public ShipIdBlock(Properties settings) {
        super(settings);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        switch (state.getValue(BlockStateProperties.FACING))
        {
            case EAST -> {
                return Block.box(0,1,1,1,16,16);
            }
            case WEST -> {
                return Block.box(15,1,1,16,16,16);
            }
            case NORTH -> {
                return Block.box(1,1,15,16,16,16);
            }
            case SOUTH -> {
                return Block.box(1,1,0,16,16,1);
            }
        }
        //System.out.println(state.get(Properties.FACING));
        return Block.box(0,0,0,8,8,8);
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if(player.getMainHandItem().hasCustomHoverName())
        {
            ShipIdBlockEntity be = (ShipIdBlockEntity) world.getBlockEntity(pos);
            be.setShipName(player.getMainHandItem().getHoverName().getString());
        }
        return super.use(state, world, pos, player, hand, hit);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ShipIdBlockEntity(pos,state);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return super.getStateForPlacement(ctx).setValue(BlockStateProperties.FACING,ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.FACING);
    }
}
