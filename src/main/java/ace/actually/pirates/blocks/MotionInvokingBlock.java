package ace.actually.pirates.blocks;

import ace.actually.pirates.Pirates;
import ace.actually.pirates.blocks.entity.MotionInvokingBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.mod.api.SeatedControllingPlayer;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.util.DimensionIdProvider;

public class MotionInvokingBlock extends BlockWithEntity {


    public MotionInvokingBlock(Settings settings) {
        super(settings);

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
        super.onStateReplaced(state, world, pos, newState, moved);
        stopMotion(world,pos);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    public static void disarm(World world, BlockPos pos) {
        if (world.isClient()) return;

        stopMotion(world,pos);
        world.setBlockState(pos, Blocks.SPRUCE_PLANKS.getDefaultState());
        world.playSound(null, pos, SoundEvents.BLOCK_BEACON_DEACTIVATE, SoundCategory.BLOCKS, 1, 0.95f);

    }

    private static void stopMotion(World world, BlockPos pos)
    {
        DimensionIdProvider provider = (DimensionIdProvider) world;
        ChunkPos chunkPos = world.getChunk(pos).getPos();
        LoadedServerShip ship = (LoadedServerShip) ValkyrienSkiesMod.getVsCore().getHooks().getCurrentShipServerWorld().getLoadedShips().getByChunkPos(chunkPos.x, chunkPos.z, provider.getDimensionId());
        SeatedControllingPlayer seatedControllingPlayer = ship.getAttachment(SeatedControllingPlayer.class);
        seatedControllingPlayer.setLeftImpulse(0);
        seatedControllingPlayer.setForwardImpulse(0);
        seatedControllingPlayer.setCruise(false);
        seatedControllingPlayer.setUpImpulse(0);
        ship.setAttachment(SeatedControllingPlayer.class, seatedControllingPlayer);
    }
}
