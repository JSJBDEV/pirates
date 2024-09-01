package ace.actually.pirates.blocks;

import ace.actually.pirates.Pirates;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.joml.Vector3d;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

import java.util.*;

public class CannonPrimingBlockEntity extends BlockEntity {

    public int cooldown = 0;
    private int lastCooldown = 40;
    private boolean shouldCheckIfShouldAddToList;
    private static HashMap<Long, List<BlockPos>> primingBlocksDataOnShips;

    private long shipID = -1;

    public CannonPrimingBlockEntity(BlockPos pos, BlockState state) {
        super(Pirates.CANNON_PRIMING_BLOCK_ENTITY, pos, state);
        shouldCheckIfShouldAddToList = true;
    }

    public static void initHashMap() {
        primingBlocksDataOnShips = new HashMap<>();
    }

    public static void clearHashMap() {
        primingBlocksDataOnShips.clear();
    }

    public static void disarmNearest(Long shipID, Vec3d worldPos, World world) {
        List<BlockPos> cannonsShip = primingBlocksDataOnShips.get(shipID);
        BlockPos closest = null;

        if (cannonsShip == null) return;

        for (BlockPos cannonShip : cannonsShip) {
            Vector3d cannonWorld = (VSGameUtilsKt.getWorldCoordinates(world, cannonShip, VectorConversionsMCKt.toJOML(cannonShip.toCenterPos())));

            if (closest != null) {
                Vector3d closestWorld = (VSGameUtilsKt.getWorldCoordinates(world, closest, VectorConversionsMCKt.toJOML(closest.toCenterPos())));
                if (Vector3d.distance(worldPos.x, worldPos.y, worldPos.z, cannonWorld.x, cannonWorld.y, cannonWorld.z) < Vector3d.distance(worldPos.x, worldPos.y, worldPos.z, closestWorld.x, closestWorld.y, closestWorld.z)){
                    closest = cannonShip;
                }
            } else {
                closest = cannonShip;
            }
        }
        if (closest != null) {
            world.setBlockState(closest, world.getBlockState(closest).with(CannonPrimingBlock.DISARMED, true));
            primingBlocksDataOnShips.get(shipID).remove(closest);
        }
    }




    public void tick(World world, BlockPos pos, BlockState state, CannonPrimingBlockEntity be) {
        if (shouldCheckIfShouldAddToList) addToList(world, pos, state);

        if (cooldown % 3 == 0) {
            if (!world.isClient() && !state.get(Properties.DISARMED)) {
                ((ServerWorld) world).spawnParticles(ParticleTypes.WITCH, pos.getX() + Math.random(), pos.getY() + Math.random(), pos.getZ() + Math.random(), 1, 0, 0, 0, 0);
            }
        }

        if (!world.isClient && cooldown == 0) {

            if (checkShouldFire(world, pos, state) && !state.get(CannonPrimingBlock.DISARMED)){

                System.out.println(primingBlocksDataOnShips.toString());


                world.setBlockState(pos, state.with(RedstoneLampBlock.LIT, true));
                cooldown = 40 + (int) (Math.random() * 20);
                lastCooldown = cooldown;
            } else {
                cooldown = 3;
            }
        } else {
            cooldown --;
        }

        if (state.get(RedstoneLampBlock.LIT) && lastCooldown - cooldown == 10) {
            world.setBlockState(pos, state.with(RedstoneLampBlock.LIT, false));
        }
    }

    @Override
    public void markRemoved() {
        removeFromList();
        super.markRemoved();
    }

    private void removeFromList() {
        if (shipID != -1) {
            if (primingBlocksDataOnShips.containsKey(shipID)){
                primingBlocksDataOnShips.get(shipID).remove(pos);
            }
        }
    }

    private void addToList(World world, BlockPos pos, BlockState state) {
        shouldCheckIfShouldAddToList = false;
        if (!world.isClient()) {
            if (VSGameUtilsKt.isBlockInShipyard(world, pos) && world.getBlockState(pos).isOf(Pirates.CANNON_PRIMING_BLOCK)) {
                if (world.getBlockState(pos).get(CannonPrimingBlock.DISARMED)) return;
                Long shipID = Objects.requireNonNull(VSGameUtilsKt.getShipManagingPos(world, pos)).getId();
                this.shipID = shipID;
                if (primingBlocksDataOnShips.containsKey(shipID)){
                    if (!primingBlocksDataOnShips.get(shipID).contains(pos)) {
                        primingBlocksDataOnShips.get(shipID).add(pos);
                    }
                } else {
                    primingBlocksDataOnShips.put(shipID, new LinkedList<>());
                    primingBlocksDataOnShips.get(shipID).add(pos);
                }
            }
        }
    }

    private static boolean checkShouldFire(World world, BlockPos pos, BlockState state) {
        Vec3i raycastStart = state.get(Properties.FACING).getVector();
        if(!(world.getBlockState(pos.add(raycastStart)).getBlock() instanceof DispenserBlock)|| world.getBlockState(pos.add(raycastStart)).get(Properties.FACING) != state.get(Properties.FACING)){
            return false;
        }

        RaycastContext context = new RaycastContext(
                VSGameUtilsKt.toWorldCoordinates(world, Vec3d.ofCenter(pos.add(raycastStart.multiply(2)))),
                VSGameUtilsKt.toWorldCoordinates(world, Vec3d.ofCenter(pos.add(raycastStart.multiply(32)))),
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                null);

        BlockHitResult result = world.raycast(context);
        return  (VSGameUtilsKt.isBlockInShipyard(world, result.getBlockPos()));
    }




}
