package ace.actually.pirates.entities.shot;

import ace.actually.pirates.Pirates;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class ShotEntity extends ThrowableItemProjectile implements ItemSupplier {
    private LivingEntity in;
    private float damage=6;
    private String extra="";
    private int tickAge = 0;


    public ShotEntity(EntityType<? extends ThrowableItemProjectile> entityType, Level world, LivingEntity caster, Item toShow, float damageTo, String special) {
        super(entityType, world);
        in=caster;
        setItem(new ItemStack(toShow));
        damage=damageTo;
        extra=special;
    }

    public ShotEntity(Level world)
    {
        super(Pirates.SHOT_ENTITY_TYPE, world);
    }

    @Override
    public void tick () {
        if (this.tickAge > 500) {
            if (!this.level().isClientSide()) {
                explode();
            }
        } else {
            this.tickAge++;
        }

        if (!level().isClientSide() && getDeltaMovement().length() > 0.85) {
            ((ServerLevel)level()).sendParticles(ParticleTypes.CLOUD, getX(), getY(), getZ(), 1, 0, 0, 0, 0);
        }
        super.tick();
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (!this.level().isClientSide) {
            explode();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        Entity entity = entityHitResult.getEntity();
        entity.hurt(this.damageSources().explosion(null), damage);
        if (!this.level().isClientSide) {
            explode();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        super.onHitBlock(blockHitResult);
        if(extra.contains("heavy"))
        {
            level().setBlockAndUpdate(blockHitResult.getBlockPos(), Pirates.HEAVY_BLOCK.defaultBlockState());
        }

    }

    private void explode() {
        this.level().explode(this, this.getX(), this.getY(), this.getZ(), Pirates.baseShotPower, extra.contains("fire"), Level.ExplosionInteraction.TNT);
        this.discard();
    }

    @Override
    protected Item getDefaultItem() {
        return Pirates.CANNONBALL_ENT;
    }

    @Override
    public ItemStack getItem() {
        return super.getItem();
    }

    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        Entity entity = in;
        return new ClientboundAddEntityPacket(this, entity == null ? 0 : entity.getId());
    }
}
