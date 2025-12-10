package ace.actually.pirates.entities.pirate_abstract;

import ace.actually.pirates.events.IPirateDies;
import ace.actually.pirates.util.DisarmUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

public abstract class AbstractPirateEntity  extends Monster {

    protected BlockPos blockToDisable;

    protected AbstractPirateEntity(EntityType<? extends Monster> entityType, Level world, BlockPos blockToDisable) {
        super(entityType, world);

        this.blockToDisable = blockToDisable;
    }

    @Override
    public boolean isPersistenceRequired() {
        return true;
    }


    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty, MobSpawnType spawnReason, SpawnGroupData entityData, CompoundTag entityTag) {
        entityData = super.finalizeSpawn(world, difficulty, spawnReason, entityData, entityTag);
        populateDefaultEquipmentSlots(random, difficulty);
        DisarmUtils.rearm(level(),blockToDisable);
        setPersistenceRequired();
        return entityData;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(5, new PirateWanderArroundFarGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 200.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

    }

    @Override
    public void remove(RemovalReason reason) {
        DisarmUtils.disarm(level(),blockToDisable);
        IPirateDies.EVENT.invoker().interact(lastHurtByPlayer,this);
        super.remove(reason);
    }



    public boolean isOnShip() {
        return VSGameUtilsKt.getShipManaging(this) != null;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putInt("BlockToDisableX", this.blockToDisable.getX());
        nbt.putInt("BlockToDisableY", this.blockToDisable.getY());
        nbt.putInt("BlockToDisableZ", this.blockToDisable.getZ());
    }


    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        int x, y, z;
        if (nbt.contains("BlockToDisableX") && nbt.contains("BlockToDisableY") && nbt.contains("BlockToDisableZ")) {
            x = nbt.getInt("BlockToDisableX");
            y = nbt.getInt("BlockToDisableY");
            z = nbt.getInt("BlockToDisableZ");

            this.blockToDisable = new BlockPos(x, y, z);
        }
    }
}
