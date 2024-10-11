package ace.actually.pirates.entities.pirate_default;

import ace.actually.pirates.Pirates;
import ace.actually.pirates.entities.pirate_abstract.AbstractPirateEntity;
import ace.actually.pirates.entities.pirate_abstract.PirateBowAttackGoal;
import ace.actually.pirates.entities.pirate_abstract.PirateWanderArroundFarGoal;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.*;

public class PirateEntity extends AbstractPirateEntity implements RangedAttackMob {
    protected BlockPos blockToDisable;

    public PirateEntity(World world) {
        this(world, new BlockPos(0,0,0));
    }

    public PirateEntity(World world, BlockPos blockToDisable) {
        super(Pirates.PIRATE_ENTITY_TYPE, world, blockToDisable);
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
        this.getEntityWorld().spawnEntity(persistentProjectileEntity);

    }

    protected PersistentProjectileEntity createArrowProjectile(ItemStack arrow, float damageModifier) {
        return ProjectileUtil.createArrowProjectile(this, arrow, damageModifier);
    }

    public static DefaultAttributeContainer.Builder attributes() {
        return HostileEntity
                .createHostileAttributes()
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3D)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 100.0D);
    }



}
