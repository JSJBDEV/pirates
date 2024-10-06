package ace.actually.pirates.entities.abstract_pirate;

import ace.actually.pirates.Pirates;
import ace.actually.pirates.blocks.CannonPrimingBlock;
import ace.actually.pirates.blocks.MotionInvokingBlock;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.util.Objects;

public abstract class AbstractPirateEntity  extends HostileEntity {

    protected BlockPos blockToDisable;

    protected AbstractPirateEntity(EntityType<? extends HostileEntity> entityType, World world, BlockPos blockToDisable) {
        super(entityType, world);

        this.blockToDisable = blockToDisable;
    }

    @Override
    public boolean isPersistent() {
        return true;
    }


    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, EntityData entityData, NbtCompound entityTag) {
        entityData = super.initialize(world, difficulty, spawnReason, entityData, entityTag);
        initEquipment(random, difficulty);

        return entityData;
    }

    @Override
    public void remove(RemovalReason reason) {
        disableSavedBlock();
        super.remove(reason);
    }

    private void disableSavedBlock() {
        if (!Objects.equals(blockToDisable, new BlockPos(0, 0, 0))) {
            if (this.getWorld().getBlockState(blockToDisable).isOf(Pirates.CANNON_PRIMING_BLOCK)) {
                CannonPrimingBlock.disarm(this.getWorld(), blockToDisable);
            } else if (this.getWorld().getBlockState(blockToDisable).isOf(Pirates.MOTION_INVOKING_BLOCK)) {
                MotionInvokingBlock.disarm(this.getWorld(), blockToDisable);
            }
        }
    }

    public boolean isOnShip() {
        return VSGameUtilsKt.getShipManaging(this) != null;
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("BlockToDisableX", this.blockToDisable.getX());
        nbt.putInt("BlockToDisableY", this.blockToDisable.getY());
        nbt.putInt("BlockToDisableZ", this.blockToDisable.getZ());
    }


    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        int x, y, z;
        if (nbt.contains("BlockToDisableX") && nbt.contains("BlockToDisableY") && nbt.contains("BlockToDisableZ")) {
            x = nbt.getInt("BlockToDisableX");
            y = nbt.getInt("BlockToDisableY");
            z = nbt.getInt("BlockToDisableZ");

            this.blockToDisable = new BlockPos(x, y, z);
        }
    }

}
