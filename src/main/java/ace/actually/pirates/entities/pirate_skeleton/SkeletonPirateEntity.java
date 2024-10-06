package ace.actually.pirates.entities.pirate_skeleton;

import ace.actually.pirates.Pirates;
import ace.actually.pirates.entities.pirate_abstract.AbstractPirateEntity;
import ace.actually.pirates.entities.pirate_abstract.PirateBowAttackGoal;
import ace.actually.pirates.entities.pirate_abstract.PirateWanderArroundFarGoal;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class SkeletonPirateEntity extends AbstractPirateEntity implements RangedAttackMob {
    protected BlockPos blockToDisable;

    public SkeletonPirateEntity(World world) {
        this(world, new BlockPos(0,0,0));
    }

    public SkeletonPirateEntity(World world, BlockPos blockToDisable) {
        super(Pirates.SKELETON_PIRATE_ENTITY_TYPE, world, blockToDisable);
        if (world.getBlockState(blockToDisable).isOf(Pirates.MOTION_INVOKING_BLOCK) && !world.isClient()) {
            ServerWorld serverWorld = (ServerWorld) world;
            serverWorld.setWeather(0, 6000, true, true);
            System.out.println("Skeleton ship!! Setting thunder");
        }
    }


    @Override
    protected void initGoals() {
        super.initGoals();
        this.goalSelector.add(5, new PirateWanderArroundFarGoal(this, 1.0D));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 200.0F));
        this.goalSelector.add(6, new LookAroundGoal(this));
        this.goalSelector.add(3, new PirateBowAttackGoal<>(this, 1.0D, 20, 20.0F));
        //this.targetSelector.add(1, new RevengeGoal(this, new Class[0]));
        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
        this.targetSelector.add(3, new ActiveTargetGoal(this, MerchantEntity.class, false));
        this.targetSelector.add(3, new ActiveTargetGoal(this, IronGolemEntity.class, true));
    }

    @Override
    protected void initEquipment(Random random, LocalDifficulty localDifficulty) {
        super.initEquipment(random, localDifficulty);
        this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
    }

    @Override
    public void attack(LivingEntity target, float pullProgress) {

        ItemStack itemStack = this.getStackInHand(ProjectileUtil.getHandPossiblyHolding(this, Items.BOW));
        PersistentProjectileEntity persistentProjectileEntity = this.createArrowProjectile(itemStack, pullProgress);
        double d = target.getX() - this.getX();
        double e = target.getBodyY(0.3333333333333333) - persistentProjectileEntity.getY();
        double f = target.getZ() - this.getZ();
        double g = Math.sqrt(d * d + f * f);
        persistentProjectileEntity.setVelocity(d, e + g * 0.20000000298023224, f, 1.6F, (float) (14 - this.getEntityWorld().getDifficulty().getId() * 4));
        this.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        persistentProjectileEntity.setPierceLevel((byte)2);
        this.getEntityWorld().spawnEntity(persistentProjectileEntity);

    }

    protected PersistentProjectileEntity createArrowProjectile(ItemStack arrow, float damageModifier) {
        return ProjectileUtil.createArrowProjectile(this, arrow, damageModifier);
    }

    public static DefaultAttributeContainer.Builder attributes() {
        return HostileEntity
                .createHostileAttributes()
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3D)
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 24.0D)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 100.0D);
    }


    @Override
    public void remove(RemovalReason reason) {

        World world = this.getEntityWorld();
        if (!world.isClient()) {
            ServerWorld serverWorld = (ServerWorld) world;
            if(!anySkeletonPiratesLeft(serverWorld)) {
                serverWorld.setWeather(6000, 0, false, false);
                System.out.println("clearing the skies");
            }
        }

        super.remove(reason);
    }

    public static boolean anySkeletonPiratesLeft(ServerWorld world) {
        List<SkeletonPirateEntity> pirates = new ArrayList<>();

        TypeFilter<Entity, SkeletonPirateEntity> filter = TypeFilter.instanceOf(SkeletonPirateEntity.class);

        Predicate<SkeletonPirateEntity> predicate = entity -> true;

        world.collectEntitiesByType(filter, predicate, pirates, 2);

        return pirates.size() > 1;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_WITHER_SKELETON_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_WITHER_SKELETON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_WITHER_SKELETON_DEATH;
    }

}
