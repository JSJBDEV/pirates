package ace.actually.pirates.blocks;

import ace.actually.pirates.PatternProcessor;
import ace.actually.pirates.Pirates;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FurnaceBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.world.ServerShipWorld;
import org.valkyrienskies.eureka.util.ShipAssembler;
import org.valkyrienskies.mod.api.SeatedControllingPlayer;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.util.DimensionIdProvider;

public class MotionInvokingBlockEntity extends BlockEntity {

    NbtList instructions = new NbtList();
    long nextInstruction = 0;

    public MotionInvokingBlockEntity(BlockPos pos, BlockState state) {
        super(Pirates.MOTION_INVOKING_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, MotionInvokingBlockEntity be) {
        if(be.instructions.isEmpty() && Pirates.isLiveWorld)
        {
            be.getPattern("circle.pattern");
        }
        if(!world.isClient && Pirates.isLiveWorld && world.getTime()>=be.nextInstruction)
        {
            DimensionIdProvider provider = (DimensionIdProvider) world;
            ServerShipWorld serverShipWorld = (ServerShipWorld) ValkyrienSkiesMod.getVsCore().getHooks().getCurrentShipServerWorld();
            if(serverShipWorld.isBlockInShipyard(pos.getX(),pos.getY(),pos.getZ(),provider.getDimensionId()))
            {


                ChunkPos chunkPos = world.getChunk(pos).getPos();
                LoadedServerShip ship = (LoadedServerShip) ValkyrienSkiesMod.getVsCore().getHooks().getCurrentShipServerWorld().getLoadedShips().getByChunkPos(chunkPos.x,chunkPos.z, provider.getDimensionId());

                //Pirates.LOGGER.info("scaling of ship: "+s.x()+" "+s.y()+" "+s.z());

                SeatedControllingPlayer seatedControllingPlayer = ship.getAttachment(SeatedControllingPlayer.class);
                if(seatedControllingPlayer==null)
                {
                    seatedControllingPlayer = new SeatedControllingPlayer(state.get(FurnaceBlock.FACING));
                    ship.setAttachment(SeatedControllingPlayer.class,seatedControllingPlayer);
                }

                //this is the bit where it does things, theoretically you can derive some AI from this
                //good luck tho
                //Pirates.LOGGER.info(be.instructions.getString(0));
                be.utiliseInternalPattern(seatedControllingPlayer,be);


            }
            else
            {
                be.buildShipRec((ServerWorld) world,pos);
            }
        }

    }

    private ServerShip buildShipRec(ServerWorld world, BlockPos pos)
    {
        return ShipAssembler.INSTANCE.collectBlocks(world,pos, a-> !a.isAir() && !a.isOf(Blocks.WATER) && !a.isOf(Blocks.KELP) && !a.isOf(Blocks.KELP_PLANT) && !a.isOf(Blocks.SAND) && !a.isIn(BlockTags.ICE));
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        nbt.putLong("nextInstruction",nextInstruction);
        nbt.put("instructions",instructions);
        super.writeNbt(nbt);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        instructions= (NbtList) nbt.get("instructions");
        nextInstruction = nbt.getLong("nextInstruction");
    }



    public void getPattern(String loc)
    {
        instructions= PatternProcessor.loadPattern(loc);
        nextInstruction=world.getTime()+10;
        markDirty();
    }

    private void utiliseInternalPattern(SeatedControllingPlayer seatedControllingPlayer, MotionInvokingBlockEntity be)
    {
        String[] instruction = be.instructions.getString(0).split(" ");
        switch (instruction[0])
        {
            case "forward" -> seatedControllingPlayer.setForwardImpulse(Float.parseFloat(instruction[1]));
            case "left" -> seatedControllingPlayer.setLeftImpulse(Float.parseFloat(instruction[1]));
            case "right" -> seatedControllingPlayer.setLeftImpulse(-Float.parseFloat(instruction[1]));
            case "backwards" -> seatedControllingPlayer.setForwardImpulse(-Float.parseFloat(instruction[1]));
            case "up" -> seatedControllingPlayer.setUpImpulse(Float.parseFloat(instruction[1]));
            case "down" -> seatedControllingPlayer.setUpImpulse(-Float.parseFloat(instruction[1]));
        }
        be.nextInstruction=world.getTime()+Long.parseLong(instruction[2]);
        be.instructions.add(be.instructions.remove(0));
        be.markDirty();
    }
}
