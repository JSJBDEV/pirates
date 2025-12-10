package ace.actually.pirates.items;

import ace.actually.pirates.Pirates;
import ace.actually.pirates.blocks.entity.MotionInvokingBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.Tag;
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

public class ShipPather extends Item {
    public ShipPather(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if(context.getLevel() instanceof ServerLevel world  && context.getHand()==InteractionHand.MAIN_HAND)
        {
            if(world.getBlockState(context.getClickedPos()).is(Pirates.MOTION_INVOKING_BLOCK))
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
                    be.addPathNode(new BlockPos(pos.getX(),be.getBlockPos().getY(),pos.getZ()));
                    context.getPlayer().sendSystemMessage(Component.translatable("text.pirates.pather.add"));
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
        if(InteractionHand.MAIN_HAND==hand && !world.isClientSide && user.getMainHandItem().hasTag() && user.getMainHandItem().getTag().contains("mib"))
        {
            CompoundTag compound = user.getMainHandItem().getTag();
            int[] v = compound.getIntArray("mib");
            MotionInvokingBlockEntity be = (MotionInvokingBlockEntity) world.getBlockEntity(new BlockPos(v[0],v[1],v[2]));
            int[] t = be.getTarget();
            user.sendSystemMessage(Component.translatable("text.pirates.target").append(Component.nullToEmpty(" "+t[0]+" "+t[1]+" "+t[2])));
            user.sendSystemMessage(Component.translatable("text.pirates.paths"));
            for(Tag element: be.getPath())
            {
                if(element.getId()==Tag.TAG_INT_ARRAY)
                {
                    int[] ints = ((IntArrayTag) element).getAsIntArray();
                    user.sendSystemMessage(Component.nullToEmpty(ints[0]+" "+ints[1]+" "+ints[2]));
                }
            }
        }
        return super.use(world, user, hand);
    }
}
