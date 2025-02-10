package ace.actually.pirates.util;
import ace.actually.pirates.sound.ModSounds;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.world.World;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;
import net.minecraft.particle.DustParticleEffect;

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
        projectileEntity.setVelocity(direction.getOffsetX(), (float)direction.getOffsetY() + 0.15f, direction.getOffsetZ(), this.getForce() + 0.6f, this.getVariation() / 2);
        world.spawnEntity(projectileEntity);

        Ship ship = VSGameUtilsKt.getShipManagingPos(world, pointer.getPos());
        if (ship != null) {
            projectileEntity.addVelocity(VectorConversionsMCKt.toMinecraft(ship.getVelocity()).multiply(1/60.0));
        }

        if (!world.isClient) {
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
            // DustParticleEffect dustEffect = new DustParticleEffect(new Vec3f(1.0f, 0.0f, 0.0f), 1.0f); // Red dust, size 1.0
            for (int i = 0; i < 40; ++i) {
                world.spawnParticles(ParticleTypes.FLAME,
                        position.getX() + xmod * (0.5 + world.random.nextDouble() * 1.5),  // 🔹 Extend forward 1.5 - 3 blocks
                        position.getY() + ymod + (world.random.nextDouble() * 1.0) - 0.5,  // 🔹 Small vertical variation (-0.5 to +0.5)
                        position.getZ() + zmod * (0.5 + world.random.nextDouble() * 1.5),  // 🔹 Extend forward in Z-axis as well
                        3, (world.random.nextDouble() * 0.3) - 0.15,  // 🔹 Narrow X spread (-0.15 to +0.15)
                        (world.random.nextDouble() * 0.1) - 0.05,  // 🔹 Keep Y spread small (-0.05 to +0.05)
                        (world.random.nextDouble() * 0.3) - 0.15,  // 🔹 Narrow Z spread (-0.15 to +0.15)
                        0.02);

                // 💨 LARGE_SMOKE bounding box is TWICE as large as FLAME
                double explosionX = position.getX() + xmod * (3.5 + world.random.nextDouble() * 2);  // Move explosion ahead with variation
                double explosionY = position.getY() + ymod + (world.random.nextDouble() * 3.0) - 1.0;  // Twice the height variation
                double explosionZ = position.getZ() + zmod * (3.5 + world.random.nextDouble() * 2);  // Move explosion ahead with variation

                world.spawnParticles(ParticleTypes.CLOUD,
                        explosionX + (4 * world.random.nextDouble()) - 2,  // Twice the X spread
                        explosionY,
                        explosionZ + (4 * world.random.nextDouble()) - 2,  // Twice the Z spread
                        20, 0.0, 0.0, 0.0, 0.0);
            }
            // better particle, add no damage explosion to reuse the sound
            world.createExplosion(projectileEntity, position.getX(), position.getY(), position.getZ(), 0.0f, false, World.ExplosionSourceType.TNT);
        }
        stack.decrement(1);
        return stack;
    }

    @Override
    protected void playSound(BlockPointer pointer) {
        pointer.getWorld().playSound(null, pointer.getPos().getX(), pointer.getPos().getY(), pointer.getPos().getZ(), ModSounds.CANNONBALL_SHOT, SoundCategory.BLOCKS, 1F, 1F);
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
        return 2.1f;
    } // original 1.1f
}
