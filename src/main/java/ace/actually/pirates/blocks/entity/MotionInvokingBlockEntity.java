package ace.actually.pirates.blocks.entity;

import ace.actually.pirates.blocks.MotionInvokingBlock;
import ace.actually.pirates.util.ConfigUtils;
import ace.actually.pirates.util.EurekaCompat;
import ace.actually.pirates.Pirates;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.api.SeatedControllingPlayer;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.GameTickForceApplier;

import java.util.List;

import static net.minecraft.state.property.Properties.HORIZONTAL_FACING;

public class MotionInvokingBlockEntity extends BlockEntity {
    NbtList path = new NbtList();
    long nextInstruction = 0;
    String compat = "Eureka";

    //variables below this line aren't serialised because they don't need to be.
    int[] target = new int[3]; //x,y,z of a point in space that the ship is "trying" to get to.
    double ldx = -1; //last distance tracked along the x-axis, from the target
    double ldz = -1; //last distance tracked along the z-axis, from the target


    private static int updateTicks = -1;

    public MotionInvokingBlockEntity(BlockPos pos, BlockState state) {
        super(Pirates.MOTION_INVOKING_BLOCK_ENTITY, pos, state);
    }

    public void setCompat(String compat) {
        this.compat = compat;
        markDirty();
    }

    public static void tick(World world, BlockPos pos, BlockState state, MotionInvokingBlockEntity be) {

        if(!state.get(MotionInvokingBlock.ARMED)) return;

        if (be.compat.equals("Eureka") && EurekaCompat.isHelm(state)) {
            return;
        }
        if(updateTicks==-1)
        {
            updateTicks = Integer.parseInt(ConfigUtils.config.getOrDefault("controlled-ship-updates","100"));

        }
        if (!world.isClient && world.getGameRules().getBoolean(Pirates.PIRATES_IS_LIVE_WORLD) && world.getTime() >= be.nextInstruction) {

            if (VSGameUtilsKt.isBlockInShipyard(world, pos))
            {
                ChunkPos chunkPos = world.getChunk(pos).getPos();
                LoadedServerShip ship = VSGameUtilsKt.getShipObjectManagingPos((ServerWorld) world, chunkPos);

                if (ship != null)
                {
                    ship.setStatic(false);
                    SeatedControllingPlayer seatedControllingPlayer = ship.getAttachment(SeatedControllingPlayer.class);
                    if (seatedControllingPlayer == null && be.compat.equals("Eureka") && world.getBlockState(pos.up()).contains(HORIZONTAL_FACING))
                    {
                        seatedControllingPlayer = new SeatedControllingPlayer(world.getBlockState(pos.up()).get(HORIZONTAL_FACING).getOpposite());
                        ship.setAttachment(SeatedControllingPlayer.class, seatedControllingPlayer);
                    }

                    if(world.getTimeOfDay()%updateTicks==0)
                    {
                        if(be.path.isEmpty())
                        {
                            List<Ship> ships = VSGameUtilsKt.getAllShips(world).stream().filter(a->
                            {
                                if(a.getId()==ship.getId()) return false;
                                Vector3dc f1 = ship.getTransform().getPositionInWorld();
                                Vector3dc f2 = a.getTransform().getPositionInWorld();
                                return f1.distanceSquared(f2)<Pirates.pursuitDistance;
                            }).toList();
                            if(!ships.isEmpty())
                            {
                                Vector3dc o = ships.get(0).getTransform().getPositionInWorld();
                                be.setTarget(new int[]{(int) o.x(), (int) o.y(), (int) o.z()});
                            }
                        }
                        else
                        {
                            int[] v = be.path.getIntArray(0);
                            be.setTarget(v);
                            Vector3dc f1 = ship.getTransform().getPositionInWorld();
                            Vector3dc f2 = new Vector3d(v[0],v[1],v[2]);
                            if(f1.distanceSquared(f2)<100)
                            {
                                NbtIntArray nbtInts = (NbtIntArray) be.path.remove(0);
                                be.path.add(nbtInts);
                            }
                        }

                    }

                    switch (be.compat)
                    {
                        case "Eureka" -> EurekaCompat.moveTowards(be,seatedControllingPlayer,ship);
                        default -> be.moveShipForward(ship);
                    }


                }
            }
        }

    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        nbt.put("path",path);
        nbt.putLong("nextInstruction", nextInstruction);
        nbt.putIntArray("target",target);
        nbt.putString("compat",compat);
        super.writeNbt(nbt);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        nextInstruction = nbt.getLong("nextInstruction");
        if(nbt.contains("path"))
        {
            path = (NbtList) nbt.get("path");
        }

        if(nbt.contains("compat"))
        {
            compat = nbt.getString("compat");
        }
        if(nbt.contains("target"))
        {
            target = nbt.getIntArray("target");
        }

    }

    public void setTarget(int[] target) {
        this.target = target;
        markDirty();
    }

    public String getCompat() {
        return compat;
    }

    public int[] getTarget() {
        return target;
    }

    public double getLdx() {
        return ldx;
    }

    public double getLdz() {
        return ldz;
    }

    public void setLdx(double ldx) {
        this.ldx = ldx;
    }

    public void setLdz(double ldz) {
        this.ldz = ldz;
    }

    public NbtList getPath() {
        return path;
    }

    public void setPath(NbtList path) {
        this.path = path;
        markDirty();
    }
    public void addPathNode(BlockPos pos)
    {
        this.path.add(new NbtIntArray(new int[]{pos.getX(),pos.getY(),pos.getZ()}));
        markDirty();
    }

    /**
     * This method uses bases VS things to effectively create circles.
     * the circles arent very good. TODO: Make the circles good
     * @param ship
     */
    private void moveShipForward(LoadedServerShip ship)
    {
        double mass = ship.getInertiaData().getMass();
        Vector3d qdc = ship.getTransform().getShipToWorldRotation().getEulerAnglesZXY(new Vector3d()).normalize().mul(mass*10);
        qdc = new Vector3d(-qdc.x,0,-qdc.z);
        GameTickForceApplier gtfa = ship.getAttachment(GameTickForceApplier.class);

        if(gtfa!=null)
        {
            Vector3dc v3dc = ship.getInertiaData().getCenterOfMassInShip();
            Vector3d loc = new Vector3d(v3dc.x()+1,v3dc.y(),v3dc.z()+1);
            //if(world instanceof ServerWorld serverWorld)
            //{
            //    serverWorld.spawnParticles(ParticleTypes.BUBBLE,loc.x,loc.y,loc.z,1,0,0,0,0);
            //}
            gtfa.applyInvariantForceToPos(qdc,loc.sub(ship.getTransform().getPositionInShip()));
        }
    }
}
