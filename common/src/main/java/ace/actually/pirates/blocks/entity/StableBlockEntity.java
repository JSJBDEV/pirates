package ace.actually.pirates.blocks.entity;

import ace.actually.pirates.Pirates;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.util.GameToPhysicsAdapter;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

public class StableBlockEntity extends BlockEntity {
    double multiplier = 1;

    public StableBlockEntity(BlockPos pos, BlockState state) {
        super(Pirates.STABLE_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        multiplier=nbt.getDouble("multiplier");
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.putDouble("multiplier",multiplier);
        super.saveAdditional(nbt);
    }

    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
        setChanged();
    }

    public static void tick(Level world, BlockPos pos, BlockState state, StableBlockEntity be)
    {
        if(world instanceof ServerLevel serverWorld)
        {
            if(VSGameUtilsKt.isBlockInShipyard(serverWorld,pos))
            {
                LoadedServerShip ship = VSGameUtilsKt.getShipObjectManagingPos(serverWorld,pos);
                if(ship!=null)
                {

                    GameToPhysicsAdapter gtfa = ValkyrienSkiesMod.getOrCreateGTPA(VSGameUtilsKt.getDimensionId(world));

                    Vec3i vec3i = Vec3i.ZERO.above();

                    Vector3d v3d = VectorConversionsMCKt.toJOMLD(vec3i).mul(be.multiplier*ship.getInertiaData().getMass());
                    Vector3d loc = new Vector3d(pos.getX(),pos.getY(),pos.getZ()).sub(ship.getTransform().getPositionInShip());
                    gtfa.applyInvariantForceToPos(ship.getId(),v3d,loc);

                }

            }
        }
    }
}
