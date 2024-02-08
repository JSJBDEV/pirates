package ace.actually.pirates.entities;

import ace.actually.pirates.Pirates;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FlyingItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;

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
        System.out.println("world");
        //setNoGravity(false);
    }



    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        if(!world.isClient)
        {
            world.createExplosion(this,getX(),getY(),getZ(),damage, Explosion.DestructionType.DESTROY);
        }
        kill();
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);
        if(!world.isClient)
        {
            world.createExplosion(this,getX(),getY(),getZ(),damage, Explosion.DestructionType.DESTROY);
        }
        kill();


    }

    @Override
    protected Item getDefaultItem() {
        return Items.ACACIA_BOAT;
    }

    @Override
    public ItemStack getStack() {
        return super.getStack();
    }

    public Packet<?> createSpawnPacket() {
        Entity entity = in;
        return new EntitySpawnS2CPacket(this, entity == null ? 0 : entity.getId());
    }
}
