package ace.actually.pirates.blocks;

import ace.actually.pirates.Pirates;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockStateRaycastContext;
import net.minecraft.world.BlockView;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.core.api.world.ServerShipWorld;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.util.DimensionIdProvider;
import org.valkyrienskies.mod.common.world.RaycastUtilsKt;

public class CannonPrimingBlock extends BlockWithEntity {
    public CannonPrimingBlock(Settings settings) {
        super(settings);
    }


    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(RedstoneLampBlock.LIT).add(Properties.FACING);
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
        return getDefaultState().with(RedstoneLampBlock.LIT,false).with(Properties.FACING, facing);
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

    @Override
    public boolean emitsRedstonePower(BlockState state) {
        return state.get(RedstoneLampBlock.LIT);
    }

    @Override
    public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
       if(state.get(RedstoneLampBlock.LIT))
       {
           return 15;
       }
       return 0;
    }
}
