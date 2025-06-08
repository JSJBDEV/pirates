package ace.actually.pirates.blocks;

import ace.actually.pirates.blocks.entity.ShipIdBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ShipIdBlock extends BlockWithEntity {
    public ShipIdBlock(Settings settings) {
        super(settings);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        switch (state.get(Properties.FACING))
        {
            case EAST -> {
                return Block.createCuboidShape(0,1,1,1,16,16);
            }
            case WEST -> {
                return Block.createCuboidShape(15,1,1,16,16,16);
            }
            case NORTH -> {
                return Block.createCuboidShape(1,1,15,16,16,16);
            }
            case SOUTH -> {
                return Block.createCuboidShape(1,1,0,16,16,1);
            }
        }
        //System.out.println(state.get(Properties.FACING));
        return Block.createCuboidShape(0,0,0,8,8,8);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if(player.getMainHandStack().hasCustomName())
        {
            ShipIdBlockEntity be = (ShipIdBlockEntity) world.getBlockEntity(pos);
            be.setShipName(player.getMainHandStack().getName().getString());
        }
        return super.onUse(state, world, pos, player, hand, hit);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ShipIdBlockEntity(pos,state);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return super.getPlacementState(ctx).with(Properties.FACING,ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(Properties.FACING);
    }
}
