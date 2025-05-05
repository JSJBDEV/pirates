package ace.actually.pirates.items;

import ace.actually.pirates.Pirates;
import ace.actually.pirates.blocks.CannonPrimingBlock;
import ace.actually.pirates.entities.friendly_pirate.FriendlyPirateEntity;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;

public class CannoneerItem extends Item {
    public CannoneerItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if(context.getWorld() instanceof ServerWorld world && context.getWorld().getBlockState(context.getBlockPos()).isOf(Pirates.CANNON_PRIMING_BLOCK))
        {
            BlockPos pos = context.getBlockPos();
            FriendlyPirateEntity fpe = new FriendlyPirateEntity(world,pos);
            BlockState state = world.getBlockState(pos);
            world.setBlockState(pos,state.with(CannonPrimingBlock.DISARMED,false));
            pos = pos.offset(state.get(Properties.FACING).getOpposite());
            world.spawnEntity(fpe);
            fpe.teleport(pos.getX(),pos.getY(),pos.getZ());
            fpe.genCustomName(world);
            context.getStack().decrement(1);
        }
        return super.useOnBlock(context);
    }
}
