package ace.actually.pirates.util;
import ace.actually.pirates.Pirates;
import ace.actually.pirates.sound.ModSounds;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

/**
 * A dispenser behavior that spawns a projectile with velocity in front of the dispenser.
 */
public abstract class CannonDispenserBehavior
        extends DefaultDispenseItemBehavior {
    @Override
    public ItemStack execute(BlockSource pointer, ItemStack stack) {
        ServerLevel world = pointer.getLevel();
        Position position = DispenserBlock.getDispensePosition(pointer);
        Direction direction = pointer.getBlockState().getValue(DispenserBlock.FACING);
        Projectile projectileEntity = this.createProjectile(world, position, stack);
        projectileEntity.shoot(direction.getStepX(), (float)direction.getStepY() + 0.15f, direction.getStepZ(), this.getForce(), this.getVariation() / 2);
        world.addFreshEntity(projectileEntity);

        Ship ship = VSGameUtilsKt.getShipManagingPos(world, pointer.getPos());
        if (ship != null) {
            projectileEntity.addDeltaMovement(VectorConversionsMCKt.toMinecraft(ship.getVelocity()).scale(1/60.0));
        }

        if (!world.isClientSide) {
            int xmod = 0;
            int ymod = 0;
            int zmod = 0;

            switch (direction) {
                case NORTH -> zmod = -1;
                case EAST -> xmod = 1;
                case SOUTH -> zmod = 1;
                case WEST -> xmod = -1;
                case UP -> ymod = 1;
                case DOWN -> ymod = -1;
            }
            for(int i = 0; i < 40; ++i) {
                world.sendParticles(ParticleTypes.CLOUD, position.x() + xmod + (2 * world.random.nextDouble()) - 1, position.y() + ymod + (2 * world.random.nextDouble()) - 0.8, position.z() + zmod + (2 * world.random.nextDouble()) - 1, 1, 0.0, 0.0, 0.0, 0.005);
            }
        }
        stack.shrink(1);
        return stack;
    }

    @Override
    protected void playSound(BlockSource pointer) {
        pointer.getLevel().playSound(null, pointer.getPos().getX(), pointer.getPos().getY(), pointer.getPos().getZ(), ModSounds.CANNONBALL_SHOT, SoundSource.BLOCKS, 1F, 1F);
    }


    protected abstract Projectile createProjectile(Level var1, Position var2, ItemStack var3);

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
        return Pirates.cannonRange;
    }
}
