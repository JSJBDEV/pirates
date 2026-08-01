package ace.actually.pirates.entities.pirate_abstract;

import ace.actually.pirates.Pirates;
import ace.actually.pirates.blocks.CannonPrimingBlock;
import ace.actually.pirates.blocks.MotionInvokingBlock;
import ace.actually.pirates.entities.friendly_pirate.FriendlyPirateEntity;
import ace.actually.pirates.events.IPirateDies;
import ace.actually.pirates.util.DisarmUtils;
import ace.actually.pirates.compat.MusketModCompat;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.IllagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.util.Objects;

public abstract class AbstractPirateEntity extends IllagerEntity {

    protected BlockPos blockToDisable;

    protected AbstractPirateEntity(EntityType<? extends IllagerEntity> entityType, World world, BlockPos blockToDisable) {
        super(entityType, world);

        this.blockToDisable = blockToDisable;
    }

    @Override
    public boolean isPersistent() {
        return true;
    }
    @Override
    public void addBonusForWave(int wave, boolean unused) {
        // Your pirate might not need any wave bonus, so leave it empty or log something
    }
    @Override
    public net.minecraft.sound.SoundEvent getCelebratingSound() {
        return null; // or a custom pirate celebration sound if you have one
    }

    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, EntityData entityData, NbtCompound entityTag) {
        entityData = super.initialize(world, difficulty, spawnReason, entityData, entityTag);
        initEquipment(random, difficulty);
        DisarmUtils.rearm(getWorld(),blockToDisable);
        setPersistent();
        return entityData;
    }

    @Override
    protected void initGoals() {
        super.initGoals();
        this.goalSelector.add(5, new PirateWanderArroundFarGoal(this, 1.0D));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 200.0F));
        this.goalSelector.add(6, new LookAroundGoal(this));

    }

    @Override
    public void remove(RemovalReason reason) {
        DisarmUtils.disarm(getWorld(),blockToDisable);
        IPirateDies.EVENT.invoker().interact(attackingPlayer,this);
        super.remove(reason);
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
    // for musket mod with reloading and firing
    protected static final TrackedData<Boolean> CHARGING =
            DataTracker.registerData(FriendlyPirateEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        dataTracker.startTracking(CHARGING, false);
    }
    public boolean isCharging() {
        return this.dataTracker.get(CHARGING);
    }
    public void setCharging(boolean charging) {
        this.dataTracker.set(CHARGING, charging);
    }
    @Override
    public IllagerEntity.State getState() {
        if (this.isCharging()) {
            return State.CROSSBOW_CHARGE;
        } else if (MusketModCompat.isHoldingGun(this)) {
            if (this.isAttacking())
                return State.CROSSBOW_HOLD;
            return (MusketModCompat.isHoldingPistol(this) ? State.NEUTRAL : State.CROSSBOW_CHARGE);
        }
        return State.CROSSBOW_CHARGE; // almost never reach this state
    }
}
