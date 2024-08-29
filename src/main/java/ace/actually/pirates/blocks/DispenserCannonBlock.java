package ace.actually.pirates.blocks;

import ace.actually.pirates.Pirates;
import ace.actually.pirates.entities.ShotEntity;
import ace.actually.pirates.util.CannonDispenserBehavior;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

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

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {

        if (!world.getBlockState(pos.add(state.get(Properties.FACING).getOpposite().getVector())).isOf(Pirates.CANNON_PRIMING_BLOCK) || world.getBlockState(pos.add(state.get(Properties.FACING).getOpposite().getVector())).get(Properties.FACING) != state.get(Properties.FACING)) {
            world.setBlockState(pos, Blocks.DISPENSER.getDefaultState().with(Properties.FACING, state.get(FACING)), 3);
        }

        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

}
