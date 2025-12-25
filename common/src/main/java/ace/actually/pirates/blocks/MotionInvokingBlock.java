package ace.actually.pirates.blocks;

import ace.actually.pirates.Pirates;
import ace.actually.pirates.blocks.entity.MotionInvokingBlockEntity;
import ace.actually.pirates.util.EurekaCompat;
import ace.actually.pirates.util.SailsCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.util.DimensionIdProvider;

public class MotionInvokingBlock extends BaseEntityBlock {
    public static final BooleanProperty ARMED = BooleanProperty.create("armed");
    public static final IntegerProperty COMPAT = IntegerProperty.create("compat", 0, 2);

    public MotionInvokingBlock(Properties settings) {
        super(settings);

    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ARMED);
        builder.add(COMPAT);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        int compatVal = 0;
        if (Pirates.loadedCompats.sails && SailsCompat.checkHelm(ctx.getLevel(), ctx.getClickedPos())) {
            compatVal = 1;
        }
        if (Pirates.loadedCompats.eureka && EurekaCompat.checkHelm(ctx.getLevel(), ctx.getClickedPos())) {
            compatVal = 2;
        }
        return super.getStateForPlacement(ctx).setValue(ARMED,true).setValue(COMPAT, compatVal);
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
//        if(world instanceof  ServerWorld serverWorld && player.getStackInHand(hand).isOf(Items.DEBUG_STICK)) {
//            MotionInvokingBlockEntity be = (MotionInvokingBlockEntity) serverWorld.getBlockEntity(pos);
//            be.setCompat("None");
//        }
        return super.use(state, world, pos, player, hand, hit);
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        state.setValue(COMPAT, 0);
        if (Pirates.loadedCompats.sails && SailsCompat.checkHelm(world, pos)) {
            state.setValue(COMPAT, 1);
        }
        if (Pirates.loadedCompats.eureka && EurekaCompat.checkHelm(world, pos)) {
            state.setValue(COMPAT, 2);
        }
        world.setBlock(pos, state, 10);
    }

        @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MotionInvokingBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, Pirates.MOTION_INVOKING_BLOCK_ENTITY.get(), MotionInvokingBlockEntity::tick);
    }

    @Override
    public void spawnAfterBreak(BlockState state, ServerLevel world, BlockPos pos, ItemStack tool, boolean dropExperience) {
        super.spawnAfterBreak(state, world, pos, tool, dropExperience);
        int i = 15 + world.random.nextInt(15) + world.random.nextInt(15);
        this.popExperience(world, pos, i);
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
//        if (!newState.get(ARMED)) { //fixme nullptr exception?
//            stopMotion(world,pos); //fixme figure out how to activate this only when newState is not a MotionInvokingBlock
//        } //potentially could just move the stopMotion call into disarm()
//
        super.onRemove(state, world, pos, newState, moved);

    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    public static void disarm(Level world, BlockPos pos) {
        if (world.isClientSide()) return;

        world.setBlockAndUpdate(pos, Pirates.MOTION_INVOKING_BLOCK.get().defaultBlockState().setValue(ARMED,false));
        world.playSound(null, pos, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 1, 0.95f);
        stopMotion(world,pos); //fixme possibly an issue
    }

    private static void stopMotion(Level world, BlockPos pos) {
        if(!world.isClientSide) {
            DimensionIdProvider provider = (DimensionIdProvider) world;
            ChunkPos chunkPos = world.getChunk(pos).getPos();
            LoadedServerShip ship = (LoadedServerShip) ValkyrienSkiesMod.getVsCore().getHooks().getCurrentShipServerWorld().getLoadedShips().getByChunkPos(chunkPos.x, chunkPos.z, provider.getDimensionId());
            if(ship!=null) {
                MotionInvokingBlockEntity be = (MotionInvokingBlockEntity) world.getBlockEntity(pos);
                if(world.getBlockState(pos).getValue(COMPAT).equals(1)) {
                    SailsCompat.stopMotion(ship);
                } else if (world.getBlockState(pos).getValue(COMPAT).equals(2)) {
                    EurekaCompat.stopMotion(ship);
                }
            }
        }


    }
}
