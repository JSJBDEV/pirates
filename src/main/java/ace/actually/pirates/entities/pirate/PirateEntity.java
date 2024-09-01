package ace.actually.pirates.entities.pirate;

import ace.actually.pirates.Pirates;
import ace.actually.pirates.blocks.CannonPrimingBlockEntity;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.*;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

public class PirateEntity extends HostileEntity implements RangedAttackMob {
    public PirateEntity(World world) {
        super(Pirates.PIRATE_ENTITY_TYPE, world);
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

    @Override
    protected void initEquipment(Random random, LocalDifficulty localDifficulty) {
        super.initEquipment(random, localDifficulty);
        this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
    }

    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, EntityData entityData, NbtCompound entityTag) {
        entityData = super.initialize(world, difficulty, spawnReason, entityData, entityTag);
        initEquipment(random,difficulty);

        return entityData;
    }

    @Override
    public void attack(LivingEntity target, float pullProgress) {

        ItemStack itemStack = this.getStackInHand(ProjectileUtil.getHandPossiblyHolding(this, Items.BOW));
        PersistentProjectileEntity persistentProjectileEntity = this.createArrowProjectile(itemStack, pullProgress);
        double d = target.getX() - this.getX();
        double e = target.getBodyY(0.3333333333333333) - persistentProjectileEntity.getY();
        double f = target.getZ() - this.getZ();
        double g = Math.sqrt(d * d + f * f);
        persistentProjectileEntity.setVelocity(d, e + g * 0.20000000298023224, f, 1.6F, (float)(14 - this.getEntityWorld().getDifficulty().getId() * 4));
        this.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        this.getEntityWorld().spawnEntity(persistentProjectileEntity);

//        if(random.nextInt(10)==0)
//        {
//            //we are basically saying "if the target is on a solid ship" like the VS ones, or a create contraption i guess
//            if(!target.isSubmergedInWater())
//            {
//                this.teleport(target.getX(),target.getY()+0.1,target.getZ());
//            }
//        }
//        if(isSubmergedInWater() && !target.isSubmergedInWater())
//        {
//            this.teleport(target.getX(),target.getY()+0.1,target.getZ());
//        }

    }
    protected PersistentProjectileEntity createArrowProjectile(ItemStack arrow, float damageModifier) {
        return ProjectileUtil.createArrowProjectile(this, arrow, damageModifier);
    }

    public static DefaultAttributeContainer.Builder attributes() {
        return HostileEntity
                .createHostileAttributes()
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3D)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE,100.0D);
    }

    @Override
    public boolean isPersistent() {
        return true;
    }

    @Override
    public boolean canSpawn(WorldAccess world, SpawnReason spawnReason) {
        if(spawnReason==SpawnReason.SPAWNER)
        {
            return true;
        }
        return super.canSpawn(world, spawnReason);
    }

    @Override
    public void remove(RemovalReason reason) {
        World world = this.getWorld();
        Vec3d pos = this.getPos();

        if (!world.isClient()) {
            RaycastContext context = new RaycastContext(pos, pos.add(0, -5, 0), RaycastContext.ShapeType.COLLIDER,
                    RaycastContext.FluidHandling.NONE, this);
            BlockHitResult result = world.raycast(context);
            if (VSGameUtilsKt.isBlockInShipyard(world, result.getBlockPos())) {
                CannonPrimingBlockEntity.disarmNearest(VSGameUtilsKt.getShipManagingPos(world, result.getBlockPos()).getId(), pos, world);
            }
        }
        super.remove(reason);
    }
}
