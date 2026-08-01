package ace.actually.pirates.entities.friendly_pirate;

import ace.actually.pirates.Pirates;
import ace.actually.pirates.entities.pirate_abstract.AbstractPirateEntity;
import ace.actually.pirates.entities.pirate_abstract.PirateBowAttackGoal;
import ace.actually.pirates.entities.pirate_abstract.PirateGunAttackGoal;
import ace.actually.pirates.entities.pirate_abstract.PirateWanderArroundFarGoal;
import ace.actually.pirates.entities.pirate_default.PirateEntity;
import ace.actually.pirates.util.DisarmUtils;
import ace.actually.pirates.compat.MusketModCompat;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.PillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.util.List;

public class FriendlyPirateEntity extends AbstractPirateEntity implements RangedAttackMob {

    private static final String[] FIRST = {"Johan","John","Bob","Cali","Dorris","Lopez","Wilhelm","Armstrong","David","Giorno"};
    private static final String[] LAST = {"Diver","Smith","Forest","Maze","Fisherman","Callous","Calculated","Fierce","Flatulent","Agreeable","Rational"};
    private static final TrackedData<String> JOB = DataTracker.registerData(FriendlyPirateEntity.class, TrackedDataHandlerRegistry.STRING);

    public FriendlyPirateEntity(World world)
    {
        super(Pirates.FRIENDLY_PIRATE_TYPE, world, BlockPos.ORIGIN);
    }

    public String getPirateJob() {
        return dataTracker.get(JOB);
    }

    public void setPirateJob(String pirateJob) {
        dataTracker.set(JOB,pirateJob);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putString("pirateJob",dataTracker.get(JOB));
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        dataTracker.set(JOB,nbt.getString("pirateJob"));
    }

    public FriendlyPirateEntity(World world, BlockPos blockToDisable) {
        super(Pirates.FRIENDLY_PIRATE_TYPE, world, blockToDisable);
        initEquipment(world.random,world.getLocalDifficulty(getBlockPos()));
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        dataTracker.startTracking(JOB,"none");

    }

    public void genCustomName(World world)
    {
        setCustomName(Text.of(FIRST[world.random.nextInt(FIRST.length)]+" the "+LAST[world.random.nextInt(LAST.length)]));
    }

    @Override
    protected void initGoals() {
        super.initGoals();
//        this.goalSelector.add(3, new PirateBowAttackGoal<>(this, 1.0D, 20, 20.0F));
        this.goalSelector.add(3, MusketModCompat.createRangedGoal(this));
        this.targetSelector.add(3, new ActiveTargetGoal(this, PirateEntity.class, true));
        this.targetSelector.add(3, new ActiveTargetGoal(this, PillagerEntity.class, true));
//        this.targetSelector.add(1, new RevengeGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        if(getEntityWorld().getTimeOfDay()%1000L==0L)
        {
            if(getPirateJob().equals("doctor"))
            {
                List<FriendlyPirateEntity> crew =  getEntityWorld().getEntitiesByClass(FriendlyPirateEntity.class,new Box(getBlockPos().add(-10,-10,-10),getBlockPos().add(10,10,10)),LivingEntity::isAlive);
                crew.forEach(a->a.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION,500,1)));
            }
        }
    }

    public static DefaultAttributeContainer.Builder attributes() {
        return HostileEntity
                .createHostileAttributes()
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3D)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 100.0D);
    }


    @Override
    protected void initEquipment(Random random, LocalDifficulty localDifficulty) {
        super.initEquipment(random, localDifficulty);
        MusketModCompat.equipRandomGunOrBow(this, random);
    }

    @Override
    public void attack(LivingEntity target, float pullProgress) {

//        ItemStack itemStack = this.getStackInHand(ProjectileUtil.getHandPossiblyHolding(this, Items.BLUNDERBUSS));
//        PersistentProjectileEntity persistentProjectileEntity = this.createArrowProjectile(itemStack, pullProgress);
//        double d = target.getX() - this.getX();
//        double e = target.getBodyY(0.3333333333333333) - persistentProjectileEntity.getY();
//        double f = target.getZ() - this.getZ();
//        double g = Math.sqrt(d * d + f * f);
//        persistentProjectileEntity.setVelocity(d, e + g * 0.20000000298023224, f, 1.6F, (float) (14 - this.getEntityWorld().getDifficulty().getId() * 4));
//        this.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
//        this.getEntityWorld().spawnEntity(persistentProjectileEntity);
    }

    protected PersistentProjectileEntity createArrowProjectile(ItemStack arrow, float damageModifier) {
        return ProjectileUtil.createArrowProjectile(this, arrow, damageModifier);
    }

    @Override
    public ActionResult interactAt(PlayerEntity player, Vec3d hitPos, Hand hand) {
        ItemStack stack = Pirates.recruitCost.get();
        if(!hasCustomName() && player.getStackInHand(hand).isOf(stack.getItem()))
        {
            if(player.getStackInHand(hand).getCount()>=stack.getCount())
            {
                player.giveItemStack(new ItemStack(Pirates.CANNONEER_ITEM));
                player.getStackInHand(hand).decrement(stack.getCount());
                this.teleport(0,0,0);
                this.kill();
            }

        }
        return super.interactAt(player, hitPos, hand);
    }
}
