package ace.actually.pirates.entities.legacy;

import ace.actually.pirates.Pirates;
import ace.actually.pirates.entities.ShotEntity;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.Hand;
import net.minecraft.world.*;

import java.util.UUID;

public class PirateShipEntity extends HostileEntity implements RangedAttackMob {

    public PirateShipEntity(World world) {
        super(Pirates.SHIP,world);
        this.setPathfindingPenalty(PathNodeType.WATER, -1.0F);
    }




    @Override
    protected void initGoals() {
        super.initGoals();
        this.goalSelector.add(5, new WanderAroundFarGoal(this, 1.0D));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 200.0F));
        this.goalSelector.add(6, new LookAroundGoal(this));
        this.goalSelector.add(4, new BowAttackGoal(this,1.0D, 20, 200.0F));
        this.targetSelector.add(1, new RevengeGoal(this, new Class[0]));
        this.targetSelector.add(3, new ActiveTargetGoal(this, PlayerEntity.class, true));
    }

    protected void initEquipment(LocalDifficulty difficulty) {
        super.initEquipment(difficulty);
        this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
        PillagerEntity entity = new PillagerEntity(EntityType.PILLAGER,world);
        entity.setUuid(UUID.randomUUID());
        entity.setPosition(getPos());
        entity.setStackInHand(Hand.MAIN_HAND,new ItemStack(Items.CROSSBOW));
        world.spawnEntity(entity);
        entity.startRiding(this);

    }



    @Override
    public double getMountedHeightOffset() {
        return 0.1;
    }

    @Override
    public boolean canBeRiddenInWater() {
        return true;
    }


    @Override
    public boolean canBreatheInWater() {
        return true;
    }

    @Override
    public boolean canWalkOnFluid(FluidState fluidState) {
        return fluidState.isOf(Fluids.WATER);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.world.getFluidState(this.getBlockPos().up()).isIn(FluidTags.WATER)) {
            this.onGround = true;
        } else {
            this.setVelocity(this.getVelocity().multiply(0.5D).add(0.0D, 1D, 0.0D));
        }
    }

    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, EntityData entityData, NbtCompound entityTag) {
        entityData = super.initialize(world, difficulty, spawnReason, entityData, entityTag);
        initEquipment(difficulty);

        return entityData;
    }

    @Override
    public void attack(LivingEntity target, float pullProgress) {

        if(random.nextInt(3)==0)
        {
            ShotEntity shotEntity = new ShotEntity(Pirates.SHOT_ENTITY_TYPE,world,this,Items.ANVIL,1,"");
            shotEntity.setPosition(getX(),getY()+1,getZ());

            double d = target.getX() - this.getX();
            double e = target.getBodyY(0.3333333333333333D) - shotEntity.getY();
            double f = target.getZ() - this.getZ();
            double g = Math.sqrt(d * d + f * f);
            shotEntity.setVelocity(d, e + g * 0.20000000298023224D, f, 1.6F, (float)(14 - this.world.getDifficulty().getId() * 4));
            this.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
            this.world.spawnEntity(shotEntity);
        }
        if(random.nextInt(10)==0)
        {
            //we are basically saying "if the target is on a solid ship" like the VS ones, or a create contraption i guess
            if(!target.isSubmergedInWater())
            {
                VindicatorEntity entity = new VindicatorEntity(EntityType.VINDICATOR,world);
                entity.setPosition(target.getX()+random.nextInt(-5,5),target.getY()+0.1,target.getZ()+random.nextInt(-5,5));
                entity.setUuid(UUID.randomUUID());
                world.spawnEntity(entity);
            }
        }

    }

    public static DefaultAttributeContainer.Builder attributes() {
        return HostileEntity
                .createHostileAttributes()
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3D)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE,100.0D);
    }

    public EntityGroup getGroup() {
        return EntityGroup.AQUATIC;
    }

    @Override
    public boolean canSpawn(WorldView world) {
        return world.doesNotIntersectEntities(this);
    }

    @Override
    public boolean canSpawn(WorldAccess world, SpawnReason spawnReason) {
        return world.doesNotIntersectEntities(this);
    }

}
