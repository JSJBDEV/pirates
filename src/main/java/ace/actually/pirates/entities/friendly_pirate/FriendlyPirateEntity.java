package ace.actually.pirates.entities.friendly_pirate;

import ace.actually.pirates.Pirates;
import ace.actually.pirates.entities.pirate_abstract.AbstractPirateEntity;
import ace.actually.pirates.entities.pirate_abstract.PirateBowAttackGoal;
import ace.actually.pirates.entities.pirate_abstract.PirateWanderArroundFarGoal;
import ace.actually.pirates.util.DisarmUtils;
import net.minecraft.core.BlockPos;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.util.List;

public class FriendlyPirateEntity extends AbstractPirateEntity implements RangedAttackMob {

    private static final String[] FIRST = {"Johan","John","Bob","Cali","Dorris","Lopez","Wilhelm","Armstrong","David","Giorno"};
    private static final String[] LAST = {"Diver","Smith","Forest","Maze","Fisherman","Callous","Calculated","Fierce","Flatulent","Agreeable","Rational"};
    private static final EntityDataAccessor<String> JOB = SynchedEntityData.defineId(FriendlyPirateEntity.class, EntityDataSerializers.STRING);

    public FriendlyPirateEntity(Level world)
    {
        super(Pirates.FRIENDLY_PIRATE_TYPE, world, BlockPos.ZERO);
    }

    public String getPirateJob() {
        return entityData.get(JOB);
    }

    public void setPirateJob(String pirateJob) {
        entityData.set(JOB,pirateJob);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putString("pirateJob",entityData.get(JOB));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        entityData.set(JOB,nbt.getString("pirateJob"));
    }

    public FriendlyPirateEntity(Level world, BlockPos blockToDisable) {
        super(Pirates.FRIENDLY_PIRATE_TYPE, world, blockToDisable);
        populateDefaultEquipmentSlots(world.random,world.getCurrentDifficultyAt(blockPosition()));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(JOB,"none");

    }

    public void genCustomName(Level world)
    {
        setCustomName(Component.nullToEmpty(FIRST[world.random.nextInt(FIRST.length)]+" the "+LAST[world.random.nextInt(LAST.length)]));
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(3, new PirateBowAttackGoal<>(this, 1.0D, 20, 20.0F));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));

    }

    @Override
    public void tick() {
        super.tick();
        if(getCommandSenderWorld().getDayTime()%1000L==0L)
        {
            if(getPirateJob().equals("doctor"))
            {
                List<FriendlyPirateEntity> crew =  getCommandSenderWorld().getEntitiesOfClass(FriendlyPirateEntity.class,new AABB(blockPosition().offset(-10,-10,-10),blockPosition().offset(10,10,10)),LivingEntity::isAlive);
                crew.forEach(a->a.addEffect(new MobEffectInstance(MobEffects.REGENERATION,500,1)));
            }
        }
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster
                .createMonsterAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.FOLLOW_RANGE, 100.0D);
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
        this.getCommandSenderWorld().addFreshEntity(persistentProjectileEntity);

    }

    protected AbstractArrow createArrowProjectile(ItemStack arrow, float damageModifier) {
        return ProjectileUtil.getMobArrow(this, arrow, damageModifier);
    }

    @Override
    public InteractionResult interactAt(Player player, Vec3 hitPos, InteractionHand hand) {
        ItemStack stack = Pirates.recruitCost.get();
        if(!hasCustomName() && player.getItemInHand(hand).is(stack.getItem()))
        {
            if(player.getItemInHand(hand).getCount()>=stack.getCount())
            {
                player.addItem(new ItemStack(Pirates.CANNONEER_ITEM));
                player.getItemInHand(hand).shrink(stack.getCount());
                this.teleportToWithTicket(0,0,0);
                this.kill();
            }

        }
        return super.interactAt(player, hitPos, hand);
    }
}
