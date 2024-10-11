package ace.actually.pirates.blocks.entity;

import ace.actually.pirates.util.PatternProcessor;
import ace.actually.pirates.Pirates;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.eureka.block.ShipHelmBlock;
import org.valkyrienskies.eureka.util.ShipAssembler;
import org.valkyrienskies.mod.api.SeatedControllingPlayer;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.util.DimensionIdProvider;

import static net.minecraft.state.property.Properties.HORIZONTAL_FACING;

public class MotionInvokingBlockEntity extends BlockEntity {

    NbtList instructions = new NbtList();
    long nextInstruction = 0;

    public MotionInvokingBlockEntity(BlockPos pos, BlockState state) {
        super(Pirates.MOTION_INVOKING_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, MotionInvokingBlockEntity be) {

        if (!(world.getBlockState(pos.up()).getBlock() instanceof ShipHelmBlock)) {
            return;
        }

        if (be.instructions.isEmpty() && world.getGameRules().getBoolean(Pirates.PIRATES_IS_LIVE_WORLD)) {
            be.getPattern("circle.pattern");
        }
        if (!world.isClient && world.getGameRules().getBoolean(Pirates.PIRATES_IS_LIVE_WORLD) && world.getTime() >= be.nextInstruction) {
            DimensionIdProvider provider = (DimensionIdProvider) world;

            if (VSGameUtilsKt.isBlockInShipyard(world, pos)) {


                ChunkPos chunkPos = world.getChunk(pos).getPos();
                LoadedServerShip ship = (LoadedServerShip) ValkyrienSkiesMod.getVsCore().getHooks().getCurrentShipServerWorld().getLoadedShips().getByChunkPos(chunkPos.x, chunkPos.z, provider.getDimensionId());

                //Pirates.LOGGER.info("scaling of ship: "+s.x()+" "+s.y()+" "+s.z());

                if (ship != null) {
                    SeatedControllingPlayer seatedControllingPlayer = ship.getAttachment(SeatedControllingPlayer.class);
                    if (seatedControllingPlayer == null) {
                        if (world.getBlockState(pos.up()).getBlock() instanceof ShipHelmBlock) {
                            seatedControllingPlayer = new SeatedControllingPlayer(world.getBlockState(pos.up()).get(HORIZONTAL_FACING).getOpposite());
                        } else {
                            return;
                        }
                        ship.setAttachment(SeatedControllingPlayer.class, seatedControllingPlayer);
                    }

                    //this is the bit where it does things, theoretically you can derive some AI from this
                    //good luck tho
                    //Pirates.LOGGER.info(be.instructions.getString(0));
                    be.utiliseInternalPattern(seatedControllingPlayer, be);

                }
            } else {
                be.buildShipRec((ServerWorld) world, pos);
            }
        }

    }

    private void buildShipRec(ServerWorld world, BlockPos pos) {
        ShipAssembler.INSTANCE.collectBlocks(world, pos, a -> !a.isAir() && !a.isOf(Blocks.WATER) && !a.isOf(Blocks.KELP) && !a.isOf(Blocks.KELP_PLANT) && !a.isOf(Blocks.SAND) && !a.isIn(BlockTags.ICE) && !a.isOf(Blocks.STONE));
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        nbt.putLong("nextInstruction", nextInstruction);
        nbt.put("instructions", instructions);
        super.writeNbt(nbt);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        instructions = (NbtList) nbt.get("instructions");
        nextInstruction = nbt.getLong("nextInstruction");
    }


    public void getPattern(String loc) {
        instructions = PatternProcessor.loadPattern(loc);
        nextInstruction = world.getTime() + 10;
        markDirty();
    }

    private void utiliseInternalPattern(SeatedControllingPlayer seatedControllingPlayer, MotionInvokingBlockEntity be) {
        String[] instruction = be.instructions.getString(0).split(" ");

        switch (instruction[0]) {
            case "forward" -> seatedControllingPlayer.setForwardImpulse(Float.parseFloat(instruction[1]));
            case "left" -> seatedControllingPlayer.setLeftImpulse(Float.parseFloat(instruction[1]));
            case "right" -> seatedControllingPlayer.setLeftImpulse(-Float.parseFloat(instruction[1]));
            case "backwards" -> seatedControllingPlayer.setForwardImpulse(-Float.parseFloat(instruction[1]));
            case "up" -> seatedControllingPlayer.setUpImpulse(Float.parseFloat(instruction[1]));
            case "down" -> seatedControllingPlayer.setUpImpulse(-Float.parseFloat(instruction[1]));
        }
        be.nextInstruction = world.getTime() + Long.parseLong(instruction[2]);
        be.instructions.add(be.instructions.remove(0));
        be.markDirty();
    }
}
