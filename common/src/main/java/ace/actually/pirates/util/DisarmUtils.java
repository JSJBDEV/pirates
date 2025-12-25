package ace.actually.pirates.util;

import ace.actually.pirates.Pirates;
import ace.actually.pirates.blocks.CannonPrimingBlock;
import ace.actually.pirates.blocks.MotionInvokingBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.Objects;

public class DisarmUtils {
    public static void disarm(Level world, BlockPos blockToDisable) {
        if (!Objects.equals(blockToDisable, new BlockPos(0, 0, 0))) {

            if (world.getBlockState(blockToDisable).is(Pirates.CANNON_PRIMING_BLOCK.get())) {
                CannonPrimingBlock.disarm(world, blockToDisable);
            } else if (world.getBlockState(blockToDisable).is(Pirates.MOTION_INVOKING_BLOCK.get())) {
                MotionInvokingBlock.disarm(world, blockToDisable);

            }
        }
    }
    public static void rearm(Level world, BlockPos blockToDisable)
    {
        if (!blockToDisable.equals(BlockPos.ZERO))
        {
            if (world.getBlockState(blockToDisable).is(Pirates.CANNON_PRIMING_BLOCK.get())) {
                world.setBlockAndUpdate(blockToDisable,world.getBlockState(blockToDisable).setValue(CannonPrimingBlock.DISARMED,false));
            } else if (world.getBlockState(blockToDisable).is(Pirates.MOTION_INVOKING_BLOCK.get())) {
                world.setBlockAndUpdate(blockToDisable,world.getBlockState(blockToDisable).setValue(MotionInvokingBlock.ARMED,true));
            }
        }
    }
}
