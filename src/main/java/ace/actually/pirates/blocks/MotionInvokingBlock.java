package ace.actually.pirates.blocks;

import ace.actually.pirates.Pirates;
import ace.actually.pirates.blocks.entity.MotionInvokingBlockEntity;
import ace.actually.pirates.util.EurekaCompat;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
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


    public MotionInvokingBlock(Settings settings) {
        super(settings);

    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if(world instanceof  ServerWorld serverWorld && player.getStackInHand(hand).isOf(Items.DEBUG_STICK))
        {
            MotionInvokingBlockEntity be = (MotionInvokingBlockEntity) serverWorld.getBlockEntity(pos);
            be.setCompat("None");
        }
        return super.onUse(state, world, pos, player, hand, hit);
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
        stopMotion(world,pos);
        super.onStateReplaced(state, world, pos, newState, moved);

    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    public static void disarm(World world, BlockPos pos) {
        if (world.isClient()) return;

        world.setBlockState(pos, Blocks.SPRUCE_PLANKS.getDefaultState());
        world.playSound(null, pos, SoundEvents.BLOCK_BEACON_DEACTIVATE, SoundCategory.BLOCKS, 1, 0.95f);

    }

    private static void stopMotion(World world, BlockPos pos)
    {
        if(!world.isClient)
        {
            DimensionIdProvider provider = (DimensionIdProvider) world;
            ChunkPos chunkPos = world.getChunk(pos).getPos();
            LoadedServerShip ship = (LoadedServerShip) ValkyrienSkiesMod.getVsCore().getHooks().getCurrentShipServerWorld().getLoadedShips().getByChunkPos(chunkPos.x, chunkPos.z, provider.getDimensionId());
            if(ship!=null)
            {
                MotionInvokingBlockEntity be = (MotionInvokingBlockEntity) world.getBlockEntity(pos);
                if(be.getCompat().equals("Eureka"))
                {
                    EurekaCompat.stopMotion(ship);
                }
            }
        }


    }
}
