package ace.actually.pirates.util;

import ace.actually.pirates.Pirates;
import ace.actually.pirates.blocks.CannonPrimingBlock;
import ace.actually.pirates.blocks.MotionInvokingBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Objects;

public class DisarmUtils {
    public static void disarm(World world, BlockPos blockToDisable) {
        if (!Objects.equals(blockToDisable, new BlockPos(0, 0, 0))) {

            if (world.getBlockState(blockToDisable).isOf(Pirates.CANNON_PRIMING_BLOCK)) {
                CannonPrimingBlock.disarm(world, blockToDisable);
            } else if (world.getBlockState(blockToDisable).isOf(Pirates.MOTION_INVOKING_BLOCK)) {
                MotionInvokingBlock.disarm(world, blockToDisable);

            }
        }
    }
    public static void rearm(World world, BlockPos blockToDisable)
    {
        if (!blockToDisable.equals(BlockPos.ORIGIN))
        {
            if (world.getBlockState(blockToDisable).isOf(Pirates.CANNON_PRIMING_BLOCK)) {
                world.setBlockState(blockToDisable,world.getBlockState(blockToDisable).with(CannonPrimingBlock.DISARMED,false));
            } else if (world.getBlockState(blockToDisable).isOf(Pirates.MOTION_INVOKING_BLOCK)) {
                world.setBlockState(blockToDisable,world.getBlockState(blockToDisable).with(MotionInvokingBlock.ARMED,true));
            }
        }
    }
}
