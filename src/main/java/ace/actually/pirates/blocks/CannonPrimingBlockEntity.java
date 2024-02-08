package ace.actually.pirates.blocks;

import ace.actually.pirates.ConfigUtils;
import ace.actually.pirates.Pirates;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.RedstoneLampBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.core.api.world.ServerShipWorld;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.util.DimensionIdProvider;

import java.util.List;

public class CannonPrimingBlockEntity extends BlockEntity {

    public CannonPrimingBlockEntity(BlockPos pos, BlockState state) {
        super(Pirates.CANNON_PRIMING_BLOCK_ENTITY, pos, state);

    }

    public static void tick(World world, BlockPos pos, BlockState state, CannonPrimingBlockEntity be)
    {
        if(state.get(RedstoneLampBlock.LIT))
        {
            world.setBlockState(pos,state.with(RedstoneLampBlock.LIT,false));
        }
        if(!world.isClient && world.getTimeOfDay()%40==0)
        {

            ServerShipWorld serverShipWorld = (ServerShipWorld) ValkyrienSkiesMod.getVsCore().getHooks().getCurrentShipServerWorld();
            DimensionIdProvider provider = (DimensionIdProvider) world;
            if(serverShipWorld.isBlockInShipyard(pos.getX(),pos.getY(),pos.getZ(),provider.getDimensionId()))
            {
                ChunkPos chunkPos = world.getChunk(pos).getPos();
                LoadedServerShip ship = (LoadedServerShip) ValkyrienSkiesMod.getVsCore().getHooks().getCurrentShipServerWorld().getLoadedShips().getByChunkPos(chunkPos.x,chunkPos.z, provider.getDimensionId());

                Vec3d middle = VSGameUtilsKt.toWorldCoordinates(ship,Vec3d.ofCenter(pos));

                //Pirates.LOGGER.info("translated: "+middle);

                if(!world.getPlayers().isEmpty())
                {
                    List<PlayerEntity> players = world.getEntitiesByClass(PlayerEntity.class,new Box(middle.add(-20,-20,-20),middle.add(20,20,20)),a->!a.isCreative());
                    if(!players.isEmpty())
                    {
                        RaycastContext context = new RaycastContext(middle,players.get(0).getPos(), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE,null);

                        BlockHitResult result = world.raycast(context);
                        if(world.getBlockState(result.getBlockPos()).isOf(Blocks.DISPENSER))
                        {
                            world.setBlockState(pos,state.with(RedstoneLampBlock.LIT,true));
                        }

                        //Pirates.LOGGER.info(world.getBlockState(result.getBlockPos()).toString()+" at "+result.getPos().toString() + " for "+result.getBlockPos());

                    }
                }

            }
        }
    }
}
