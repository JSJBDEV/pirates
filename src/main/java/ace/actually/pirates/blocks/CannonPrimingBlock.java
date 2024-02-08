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
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return Block.createCuboidShape(0,0,0,2,2,2);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(RedstoneLampBlock.LIT);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return getDefaultState().with(RedstoneLampBlock.LIT,false);
    }

    /**
     *
     * @param origin a point to "look" from
     * @param target a point to "look" at
     * @return the vector that can be used to travel between these points
     */
    public Vec3d pointLine(Vec3d origin, Vec3d target)
    {
        double d = target.x - origin.x;
        double e = target.y - origin.y;
        double f = target.z - origin.z;
        double g = Math.sqrt(d * d + f * f);
        float pitch = (MathHelper.wrapDegrees((float)(-(MathHelper.atan2(e, g) * 57.2957763671875))));
        float yaw = MathHelper.wrapDegrees((float)(MathHelper.atan2(f, d) * 57.2957763671875) - 90.0F);
        return getRotationVector(pitch,yaw);
    }

    protected final Vec3d getRotationVector(float pitch, float yaw) {
        float f = pitch * 0.017453292F;
        float g = -yaw * 0.017453292F;
        float h = MathHelper.cos(g);
        float i = MathHelper.sin(g);
        float j = MathHelper.cos(f);
        float k = MathHelper.sin(f);
        return new Vec3d((double)(i * j), (double)(-k), (double)(h * j));
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {

        return new CannonPrimingBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, Pirates.CANNON_PRIMING_BLOCK_ENTITY, CannonPrimingBlockEntity::tick);
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
