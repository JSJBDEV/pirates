package ace.actually.pirates.blocks;

import ace.actually.pirates.Pirates;
import ace.actually.pirates.entities.ShotEntity;
import ace.actually.pirates.util.CannonDispenserBehavior;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.*;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

import java.util.Objects;

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
                    ShotEntity qentity = Util.make(new ShotEntity(Pirates.SHOT_ENTITY_TYPE,world,null,Pirates.CANNONBALL_ENT,2,""), (entity) -> {});
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

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        DispenserBlockEntity entity = new DispenserBlockEntity(pos, state);
        entity.setCustomName(Text.of("Cannon"));
        return entity;
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {}

    @Override
    public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
        return new ItemStack(Items.DISPENSER);
    }

}
