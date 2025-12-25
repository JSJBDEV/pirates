package ace.actually.pirates.items;

import ace.actually.pirates.entities.friendly_pirate.FriendlyPirateEntity;
import ace.actually.pirates.util.DisarmUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class ContractItem extends Item {
    Block jobsite;
    String jobname;
    public ContractItem(Block jobsite, String jobname) {
        super(new Properties());
        this.jobname=jobname;
        this.jobsite=jobsite;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if(context.getLevel() instanceof ServerLevel world && context.getLevel().getBlockState(context.getClickedPos()).is(jobsite))
        {
            BlockPos pos = context.getClickedPos();
            FriendlyPirateEntity fpe = new FriendlyPirateEntity(world,pos);
            fpe.setPirateJob(jobname);
            BlockState state = world.getBlockState(pos);
            DisarmUtils.rearm(world,pos);

            BlockPos spos;
            if(state.hasProperty(BlockStateProperties.FACING))
            {
                spos = pos.relative(state.getValue(BlockStateProperties.FACING).getOpposite());
            }
            else
            {
                spos = pos.above();
            }
            world.addFreshEntity(fpe);
            fpe.teleportToWithTicket(spos.getX(),spos.getY(),spos.getZ());
            fpe.genCustomName(world);
            context.getItemInHand().shrink(1);

        }
        return super.useOn(context);
    }
}
