package ace.actually.pirates.shipai;

import ace.actually.pirates.blocks.MotionInvokingBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ShipAI {


    void drive(World world, BlockPos pos, BlockState state, MotionInvokingBlockEntity be);
}
