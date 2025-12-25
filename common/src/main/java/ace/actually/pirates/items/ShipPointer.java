package ace.actually.pirates.items;

import ace.actually.pirates.Pirates;
import ace.actually.pirates.blocks.entity.MotionInvokingBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class ShipPointer extends Item {
    public ShipPointer(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if(context.getLevel() instanceof ServerLevel world  && context.getHand()==InteractionHand.MAIN_HAND)
        {
            if(world.getBlockState(context.getClickedPos()).is(Pirates.MOTION_INVOKING_BLOCK.get()))
            {
                CompoundTag compound = new CompoundTag();
                BlockPos v = context.getClickedPos();
                compound.putIntArray("mib",new int[]{v.getX(),v.getY(),v.getZ()});

                context.getPlayer().getItemInHand(InteractionHand.MAIN_HAND).setTag(compound);
            }
            else
            {

                if(context.getItemInHand().hasTag() && context.getItemInHand().getTag().contains("mib"))
                {
                    CompoundTag compound = context.getItemInHand().getTag();
                    int[] v = compound.getIntArray("mib");
                    MotionInvokingBlockEntity be = (MotionInvokingBlockEntity) world.getBlockEntity(new BlockPos(v[0],v[1],v[2]));
                    BlockPos pos = context.getClickedPos();
                    be.setTarget(new int[]{pos.getX(),pos.getY(),pos.getZ()});
                }
                else
                {
                    context.getPlayer().sendSystemMessage(Component.translatable("text.pirates.need_mib"));
                }
            }

        }
        return super.useOn(context);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        if(InteractionHand.MAIN_HAND==hand && !world.isClientSide)
        {
            CompoundTag compound = user.getMainHandItem().getTag();
            int[] v = compound.getIntArray("mib");
            MotionInvokingBlockEntity be = (MotionInvokingBlockEntity) world.getBlockEntity(new BlockPos(v[0],v[1],v[2]));
            int[] t = be.getTarget();
            user.sendSystemMessage(Component.translatable("text.pirates.target").append(Component.nullToEmpty(" "+t[0]+" "+t[1]+" "+t[2])));
        }
        return super.use(world, user, hand);
    }

}
