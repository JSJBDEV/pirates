package ace.actually.pirates.items;

import ace.actually.pirates.Pirates;
import ace.actually.pirates.blocks.CannonPrimingBlock;
import ace.actually.pirates.entities.friendly_pirate.FriendlyPirateEntity;
import ace.actually.pirates.util.DisarmUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;

public class ContractItem extends Item {
    Block jobsite;
    String jobname;
    public ContractItem(Block jobsite, String jobname) {
        super(new Settings());
        this.jobname=jobname;
        this.jobsite=jobsite;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if(context.getWorld() instanceof ServerWorld world && context.getWorld().getBlockState(context.getBlockPos()).isOf(jobsite))
        {
            BlockPos pos = context.getBlockPos();
            FriendlyPirateEntity fpe = new FriendlyPirateEntity(world,pos);
            fpe.setPirateJob(jobname);
            BlockState state = world.getBlockState(pos);
            DisarmUtils.rearm(world,pos);

            BlockPos spos;
            if(state.contains(Properties.FACING))
            {
                spos = pos.offset(state.get(Properties.FACING).getOpposite());
            }
            else
            {
                spos = pos.up();
            }
            world.spawnEntity(fpe);
            fpe.teleport(spos.getX(),spos.getY(),spos.getZ());
            fpe.genCustomName(world);
            context.getStack().decrement(1);

        }
        return super.useOnBlock(context);
    }
}
