package ace.actually.pirates.blocks.entity;

import ace.actually.pirates.Pirates;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.GameTickForceApplier;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

public class StableBlockEntity extends BlockEntity {
    double multiplier = 1;

    public StableBlockEntity(BlockPos pos, BlockState state) {
        super(Pirates.STABLE_BLOCK_ENTITY, pos, state);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        multiplier=nbt.getDouble("multiplier");
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        nbt.putDouble("multiplier",multiplier);
        super.writeNbt(nbt);
    }

    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
        markDirty();
    }

    public static void tick(World world, BlockPos pos, BlockState state, StableBlockEntity be)
    {
        if(world instanceof ServerWorld serverWorld)
        {
            if(VSGameUtilsKt.isBlockInShipyard(serverWorld,pos))
            {
                LoadedServerShip ship = VSGameUtilsKt.getShipObjectManagingPos(serverWorld,pos);
                if(ship!=null)
                {
                    GameTickForceApplier gtfa = ship.getAttachment(GameTickForceApplier.class);

                    if(gtfa!=null)
                    {
                        Vec3i vec3i = Vec3i.ZERO.up();

                        Vector3d v3d = VectorConversionsMCKt.toJOMLD(vec3i).mul(be.multiplier*ship.getInertiaData().getMass());
                        Vector3d loc = new Vector3d(pos.getX(),pos.getY(),pos.getZ()).sub(ship.getTransform().getPositionInShip());
                        gtfa.applyInvariantForceToPos(v3d,loc);
                        //gtfa.applyInvariantForce(v3d);
                    }

                }

            }
        }
    }
}
