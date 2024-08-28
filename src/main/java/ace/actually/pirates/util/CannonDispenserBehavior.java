package ace.actually.pirates.util;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.world.World;

/**
 * A dispenser behavior that spawns a projectile with velocity in front of the dispenser.
 */
public abstract class CannonDispenserBehavior
        extends ItemDispenserBehavior {
    @Override
    public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
        ServerWorld world = pointer.getWorld();
        Position position = DispenserBlock.getOutputLocation(pointer);
        Direction direction = pointer.getBlockState().get(DispenserBlock.FACING);
        ProjectileEntity projectileEntity = this.createProjectile(world, position, stack);
        projectileEntity.setVelocity(direction.getOffsetX(), (float)direction.getOffsetY() + 0.2f, direction.getOffsetZ(), this.getForce() + 0.4f, this.getVariation() / 2);
        world.spawnEntity(projectileEntity);
        if (!world.isClient) {
            int xmod = 0;
            int ymod = 0;
            int zmod = 0;
            if (direction == Direction.NORTH) {
                zmod = -1;
            } else if (direction == Direction.EAST) {
                xmod = 1;
            } else if (direction == Direction.SOUTH) {
                zmod = 1;
            } else if (direction == Direction.WEST) {
                xmod = -1;
            } else if (direction == Direction.UP) {
                ymod = 1;
            } else if (direction == Direction.DOWN) {
                ymod = -1;
            }
            for(int i = 0; i < 5; ++i) {
                world.spawnParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, position.getX() + xmod + world.random.nextDouble() - 0.5, position.getY() + ymod, position.getZ() + zmod + world.random.nextDouble() - 0.5, 1, 0.0, 0.0, 0.0, 0.005);
            }
        }
        stack.decrement(1);
        return stack;
    }

    @Override
    protected void playSound(BlockPointer pointer) {
        pointer.getWorld().playSound(null, pointer.getPos().getX(), pointer.getPos().getY(), pointer.getPos().getZ(), SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.BLOCKS, 0.5F, 1F);
    }


    protected abstract ProjectileEntity createProjectile(World var1, Position var2, ItemStack var3);

    /**
     * {@return the variation of a projectile's velocity when spawned}
     */
    protected float getVariation() {
        return 6.0f;
    }

    /**
     * {@return the force of a projectile's velocity when spawned}
     */
    protected float getForce() {
        return 1.1f;
    }
}
