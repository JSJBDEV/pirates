package ace.actually.pirates.items;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

public class RaycastingItem extends Item {
    public RaycastingItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        HitResult result = user.raycast(100,1,false);
        if(result instanceof BlockHitResult blockHitResult)
        {
            user.sendMessage(Text.of("("+world.getTime()+") "+world.getBlockState(blockHitResult.getBlockPos()).toString()+" at "+blockHitResult.getBlockPos().toString()),false);
        }
        return super.use(world, user, hand);
    }
}
