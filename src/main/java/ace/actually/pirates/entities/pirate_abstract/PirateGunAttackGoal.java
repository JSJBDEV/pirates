package ace.actually.pirates.entities.pirate_abstract;
import ace.actually.pirates.entities.friendly_pirate.FriendlyPirateEntity;
import ewewukek.musketmod.GunItem;
import ewewukek.musketmod.RangedGunAttackGoal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.util.Objects;

public class PirateGunAttackGoal<T extends HostileEntity> extends RangedGunAttackGoal<T> {
    private final double speed;
    private final float squaredRange;
    private int targetSeeingTicker = 0;
    private boolean movingToLeft = false;
    private boolean backward = false;
    private int combatTicks = -1;
    private int attackDelay = 0;
    protected final AbstractPirateEntity pirate;
    public PirateGunAttackGoal(T mob, double speed, int attackInterval, float range) {
        super(mob);
        this.speed = speed;
        this.squaredRange = range * range;
        this.pirate = (AbstractPirateEntity) mob;
    }

    @Override
    public void tick() {
        super.tick();

        LivingEntity target = this.mob.getTarget();
        if (target != null) {
            double distance = this.mob.squaredDistanceTo(target);
            boolean canSee = this.mob.getWorld().raycast(new RaycastContext(
                    this.mob.getEyePos(),
                    target.getEyePos(),
                    RaycastContext.ShapeType.COLLIDER,
                    RaycastContext.FluidHandling.NONE,
                    this.mob
            )).getType() == HitResult.Type.MISS;


            if (canSee != (targetSeeingTicker > 0)) {
                targetSeeingTicker = 0;
            }
            if (canSee) targetSeeingTicker++;
            else targetSeeingTicker--;

            // 🏹 Bonus: Prevent chasing while reloading from elevated position
            if (this.mob.getY() - target.getY() > 3.0 && !this.isReady()) {
                this.mob.getNavigation().stop();
                combatTicks = -1;
                return;
            }

            if (distance <= this.squaredRange && targetSeeingTicker >= 20) {
                this.mob.getNavigation().stop();
                combatTicks++;
                if (Math.abs(this.mob.getY() - target.getY()) > 5) {
                    // Vertical target too far — stop navigation to avoid spinning
                    this.mob.getNavigation().stop();
                    this.mob.getLookControl().lookAt(target.getX(), target.getEyeY(), target.getZ(), 30.0F, 30.0F);
                    return;
                }

            } else if (
                    this.isReady() &&
                            VSGameUtilsKt.getShipManaging(this.mob) != null &&
                            VSGameUtilsKt.getShipManaging(target) != null &&
                            Objects.equals(VSGameUtilsKt.getShipManaging(this.mob), VSGameUtilsKt.getShipManaging(target))
            ) {
                BlockPos targetBlockPos = BlockPos.ofFloored(target.getX(), target.getY(), target.getZ());

                if (
                        VSGameUtilsKt.getShipManaging(this.mob) ==
                                VSGameUtilsKt.getShipObjectManagingPos((ServerWorld) this.mob.getWorld(), targetBlockPos)
                ) {
                    this.mob.getNavigation().startMovingTo(target, this.speed);
                    combatTicks = -1;
                } else {
                    this.mob.getNavigation().stop(); // Prevent walking off ship
                }
            } else {
                this.mob.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION));
            }

            if (combatTicks >= 20) {
                if (this.mob.getRandom().nextFloat() < 0.3) movingToLeft = !movingToLeft;
                if (this.mob.getRandom().nextFloat() < 0.3) backward = !backward;
                combatTicks = 0;
            }

            if (combatTicks > -1) {
                if (distance > squaredRange * 0.75f) backward = false;
                else if (distance < squaredRange * 0.25f) backward = true;
                this.mob.getMoveControl().strafeTo(backward ? -0.5f : 0.5f, movingToLeft ? 0.5f : -0.5f);

                Vec3d targetEyePos = target.getEyePos();
                this.mob.getLookControl().lookAt(targetEyePos.x, targetEyePos.y, targetEyePos.z, 30.0F, 30.0F);

                Entity vehicle = this.mob.getControllingVehicle();
                if (vehicle instanceof MobEntity mobVehicle) {
                    mobVehicle.getLookControl().lookAt(targetEyePos.x, targetEyePos.y, targetEyePos.z, 30.0F, 30.0F);
                }
            } else {
                Vec3d targetEyePos = target.getEyePos();
                this.mob.getLookControl().lookAt(targetEyePos.x, targetEyePos.y, targetEyePos.z, 30.0F, 30.0F);
            }

            // 🔫 Firing logic (unchanged)
            if (this.isReady()) {
                pirate.setCharging(false);
                this.mob.setAttacking(true);
                if (attackDelay > 0) {
                    attackDelay--;
                } else if (canSee) {
                    this.fire(2.0F);
                    attackDelay = 20 + this.mob.getRandom().nextInt(10);
                }
            } else {
                if (!this.mob.isUsingItem()) {
                    this.reload();
                    pirate.setCharging(true);
                    this.mob.setAttacking(false);
                }
            }
        }
    }

    @Override
    public void onReady() {
        this.attackDelay = 20;
        pirate.setCharging(false); // ✅ Stop showing reload pose
    }
    @Override
    public boolean canStart() {
        return GunItem.isHoldingGun(this.mob); // Ignore target
    }
    @Override
    public void stop() {
        this.mob.setAttacking(false);

        if (this.mob.isUsingItem()) {
            this.mob.clearActiveItem();
        }

        // ☑️ If gun is not ready, reload after combat ends
        if (!this.isReady() && !this.mob.isUsingItem()) {
            this.reload();
            pirate.setCharging(true); // show reload animation
        } else {
            pirate.setCharging(false);
        }
    }
}

