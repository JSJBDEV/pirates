package ace.actually.pirates.entities.pirate_skeleton;

import ace.actually.pirates.Pirates;
import ace.actually.pirates.entities.pirate_abstract.AbstractPirateEntity;
import ace.actually.pirates.entities.pirate_abstract.PirateBowAttackGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class SkeletonPirateEntity extends AbstractPirateEntity implements RangedAttackMob {
    protected BlockPos blockToDisable;

    public SkeletonPirateEntity(Level world) {
        this(world, new BlockPos(0,0,0));
    }

    public SkeletonPirateEntity(Level world, BlockPos blockToDisable) {
        super(Pirates.SKELETON_PIRATE_ENTITY_TYPE.get(), world, blockToDisable);
        if (world.getBlockState(blockToDisable).is(Pirates.MOTION_INVOKING_BLOCK.get()) && !world.isClientSide()) {
            ServerLevel serverWorld = (ServerLevel) world;
            serverWorld.setWeatherParameters(0, 36000, true, true);
            System.out.println("Skeleton ship!! Setting thunder");
        }
    }


    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(3, new PirateBowAttackGoal<>(this, 1.0D, 20, 20.0F));
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance localDifficulty) {
        super.populateDefaultEquipmentSlots(random, localDifficulty);
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
    }

    @Override
    public void performRangedAttack(LivingEntity target, float pullProgress) {

        ItemStack itemStack = this.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, Items.BOW));
        AbstractArrow persistentProjectileEntity = this.createArrowProjectile(itemStack, pullProgress);
        double d = target.getX() - this.getX();
        double e = target.getY(0.3333333333333333) - persistentProjectileEntity.getY();
        double f = target.getZ() - this.getZ();
        double g = Math.sqrt(d * d + f * f);
        persistentProjectileEntity.shoot(d, e + g * 0.20000000298023224, f, 1.6F, (float) (14 - this.getCommandSenderWorld().getDifficulty().getId() * 4));
        this.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        persistentProjectileEntity.setPierceLevel((byte)2);
        this.getCommandSenderWorld().addFreshEntity(persistentProjectileEntity);

    }

    protected AbstractArrow createArrowProjectile(ItemStack arrow, float damageModifier) {
        return ProjectileUtil.getMobArrow(this, arrow, damageModifier);
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster
                .createMonsterAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.MAX_HEALTH, 24.0D)
                .add(Attributes.FOLLOW_RANGE, 100.0D);
    }


    @Override
    public void remove(RemovalReason reason) {

        Level world = this.getCommandSenderWorld();
        if (!world.isClientSide()) {
            ServerLevel serverWorld = (ServerLevel) world;
            if(!anySkeletonPiratesLeft(serverWorld)) {
                serverWorld.setWeatherParameters(6000, 0, false, false);
                System.out.println("clearing the skies");
            }
        }

        super.remove(reason);
    }

    public static boolean anySkeletonPiratesLeft(ServerLevel world) {
        List<SkeletonPirateEntity> pirates = new ArrayList<>();

        EntityTypeTest<Entity, SkeletonPirateEntity> filter = EntityTypeTest.forClass(SkeletonPirateEntity.class);

        Predicate<SkeletonPirateEntity> predicate = entity -> true;

        world.getEntities(filter, predicate, pirates, 2);

        return pirates.size() > 1;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.WITHER_SKELETON_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.WITHER_SKELETON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WITHER_SKELETON_DEATH;
    }

}
