package ace.actually.pirates.blocks;

import ace.actually.pirates.Pirates;
import ace.actually.pirates.entities.ShotEntity;
import ace.actually.pirates.util.CannonDispenserBehavior;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Util;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class DispenserCannonBlock extends DispenserBlock {
    public DispenserCannonBlock(AbstractBlock.Settings settings) {
        super(settings);
    }

    @Override
    protected DispenserBehavior getBehaviorForItem(ItemStack stack) {
        if(stack.getItem() == Pirates.CANNONBALL){
            return new CannonDispenserBehavior() {
                @Override
                protected ProjectileEntity createProjectile(World world, Position position, ItemStack stack) {
                    ShotEntity qentity = Util.make(new ShotEntity(Pirates.SHOT_ENTITY_TYPE,world,null,Pirates.CANNONBALL,2,""), (entity) -> {
                        entity.setItem(stack);
                    });
                    qentity.setPosition(new Vec3d(position.getX(),position.getY(),position.getZ()));
                    return qentity;
                }
            };
        }
        return super.getBehaviorForItem(stack);
    }

}
