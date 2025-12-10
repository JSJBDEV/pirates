package ace.actually.pirates.util;

import ace.actually.pirates.Pirates;
import ace.actually.pirates.blocks.CannonPrimingBlock;
import ace.actually.pirates.blocks.MotionInvokingBlock;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class DisarmUtils {
    public static void disarm(Level world, BlockPos blockToDisable) {
        if (!Objects.equals(blockToDisable, new BlockPos(0, 0, 0))) {

            if (world.getBlockState(blockToDisable).is(Pirates.CANNON_PRIMING_BLOCK)) {
                CannonPrimingBlock.disarm(world, blockToDisable);
            } else if (world.getBlockState(blockToDisable).is(Pirates.MOTION_INVOKING_BLOCK)) {
                MotionInvokingBlock.disarm(world, blockToDisable);

            }
        }
    }
    public static void rearm(Level world, BlockPos blockToDisable)
    {
        if (!blockToDisable.equals(BlockPos.ZERO))
        {
            if (world.getBlockState(blockToDisable).is(Pirates.CANNON_PRIMING_BLOCK)) {
                world.setBlockAndUpdate(blockToDisable,world.getBlockState(blockToDisable).setValue(CannonPrimingBlock.DISARMED,false));
            } else if (world.getBlockState(blockToDisable).is(Pirates.MOTION_INVOKING_BLOCK)) {
                world.setBlockAndUpdate(blockToDisable,world.getBlockState(blockToDisable).setValue(MotionInvokingBlock.ARMED,true));
            }
        }
    }
}
