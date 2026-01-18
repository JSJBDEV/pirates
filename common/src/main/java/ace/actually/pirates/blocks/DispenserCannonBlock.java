package ace.actually.pirates.blocks;

import ace.actually.pirates.Pirates;
import ace.actually.pirates.entities.shot.ShotEntity;
import ace.actually.pirates.util.CannonDispenserBehavior;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

public class DispenserCannonBlock extends DispenserBlock {
    public DispenserCannonBlock(Properties settings) {
        super(settings);
    }

    @Override
    protected DispenseItemBehavior getDispenseMethod(ItemStack stack) {
        if(stack.getItem() == Pirates.CANNONBALL.get()){
            return new CannonDispenserBehavior() {
                @Override
                protected Projectile createProjectile(Level world, Position position, ItemStack stack) {
                    ShotEntity qentity = Util.make(new ShotEntity(Pirates.SHOT_ENTITY_TYPE.get(),world,null,Pirates.CANNONBALL_ENT.get(),6,""), (entity) -> {});
                    qentity.setPos(new Vec3(position.x(),position.y(),position.z()));
                    return qentity;
                }
            };
        }
        if(stack.getItem() == Pirates.FIRE_CANNONBALL.get()){
            return new CannonDispenserBehavior() {
                @Override
                protected Projectile createProjectile(Level world, Position position, ItemStack stack) {
                    ShotEntity qentity = Util.make(new ShotEntity(Pirates.SHOT_ENTITY_TYPE.get(),world,null,Pirates.CANNONBALL_ENT.get(),3,"fire"), (entity) -> {});
                    qentity.setPos(new Vec3(position.x(),position.y(),position.z()));
                    return qentity;
                }
            };
        }
        if(stack.getItem() == Pirates.WEIGHTED_CANNONBALL.get()){
            return new CannonDispenserBehavior() {
                @Override
                protected Projectile createProjectile(Level world, Position position, ItemStack stack) {
                    ShotEntity qentity = Util.make(new ShotEntity(Pirates.SHOT_ENTITY_TYPE.get(),world,null,Pirates.CANNONBALL_ENT.get(),3,"heavy"), (entity) -> {});
                    qentity.setPos(new Vec3(position.x(),position.y(),position.z()));
                    return qentity;
                }
            };
        }
        return super.getDispenseMethod(stack);
    }

    @SuppressWarnings("deprecation")
    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos) {

        if (!world.getBlockState(pos.offset(state.getValue(BlockStateProperties.FACING).getOpposite().getNormal())).is(Pirates.CANNON_PRIMING_BLOCK.get()) || world.getBlockState(pos.offset(state.getValue(BlockStateProperties.FACING).getOpposite().getNormal())).getValue(BlockStateProperties.FACING) != state.getValue(BlockStateProperties.FACING)) {
            world.setBlock(pos, Blocks.DISPENSER.defaultBlockState().setValue(BlockStateProperties.FACING, state.getValue(FACING)), 3);
        }

        return super.updateShape(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        DispenserBlockEntity entity = new DispenserBlockEntity(pos, state);
        entity.setCustomName(Component.nullToEmpty("Cannon"));
        return entity;
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {}

    @Override
    public ItemStack getCloneItemStack(BlockGetter world, BlockPos pos, BlockState state) {
        return new ItemStack(Items.DISPENSER);
    }

}
