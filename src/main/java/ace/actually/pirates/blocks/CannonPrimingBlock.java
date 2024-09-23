package ace.actually.pirates.blocks;

import ace.actually.pirates.Pirates;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import org.jetbrains.annotations.Nullable;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

public class CannonPrimingBlock extends BlockWithEntity {
    public CannonPrimingBlock(Settings settings) {
        super(settings);
    }
    public static final BooleanProperty DISARMED = Properties.DISARMED;


    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(RedstoneLampBlock.LIT).add(Properties.FACING).add(DISARMED);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockPos pos = ctx.getBlockPos();
        World world = ctx.getWorld();
        Direction facing;

        if (world.getBlockState(pos.north()).isOf(Blocks.DISPENSER)) {
            facing = Direction.NORTH;
        } else if (world.getBlockState(pos.east()).isOf(Blocks.DISPENSER)) {
            facing = Direction.EAST;
        }  else if (world.getBlockState(pos.south()).isOf(Blocks.DISPENSER)) {
            facing = Direction.SOUTH;
        }  else if (world.getBlockState(pos.west()).isOf(Blocks.DISPENSER)) {
            facing = Direction.WEST;
        }  else if (world.getBlockState(pos.up()).isOf(Blocks.DISPENSER)) {
            facing = Direction.UP;
        }  else if (world.getBlockState(pos.down()).isOf(Blocks.DISPENSER)) {
            facing = Direction.DOWN;
        } else {
            facing = ctx.getPlayerLookDirection();

        }

        if (world.getBlockState(pos.add(facing.getVector())).isOf(Blocks.DISPENSER) && world.getBlockState(pos.add(facing.getVector())).get(Properties.FACING) == facing) {
            world.setBlockState(pos.add(facing.getVector()), Pirates.DISPENSER_CANNON_BLOCK.getDefaultState().with(Properties.FACING, facing), 3);
        }
        return getDefaultState().with(RedstoneLampBlock.LIT,false).with(Properties.FACING, facing).with(DISARMED, true);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {

        return new CannonPrimingBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, Pirates.CANNON_PRIMING_BLOCK_ENTITY, (world1, pos, state1, be) -> be.tick(world1, pos, state1, be));
    }

//    @Override
//    public boolean emitsRedstonePower(BlockState state) {
//        return state.get(RedstoneLampBlock.LIT);
//    }
//
//    @Override
//    public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
//       if(state.get(RedstoneLampBlock.LIT))
//       {
//           return 15;
//       }
//       return 0;
//    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {

        if (world.getBlockState(pos.add(state.get(Properties.FACING).getVector())).isOf(Blocks.DISPENSER) && world.getBlockState(pos.add(state.get(Properties.FACING).getVector())).get(Properties.FACING) == state.get(Properties.FACING)) {
            world.setBlockState(pos.add(state.get(Properties.FACING).getVector()), Pirates.DISPENSER_CANNON_BLOCK.getDefaultState().with(Properties.FACING, state.get(Properties.FACING)), 3);
        }

        if (world.isReceivingRedstonePower(pos) && !world.isClient()) {((CannonPrimingBlockEntity)world.getBlockEntity(pos)).fire((World) world, pos, state, 19);}
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return (BlockState)state.with(Properties.FACING, rotation.rotate((Direction)state.get(Properties.FACING)));
    }

    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation((Direction)state.get(Properties.FACING)));
    }

    public static void disarm(World world, BlockPos pos) {
        if (world.isClient()) return;
        BlockState blockState = world.getBlockState(pos);
        if (!blockState.get(DISARMED)) {
            world.setBlockState(pos, blockState.with(DISARMED, true));
            world.playSound(null, pos, SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT, SoundCategory.BLOCKS, 0.5f, 1.5f);
        }
    }
}
