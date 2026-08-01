package ace.actually.pirates.entities.shot;

import ace.actually.pirates.Pirates;
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

import java.util.function.Consumer;

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
                explode(null);
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
            BlockPos impactPos = hitResult instanceof BlockHitResult blockHitResult
                    ? blockHitResult.getBlockPos()
                    : null;
            explode(impactPos);
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        Entity entity = entityHitResult.getEntity();
        entity.damage(this.getDamageSources().explosion(null), damage);

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
    private boolean isBelowWaterLine(BlockPos pos, Ship ship, Vec3d shotDirection) {
        // Convert BlockPos to world coordinates
        Vector3d worldVec = VSGameUtilsKt.toWorldCoordinates(ship,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

        // Convert to world BlockPos
        BlockPos worldPos = new BlockPos((int) Math.floor(worldVec.x()),
                (int) Math.floor(worldVec.y()),
                (int) Math.floor(worldVec.z()));

        // Step 1️⃣ — Check if water below (depth 3)
        boolean waterBelow = false;
        for (int i = 0; i < 3; i++) {
            BlockPos checkPos = worldPos.down(i);
            BlockState state = this.getWorld().getBlockState(checkPos);
            if (state.isOf(Blocks.WATER)) {
                waterBelow = true;
                break;
            }
        }
        if (!waterBelow) {
            // Debug:
            // System.out.println("No water below → no leak");
            return false; // Not below waterline → skip further checks
        }

        // Step 2️⃣ — Check surrounding blocks to determine if hull or thin structure (mast, etc.)
        // Calculate "left" and "right" vectors based on shot direction
        Vec3d left = shotDirection.crossProduct(new Vec3d(0, 1, 0)).normalize(); // Left
        Vec3d right = left.multiply(-1); // Right

        // Prepare surrounding positions
        BlockPos leftPos = worldPos.add((int) Math.round(left.x), 0, (int) Math.round(left.z));
        BlockPos rightPos = worldPos.add((int) Math.round(right.x), 0, (int) Math.round(right.z));
        BlockPos upPos = worldPos.up();
        BlockPos downPos = worldPos.down();

        // Get block states
        BlockState centerState = this.getWorld().getBlockState(worldPos);
        BlockState leftState = this.getWorld().getBlockState(leftPos);
        BlockState rightState = this.getWorld().getBlockState(rightPos);
        BlockState upState = this.getWorld().getBlockState(upPos);
        BlockState downState = this.getWorld().getBlockState(downPos);

        // Count how many neighbors match center block
        int sameCount = 0;
        if (leftState.getBlock() == centerState.getBlock()) sameCount++;
        if (rightState.getBlock() == centerState.getBlock()) sameCount++;
        if (upState.getBlock() == centerState.getBlock()) sameCount++;
        if (downState.getBlock() == centerState.getBlock()) sameCount++;

        // Step 3️⃣ — Threshold for "is this block part of hull"
        int threshold = 2; // Tune this — recommend 2 or 3

        if (sameCount >= threshold) {
            // Debug:
            // System.out.println("HULL detected! sameCount=" + sameCount);
            return true; // Surrounded → likely hull → leak
        } else {
            // Debug:
            // System.out.println("THIN structure (mast/beam), sameCount=" + sameCount + " → no leak");
            return false; // Thin structure → no leak
        }
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
    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);
        if(extra.contains("heavy"))
        {
            getWorld().setBlockState(blockHitResult.getBlockPos(), Pirates.HEAVY_BLOCK.getDefaultState());
        }

    }

    private void explode(BlockPos exactImpactPos) {
        World world = this.getWorld();
        BlockPos impactPos = exactImpactPos != null ? exactImpactPos : this.getBlockPos();
        BlockState state = world.getBlockState(impactPos);

        // Destroy only the exact block reported by the collision result.
        if (!state.isAir() && state.getHardness(world, impactPos) >= 0) {
            world.breakBlock(impactPos, true);
        }

        applyExplosionEffects(world, impactPos);
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
