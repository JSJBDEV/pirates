package ace.actually.pirates.entities.shot;

import ace.actually.pirates.Pirates;
import ace.actually.pirates.util.EurekaCompat;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
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
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.eureka.fabric.EurekaModFabric;
import org.valkyrienskies.mod.common.VSClientGameUtils;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

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
    private boolean isBelowWaterLine(BlockPos pos, Ship ship) {
        // ✅ Convert BlockPos to world coordinates using Valkyrien Skies' Vector3d
        Vector3d worldVec = VSGameUtilsKt.toWorldCoordinates(ship,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

        // ✅ Convert Vector3d to BlockPos (flooring the values to ensure a valid position)
        BlockPos worldPos = new BlockPos((int) Math.floor(worldVec.x()),
                                         (int) Math.floor(worldVec.y()),
                                         (int) Math.floor(worldVec.z()));

        // 🔹 Now check for water below in actual world space
        int depth = 3;
        for (int i = 0; i < depth; i++) {
            BlockPos checkPos = worldPos.down(i); // Move down in world coordinates
            BlockState state = this.getWorld().getBlockState(checkPos);

            if (state.isOf(Blocks.WATER)) {
                return true; // ✅ Found water below the ship
            }
        }
        return false; // ❌ No water found below
    }
    /**
     * 🔹 Applies explosion splash damage & knockback to entities near the impact area.
     */
    private void applyExplosionEffects(World world, BlockPos blockPos) {
        ServerWorld serverWorld = (ServerWorld) world;

        // 🌫️ **Spawn Debris Particles**
        serverWorld.spawnParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5,
                15, 0.4, 0.4, 0.4, 0.1);

        // 💥 **Small Shockwave Effect**
        serverWorld.spawnParticles(ParticleTypes.SMOKE,
                blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5,
                10, 0.3, 0.3, 0.3, 0.05);

        // 💨 **Trigger a Small Explosion Effect (Visual Only)**
        world.createExplosion(this, blockPos.getX(), blockPos.getY(), blockPos.getZ(),
                0.0f, extra.contains("fire"), World.ExplosionSourceType.TNT);

        // 💣 Apply Knockback & Damage to Nearby Entities
        double explosionRadius = 2.0;
        double knockbackStrength = 2.0;
        world.getOtherEntities(this, this.getBoundingBox().expand(explosionRadius)).forEach(entity -> {
            entity.damage(this.getDamageSources().explosion(null), damage);
            Vec3d pushDirection = entity.getPos().subtract(this.getPos()).normalize();
            double forceMultiplier = 1.0 - (entity.getPos().distanceTo(this.getPos()) / explosionRadius);
            Vec3d knockback = pushDirection.multiply(knockbackStrength * forceMultiplier);
            entity.addVelocity(knockback.x, knockback.y + 0.2, knockback.z);
            entity.velocityModified = true;
        });
    }
    private void damageBlockVisual(World world, BlockPos blockPos) {
        int damageStage = 9;
        if (world instanceof ServerWorld serverWorld) {
            // Assign a random entity ID for the visual effect
            int entityId = blockPos.hashCode();

            // Send block breaking animation
            serverWorld.setBlockBreakingInfo(entityId, blockPos, damageStage);
        }
    }
    private void explode() {
        World world = this.getWorld();
        BlockPos impactPos = this.getCollisionBlock(); // ✅ Get exact impact block
        if (impactPos == null) {
            impactPos = this.getBlockPos(); // Fallback in case collision is null
        }

        // 🔹 Shot direction (normalize for precision)
        Vec3d shotDirection = this.getVelocity().normalize();
        Vec3d currentPos = new Vec3d(impactPos.getX() + 0.5, impactPos.getY() + 0.5, impactPos.getZ() + 0.5); // Center on block

        int penetrationPower = 3; // 🔹 2 blocks will be pierced
        double stepSize = 1; // 🔹 Move forward 1 block per step

        boolean isShipBlock = VSGameUtilsKt.isBlockInShipyard(world, impactPos);

        if (!isShipBlock) {
            // 🔹 Ensure first block is destroyed
            BlockState blockStateFirst = world.getBlockState(impactPos);
            if (!blockStateFirst.isAir() && blockStateFirst.getHardness(world, impactPos) >= 0) {
                world.breakBlock(impactPos, true); // ✅ Destroy block
            }
            applyExplosionEffects(world, impactPos);
            // ✅ Handle Non-Ship Blocks (Piercing Shot with Breakable Blocks)
            for (int i = 0; i < penetrationPower; i++) {
                currentPos = currentPos.add(shotDirection.multiply(stepSize)); // Move forward in exact trajectory
                BlockPos blockToBreak = new BlockPos((int) Math.floor(currentPos.x),
                        (int) Math.floor(currentPos.y),
                        (int) Math.floor(currentPos.z));
                BlockState blockStateNext = world.getBlockState(blockToBreak);

                // 🔹 Ensure block is solid & breakable
                if (!blockStateNext.isAir() && blockStateNext.getHardness(world, blockToBreak) >= 0) {
                    world.breakBlock(blockToBreak, true); // ✅ Destroy block
                }

                // 💥 Apply Explosion Splash Damage & Knockback on Every Hit
                applyExplosionEffects(world, blockToBreak);
            }
        } else {
            // ✅ Handle Ship Blocks (Check if Below Waterline or Not)
            Ship ship = VSGameUtilsKt.getShipManagingPos(world, impactPos);
            if (ship == null)
                return;

            boolean isBelowWater = isBelowWaterLine(impactPos, ship);

            // 🔹 Ensure first block is affected
            BlockState blockStateFirst = world.getBlockState(impactPos);
            if (!blockStateFirst.isAir() && blockStateFirst.getHardness(world, impactPos) >= 0) {
                if (isBelowWater) {
                    // 🌊 Below Waterline: Replace Blocks with Netherite for Sinking Effect
                    world.setBlockState(impactPos, Pirates.CREW_SPAWNER_BLOCK.getDefaultState());
                } else {
                    // 🚢 Above Waterline: Apply Visual Damage & Splash Damage
                    damageBlockVisual(world, impactPos);
                    applyExplosionEffects(world, impactPos);
                }
            }
            // just for sinking
            penetrationPower = 2; // make water sink this ship in a sensible way :33
            for (int i = 0; i < penetrationPower; i++) {
                currentPos = currentPos.add(shotDirection.multiply(stepSize)); // Move forward in exact trajectory
                BlockPos blockToAffect = new BlockPos((int) Math.floor(currentPos.x),
                        (int) Math.floor(currentPos.y),
                        (int) Math.floor(currentPos.z));
                BlockState blockStateNext = world.getBlockState(blockToAffect);

                if (!blockStateNext.isAir() && blockStateNext.getHardness(world, blockToAffect) >= 0) {
                    if (isBelowWater) {
                        // 🌊 Below Waterline: Replace Blocks with Netherite for Sinking Effect
                        world.setBlockState(blockToAffect, Pirates.CREW_SPAWNER_BLOCK.getDefaultState());
                    } else {
                        // 🚢 Above Waterline: Apply Visual Damage & Splash Damage
                        damageBlockVisual(world, blockToAffect);
                        applyExplosionEffects(world, blockToAffect);
                    }
                } else {
                    // 🛠️ If the block is NOT breakable, just apply knockback & explosion effects
                    applyExplosionEffects(world, blockToAffect);
                }
            }
        }

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
