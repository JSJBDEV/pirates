package ace.actually.pirates.blocks.entity;

import ace.actually.pirates.Pirates;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

public class DamagedHullBlockEntity extends BlockEntity {

    private double currentMass = 1000.0; // Starts light → increases over time

    public DamagedHullBlockEntity(BlockPos pos, BlockState state) {
        super(Pirates.DAMAGED_HULL_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, DamagedHullBlockEntity blockEntity) {
        if (world.isClient) return;

        // Simulate water flooding
        blockEntity.currentMass += 100.0; // Increase mass per tick (tune this!)
        if (blockEntity.currentMass > 10000.0) {
            blockEntity.currentMass = 10000.0; // Cap at max mass (e.g. Netherite mass)
        }
    }

    public double getCurrentMass() {
        return currentMass;
    }
}
