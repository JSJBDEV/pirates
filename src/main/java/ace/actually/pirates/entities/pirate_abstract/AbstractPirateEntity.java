package ace.actually.pirates.entities.pirate_abstract;

import ace.actually.pirates.Pirates;
import ace.actually.pirates.blocks.CannonPrimingBlock;
import ace.actually.pirates.blocks.MotionInvokingBlock;
import ace.actually.pirates.events.IPirateDies;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
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
    protected void initGoals() {
        super.initGoals();
        this.goalSelector.add(5, new PirateWanderArroundFarGoal(this, 1.0D));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 200.0F));
        this.goalSelector.add(6, new LookAroundGoal(this));
        //this.targetSelector.add(1, new RevengeGoal(this, new Class[0]));
        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
        this.targetSelector.add(3, new ActiveTargetGoal(this, MerchantEntity.class, false));
        this.targetSelector.add(3, new ActiveTargetGoal(this, IronGolemEntity.class, true));
    }

    @Override
    public void remove(RemovalReason reason) {
        disableSavedBlock();
        super.remove(reason);
    }

    private void disableSavedBlock() {
        if (!Objects.equals(blockToDisable, new BlockPos(0, 0, 0))) {
            IPirateDies.EVENT.invoker().interact(attackingPlayer,this);
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
