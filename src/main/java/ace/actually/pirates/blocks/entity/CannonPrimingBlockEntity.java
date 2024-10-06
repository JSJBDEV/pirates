package ace.actually.pirates.blocks.entity;

import ace.actually.pirates.Pirates;
import ace.actually.pirates.blocks.CannonPrimingBlock;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.state.property.Properties;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

public class CannonPrimingBlockEntity extends BlockEntity {

    public int cooldown = 0;
    private int lastCooldown = 40;
    public final double randomRotation;


    public CannonPrimingBlockEntity(BlockPos pos, BlockState state) {
        super(Pirates.CANNON_PRIMING_BLOCK_ENTITY, pos, state);
        randomRotation = Math.random() * 2.5;
    }

    public void tick(World world, BlockPos pos, BlockState state, CannonPrimingBlockEntity be) {


        if (!world.isClient && cooldown == 0) {

            if ((checkShouldFire(world, pos, state) && !state.get(CannonPrimingBlock.DISARMED))){


                fire(world, pos, state);
            } else {
                cooldown = 3;
            }
        } else if (cooldown == 4) {
            if (!world.isReceivingRedstonePower(pos)) {
                cooldown --;
            }
        } else  {
            cooldown --;
        }

        if (state.get(RedstoneLampBlock.LIT) && lastCooldown - cooldown == 10) {
            world.setBlockState(pos, state.with(RedstoneLampBlock.LIT, false));
        }
    }

    public void fire(World world, BlockPos pos, BlockState state, int cooldown) {
        if (this.cooldown > 3) return;
        world.setBlockState(pos, state.with(RedstoneLampBlock.LIT, true));
        this.cooldown = cooldown;
        lastCooldown = this.cooldown;

        BlockPos ahead = pos.add(state.get(Properties.FACING).getVector());
        if (world.getBlockState(ahead).isOf(Pirates.DISPENSER_CANNON_BLOCK)) {
            world.scheduleBlockTick(ahead, world.getBlockState(ahead).getBlock(), 4);
        }
    }

    public void fire(World world, BlockPos pos, BlockState state) {
        fire(world, pos, state, 40 + (int) (Math.random() * 20));
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
