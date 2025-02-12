package ace.actually.pirates.entities.shot;

import ace.actually.pirates.Pirates;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FlyingItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public class ShotEntity extends ThrownItemEntity implements FlyingItemEntity {
    private LivingEntity in;
    private float damage=6;
    private String extra="";
    private int tickAge = 0;
    private World world;


    public ShotEntity(EntityType<? extends ThrownItemEntity> entityType, World world, LivingEntity caster, Item toShow, float damageTo, String special) {
        super(entityType, world);
        in=caster;
        setItem(new ItemStack(toShow));
        damage=20;
        extra=special;
    }

    public ShotEntity(World world)
    {
        super(Pirates.SHOT_ENTITY_TYPE, world);
    }

    @Override
    public void tick () {
        if (this.tickAge > 500) {
            if (!this.getWorld().isClient()) {
                explode();
            }
        } else {
            this.tickAge++;
        }

        if (!getWorld().isClient() && getVelocity().length() > 0.85) {
            ((ServerWorld)getWorld()).spawnParticles(ParticleTypes.CLOUD, getX(), getY(), getZ(), 1, 0, 0, 0, 0);
        }
        super.tick();
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        if (!this.getWorld().isClient) {
            explode();
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        Entity entity = entityHitResult.getEntity();
        entity.damage(this.getDamageSources().explosion(null), damage);
        if (!this.getWorld().isClient) {
            explode();
        }
    }
    private BlockPos getCollisionBlock() {
        HitResult hitResult = this.getWorld().raycast(new RaycastContext(
                this.getPos(),
                this.getPos().add(this.getVelocity().normalize().multiply(2)), // 🔹 Extend raycast slightly forward
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                this
        ));

        if (hitResult instanceof BlockHitResult blockHitResult) {
            return blockHitResult.getBlockPos(); // ✅ Return exact impacted block
        }
        return null; // No impact detected
    }
    private void explode() {
        // vanilla explosion, just tryna reuse the sound and particle of it
        this.getWorld().createExplosion(this, this.getX(), this.getY(), this.getZ(), 0.0f, extra.contains("fire"), World.ExplosionSourceType.TNT);

        // custom explosion, ignore baseShotPower and unbreakable blocks. On impact destroy any block but only 1 block
        World world = this.getWorld();
        BlockPos impactPos = this.getCollisionBlock(); // ✅ Get exact impact block
        if (impactPos == null) {
            impactPos = this.getBlockPos(); // Fallback in case collision is null
        }
        // 🔹 Get the block at impact position
        BlockState blockState = world.getBlockState(impactPos);
        // 🔹 Ensure the block is breakable (ignore air & unbreakable blocks)
        if (!blockState.isAir() && blockState.getHardness(world, impactPos) >= 0) {
            world.breakBlock(impactPos, true); // 🔹 Destroy the block at impact position
        }

        // 🔹 Explosion splash damage & knockback effect
        double explosionRadius = 1.0; // Increased radius for knockback
        double knockbackStrength = 3.0; // 🔹 Adjust force (higher = stronger push)

        world.getOtherEntities(this, this.getBoundingBox().expand(explosionRadius)).forEach(entity -> {
            // Apply damage
            entity.damage(this.getDamageSources().explosion(null), damage);
            // Apply knockback force
            Vec3d pushDirection = entity.getPos().subtract(this.getPos()).normalize(); // Direction from explosion
            double forceMultiplier = 1.0 - (entity.getPos().distanceTo(this.getPos()) / explosionRadius); // Weaker at edges
            Vec3d knockback = pushDirection.multiply(knockbackStrength * forceMultiplier);

            entity.addVelocity(knockback.x, knockback.y + 0.2, knockback.z); // 🔹 Add upwards push
            entity.velocityModified = true; // Ensure physics update
        });
        this.discard();
    }

    @Override
    protected Item getDefaultItem() {
        return Pirates.CANNONBALL_ENT;
    }

    @Override
    public ItemStack getStack() {
        return super.getStack();
    }

    public Packet<ClientPlayPacketListener> createSpawnPacket() {
        Entity entity = in;
        return new EntitySpawnS2CPacket(this, entity == null ? 0 : entity.getId());
    }
}
