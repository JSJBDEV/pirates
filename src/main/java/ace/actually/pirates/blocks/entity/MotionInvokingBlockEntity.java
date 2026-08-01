package ace.actually.pirates.blocks.entity;

import ace.actually.pirates.blocks.MotionInvokingBlock;
import ace.actually.pirates.util.ConfigUtils;
import ace.actually.pirates.util.EurekaCompat;
import ace.actually.pirates.Pirates;
import ace.actually.pirates.util.SailsCompat;
import net.fabricmc.loader.api.FabricLoader;
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
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.util.GameToPhysicsAdapter;

import java.util.List;

import static ace.actually.pirates.blocks.MotionInvokingBlock.COMPAT;
import static net.minecraft.state.property.Properties.HORIZONTAL_FACING;

@SuppressWarnings("UnstableApiUsage")
public class MotionInvokingBlockEntity extends BlockEntity {
    NbtList path = new NbtList();
    long nextInstruction = 0;
    //boolean isChecked = false;

    //variables below this line aren't serialised because they don't need to be.
    int[] target = new int[3]; //x,y,z of a point in space that the ship is "trying" to get to.
    double ldx = -1; //last distance tracked along the x-axis, from the target
    double ldz = -1; //last distance tracked along the z-axis, from the target


    private static int updateTicks = -1;

    public MotionInvokingBlockEntity(BlockPos pos, BlockState state) {
        super(Pirates.MOTION_INVOKING_BLOCK_ENTITY, pos, state);
    }

//    public void setCompat(String compat) {
//        this.compat = compat;
//        markDirty();
//    }

    public static void tick(World world, BlockPos pos, BlockState state, MotionInvokingBlockEntity be) {

//        if (!be.isChecked) {
//            state.with(COMPAT, 0);
//            if (Pirates.loadedCompats.sails && SailsCompat.checkHelm(world, pos)) {
//                state.with(COMPAT, 1);
//            }
//            if (Pirates.loadedCompats.eureka && EurekaCompat.checkHelm(world, pos)) {
//                state.with(COMPAT, 2);
//            }
//            world.setBlockState(pos, state, 10);
//            be.isChecked = true;
//        }

        if(!state.get(MotionInvokingBlock.ARMED)) return;

        //ensure compat value matches loaded dependencies and a helm is present
        if (state.get(COMPAT).equals(1)) {
            if (!Pirates.loadedCompats.sails) {
                state = state.with(COMPAT, 0);
                world.setBlockState(pos, state, 10);
                return;
            } else if (!SailsCompat.checkHelm(world, pos)) {
                MotionInvokingBlock.disarm(world, pos);
                return;
            }
        } else if (state.get(COMPAT).equals(2)) {
            if (!Pirates.loadedCompats.eureka) {
                state = state.with(COMPAT, 0);
                world.setBlockState(pos, state, 10);
                return;
            } else if (!EurekaCompat.checkHelm(world, pos)) {
                MotionInvokingBlock.disarm(world, pos);
                return;
            }
        }

        if(updateTicks==-1) {
            updateTicks = Integer.parseInt(ConfigUtils.config.getOrDefault("controlled-ship-updates","100"));

        }
        if (!world.isClient && world.getGameRules().getBoolean(Pirates.PIRATES_IS_LIVE_WORLD) && world.getTime() >= be.nextInstruction) {

            if (VSGameUtilsKt.isBlockInShipyard(world, pos)) {
                ChunkPos chunkPos = world.getChunk(pos).getPos();
                LoadedServerShip ship = VSGameUtilsKt.getShipObjectManagingPos((ServerWorld) world, chunkPos);

                if (ship != null) {
                    ship.setStatic(false);
                    SeatedControllingPlayer seatedControllingPlayer = ship.getAttachment(SeatedControllingPlayer.class);
                    if (seatedControllingPlayer == null && (world.getBlockState(pos.up()).contains(HORIZONTAL_FACING))) {
                        if (state.get(COMPAT).equals(1)) {
                            seatedControllingPlayer = new SeatedControllingPlayer(world.getBlockState(pos.up()).get(HORIZONTAL_FACING).getOpposite()); //not sure this is necessary
                        } else if (state.get(COMPAT).equals(2)) {
                            seatedControllingPlayer = new SeatedControllingPlayer(world.getBlockState(pos.up()).get(HORIZONTAL_FACING).getOpposite());
                        }
                        ship.setAttachment(SeatedControllingPlayer.class, seatedControllingPlayer);
                    }

                    if(world.getTimeOfDay()%updateTicks==0) {
                        if(be.path.isEmpty()) {
                            List<Ship> ships = VSGameUtilsKt.getAllShips(world).stream().filter(a-> {
                                if(a.getId()==ship.getId()) return false;
                                Vector3dc f1 = ship.getTransform().getPositionInWorld();
                                Vector3dc f2 = a.getTransform().getPositionInWorld();
                                return f1.distanceSquared(f2)<Pirates.pursuitDistance;
                            }).toList();
                            if(!ships.isEmpty()) {
                                Vector3dc o = ships.get(0).getTransform().getPositionInWorld();
                                be.setTarget(new int[]{(int) o.x(), (int) o.y(), (int) o.z()});
                            }
                        }
                        else {
                            int[] v = be.path.getIntArray(0);
                            be.setTarget(v);
                            Vector3dc f1 = ship.getTransform().getPositionInWorld();
                            Vector3dc f2 = new Vector3d(v[0],v[1],v[2]);
                            if(f1.distanceSquared(f2)<100) {
                                NbtIntArray nbtInts = (NbtIntArray) be.path.remove(0);
                                be.path.add(nbtInts);
                            }
                        }
                    }

                    switch (state.get(COMPAT)) {
                        case 1 -> SailsCompat.moveTowards(be,seatedControllingPlayer,ship);
                        case 2 -> EurekaCompat.moveTowards(be,seatedControllingPlayer,ship);
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
        super.writeNbt(nbt);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        nextInstruction = nbt.getLong("nextInstruction");
        if(nbt.contains("path")) {
            path = (NbtList) nbt.get("path");
        }

        if(nbt.contains("target")) {
            target = nbt.getIntArray("target");
        }

    }

    public void setTarget(int[] target) {
        this.target = target;
        markDirty();
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
    public void addPathNode(BlockPos pos) {
        this.path.add(new NbtIntArray(new int[]{pos.getX(),pos.getY(),pos.getZ()}));
        markDirty();
    }

    /**
     * This method uses bases VS things to effectively create circles.
     * the circles arent very good. TODO: Make the circles good
     * @param ship
     */
    private void moveShipForward(LoadedServerShip ship) {
        double mass = ship.getInertiaData().getMass();
        Vector3d qdc = ship.getTransform().getShipToWorldRotation().getEulerAnglesZXY(new Vector3d()).normalize().mul(mass*10);
        qdc = new Vector3d(-qdc.x,0,-qdc.z);
        GameToPhysicsAdapter gtpa = ValkyrienSkiesMod.getOrCreateGTPA(getWorld().getRegistryKey().getValue().toString());

        if(gtpa!=null) {
            Vector3dc v3dc = ship.getInertiaData().getCenterOfMassInShip();
            Vector3d loc = new Vector3d(v3dc.x()+1,v3dc.y(),v3dc.z()+1);
            //if(world instanceof ServerWorld serverWorld)
            //{
            //    serverWorld.spawnParticles(ParticleTypes.BUBBLE,loc.x,loc.y,loc.z,1,0,0,0,0);
            //}
            gtpa.applyInvariantForceToPos(ship.getId(), qdc, loc.sub(ship.getTransform().getPositionInShip()));
        }
    }
}
