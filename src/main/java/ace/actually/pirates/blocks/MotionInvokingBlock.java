package ace.actually.pirates.blocks;

import ace.actually.pirates.Pirates;
import ace.actually.pirates.blocks.entity.MotionInvokingBlockEntity;
import ace.actually.pirates.util.EurekaCompat;
import ace.actually.pirates.util.SailsCompat;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.util.DimensionIdProvider;

public class MotionInvokingBlock extends BlockWithEntity {
    public static final BooleanProperty ARMED = BooleanProperty.of("armed");
    public static final IntProperty COMPAT = IntProperty.of("compat", 0, 2);

    public MotionInvokingBlock(Settings settings) {
        super(settings);

    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(ARMED);
        builder.add(COMPAT);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        int compatVal = 0;
        if (Pirates.loadedCompats.sails && SailsCompat.checkHelm(ctx.getWorld(), ctx.getBlockPos())) {
            compatVal = 1;
        }
        if (Pirates.loadedCompats.eureka && EurekaCompat.checkHelm(ctx.getWorld(), ctx.getBlockPos())) {
            compatVal = 2;
        }
        return super.getPlacementState(ctx).with(ARMED,true).with(COMPAT, compatVal);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
//        if(world instanceof  ServerWorld serverWorld && player.getStackInHand(hand).isOf(Items.DEBUG_STICK)) {
//            MotionInvokingBlockEntity be = (MotionInvokingBlockEntity) serverWorld.getBlockEntity(pos);
//            be.setCompat("None");
//        }
        return super.onUse(state, world, pos, player, hand, hit);
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        state.with(COMPAT, 0);
        if (Pirates.loadedCompats.sails && SailsCompat.checkHelm(world, pos)) {
            state.with(COMPAT, 1);
        }
        if (Pirates.loadedCompats.eureka && EurekaCompat.checkHelm(world, pos)) {
            state.with(COMPAT, 2);
        }
        world.setBlockState(pos, state, 10);
    }

        @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new MotionInvokingBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, Pirates.MOTION_INVOKING_BLOCK_ENTITY, MotionInvokingBlockEntity::tick);
    }

    @Override
    public void onStacksDropped(BlockState state, ServerWorld world, BlockPos pos, ItemStack tool, boolean dropExperience) {
        super.onStacksDropped(state, world, pos, tool, dropExperience);
        int i = 15 + world.random.nextInt(15) + world.random.nextInt(15);
        this.dropExperience(world, pos, i);
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
//        if (!newState.get(ARMED)) { //fixme nullptr exception?
//            stopMotion(world,pos); //fixme figure out how to activate this only when newState is not a MotionInvokingBlock
//        } //potentially could just move the stopMotion call into disarm()
//
        super.onStateReplaced(state, world, pos, newState, moved);

    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    public static void disarm(World world, BlockPos pos) {
        if (world.isClient()) return;

        world.setBlockState(pos, Pirates.MOTION_INVOKING_BLOCK.getDefaultState().with(ARMED,false));
        world.playSound(null, pos, SoundEvents.BLOCK_BEACON_DEACTIVATE, SoundCategory.BLOCKS, 1, 0.95f);
        stopMotion(world,pos); //fixme possibly an issue
    }

    private static void stopMotion(World world, BlockPos pos) {
        if(!world.isClient) {
            DimensionIdProvider provider = (DimensionIdProvider) world;
            ChunkPos chunkPos = world.getChunk(pos).getPos();
            LoadedServerShip ship = (LoadedServerShip) ValkyrienSkiesMod.getVsCore().getHooks().getCurrentShipServerWorld().getLoadedShips().getByChunkPos(chunkPos.x, chunkPos.z, provider.getDimensionId());
            if(ship!=null) {
                MotionInvokingBlockEntity be = (MotionInvokingBlockEntity) world.getBlockEntity(pos);
                if(world.getBlockState(pos).get(COMPAT).equals(1)) {
                    SailsCompat.stopMotion(ship);
                } else if (world.getBlockState(pos).get(COMPAT).equals(2)) {
                    EurekaCompat.stopMotion(ship);
                }
            }
        }


    }
}
