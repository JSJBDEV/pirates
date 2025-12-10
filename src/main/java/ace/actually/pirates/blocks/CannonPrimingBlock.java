package ace.actually.pirates.blocks;

import ace.actually.pirates.Pirates;
import ace.actually.pirates.blocks.entity.CannonPrimingBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class CannonPrimingBlock extends BaseEntityBlock {
    public CannonPrimingBlock(Properties settings) {
        super(settings);
    }
    public static final BooleanProperty DISARMED = BlockStateProperties.DISARMED;


    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(RedstoneLampBlock.LIT).add(BlockStateProperties.FACING).add(DISARMED);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockPos pos = ctx.getClickedPos();
        Level world = ctx.getLevel();
        Direction facing;

        if (world.getBlockState(pos.north()).is(Blocks.DISPENSER)) {
            facing = Direction.NORTH;
        } else if (world.getBlockState(pos.east()).is(Blocks.DISPENSER)) {
            facing = Direction.EAST;
        }  else if (world.getBlockState(pos.south()).is(Blocks.DISPENSER)) {
            facing = Direction.SOUTH;
        }  else if (world.getBlockState(pos.west()).is(Blocks.DISPENSER)) {
            facing = Direction.WEST;
        }  else if (world.getBlockState(pos.above()).is(Blocks.DISPENSER)) {
            facing = Direction.UP;
        }  else if (world.getBlockState(pos.below()).is(Blocks.DISPENSER)) {
            facing = Direction.DOWN;
        } else {
            facing = ctx.getNearestLookingDirection();

        }

        if (world.getBlockState(pos.offset(facing.getNormal())).is(Blocks.DISPENSER) && world.getBlockState(pos.offset(facing.getNormal())).getValue(BlockStateProperties.FACING) == facing) {
            world.setBlock(pos.offset(facing.getNormal()), Pirates.DISPENSER_CANNON_BLOCK.defaultBlockState().setValue(BlockStateProperties.FACING, facing), 3);
        }
        return defaultBlockState().setValue(RedstoneLampBlock.LIT,false).setValue(BlockStateProperties.FACING, facing).setValue(DISARMED, true);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {

        return new CannonPrimingBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, Pirates.CANNON_PRIMING_BLOCK_ENTITY, (world1, pos, state1, be) -> be.tick(world1, pos, state1, be));
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos) {

        if (world.getBlockState(pos.offset(state.getValue(BlockStateProperties.FACING).getNormal())).is(Blocks.DISPENSER) && world.getBlockState(pos.offset(state.getValue(BlockStateProperties.FACING).getNormal())).getValue(BlockStateProperties.FACING) == state.getValue(BlockStateProperties.FACING)) {
            world.setBlock(pos.offset(state.getValue(BlockStateProperties.FACING).getNormal()), Pirates.DISPENSER_CANNON_BLOCK.defaultBlockState().setValue(BlockStateProperties.FACING, state.getValue(BlockStateProperties.FACING)), 3);
        }

        if (world.hasNeighborSignal(pos) && !world.isClientSide()) {((CannonPrimingBlockEntity)world.getBlockEntity(pos)).fire((Level) world, pos, state, 19);}
        return super.updateShape(state, direction, neighborState, world, pos, neighborPos);
    }

    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(BlockStateProperties.FACING, rotation.rotate(state.getValue(BlockStateProperties.FACING)));
    }

    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(BlockStateProperties.FACING)));
    }

    public static void disarm(Level world, BlockPos pos) {
        if (world.isClientSide()) return;
        BlockState blockState = world.getBlockState(pos);
        if (!blockState.getValue(DISARMED)) {
            world.setBlockAndUpdate(pos, blockState.setValue(DISARMED, true));
            world.playSound(null, pos, SoundEvents.REDSTONE_TORCH_BURNOUT, SoundSource.BLOCKS, 0.5f, 1.5f);
        }
    }
}
