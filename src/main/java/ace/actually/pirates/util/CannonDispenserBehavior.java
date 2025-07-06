package ace.actually.pirates.util;
import ace.actually.pirates.Pirates;
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
        projectileEntity.setVelocity(direction.getOffsetX(), (float)direction.getOffsetY() + 0.15f, direction.getOffsetZ(), this.getForce(), this.getVariation() / 2);
        world.spawnEntity(projectileEntity);

        Ship ship = VSGameUtilsKt.getShipManagingPos(world, pointer.getPos());
        if (ship != null) {
            projectileEntity.addVelocity(VectorConversionsMCKt.toMinecraft(ship.getVelocity()).multiply(1/60.0));
        }

        if (!world.isClient()) {
            net.minecraft.network.PacketByteBuf buf = net.fabricmc.fabric.api.networking.v1.PacketByteBufs.create();
            buf.writeDouble(position.getX());
            buf.writeDouble(position.getY());
            buf.writeDouble(position.getZ());
            buf.writeInt(direction.getId());

            for (net.minecraft.server.network.ServerPlayerEntity player :
                    net.fabricmc.fabric.api.networking.v1.PlayerLookup.tracking(world, pointer.getPos())) {
                net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player, Pirates.CANNON_SMOKE_PACKET_ID, buf);
            }
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
        return Pirates.cannonRange;
    }
}
