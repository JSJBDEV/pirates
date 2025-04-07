package ace.actually.pirates.items;

import ace.actually.pirates.Pirates;
import ace.actually.pirates.blocks.entity.MotionInvokingBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ShipPointer extends Item {
    public ShipPointer(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if(context.getWorld() instanceof ServerWorld world  && context.getHand()==Hand.MAIN_HAND)
        {
            if(world.getBlockState(context.getBlockPos()).isOf(Pirates.MOTION_INVOKING_BLOCK))
            {
                NbtCompound compound = new NbtCompound();
                BlockPos v = context.getBlockPos();
                compound.putIntArray("mib",new int[]{v.getX(),v.getY(),v.getZ()});

                context.getPlayer().getStackInHand(Hand.MAIN_HAND).setNbt(compound);
            }
            else
            {

                if(context.getStack().hasNbt() && context.getStack().getNbt().contains("mib"))
                {
                    NbtCompound compound = context.getStack().getNbt();
                    int[] v = compound.getIntArray("mib");
                    MotionInvokingBlockEntity be = (MotionInvokingBlockEntity) world.getBlockEntity(new BlockPos(v[0],v[1],v[2]));
                    BlockPos pos = context.getBlockPos();
                    be.setTarget(new int[]{pos.getX(),pos.getY(),pos.getZ()});
                }
                else
                {
                    context.getPlayer().sendMessage(Text.translatable("text.pirates.need_mib"));
                }
            }

        }
        return super.useOnBlock(context);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if(Hand.MAIN_HAND==hand && !world.isClient)
        {
            NbtCompound compound = user.getMainHandStack().getNbt();
            int[] v = compound.getIntArray("mib");
            MotionInvokingBlockEntity be = (MotionInvokingBlockEntity) world.getBlockEntity(new BlockPos(v[0],v[1],v[2]));
            int[] t = be.getTarget();
            user.sendMessage(Text.translatable("text.pirates.target").append(Text.of(" "+t[0]+" "+t[1]+" "+t[2])));
        }
        return super.use(world, user, hand);
    }

}
