package ace.actually.pirates.entities;

import ace.actually.pirates.Pirates;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FlyingItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

public class ShotEntity extends ThrownItemEntity implements FlyingItemEntity {
    private LivingEntity in;
    private float damage=1;
    private String extra="";


    public ShotEntity(EntityType<? extends ThrownItemEntity> entityType, World world, LivingEntity caster, Item toShow, float damageTo, String special) {
        super(entityType, world);
        in=caster;
        setItem(new ItemStack(toShow));
        damage=damageTo;
        extra=special;
        //setNoGravity(false);

    }
    public ShotEntity(World world)
    {
        super(Pirates.SHOT_ENTITY_TYPE, world);
    }

    @Override
    public void tick () {
        if (!getWorld().isClient() && getVelocity().length() > 0.85) {
            ((ServerWorld)getWorld()).spawnParticles(ParticleTypes.CLOUD, getX(), getY(), getZ(), 1, 0, 0, 0, 0);
        }
        super.tick();
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        if (!this.getWorld().isClient) {
            this.getWorld().createExplosion(this, this.getX(), this.getY(), this.getZ(), 2.2f, false, World.ExplosionSourceType.BLOCK);
            this.discard();
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        Entity entity = entityHitResult.getEntity();
        entity.damage(this.getDamageSources().explosion(null), 6.0f);
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
