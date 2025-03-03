package ace.actually.pirates.structures;

import ace.actually.pirates.Pirates;
import kotlin.Triple;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.joml.Vector3i;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;


public class ShipStructurePlacementHelper {

    public static final Queue<Triple<StructureTemplate, ServerWorld, BlockPos>> shipQueue = new ArrayBlockingQueue<>(12,false);

    private static final Set<BlockPos> blacklist = new HashSet<>();



    public static void placeShipTemplate(StructureTemplate structureTemplate, ServerWorld world, BlockPos centrePos) {
        shipQueue.add(new Triple<>(structureTemplate, world, centrePos));
        Pirates.LOGGER.info("enqueuing template at {}", centrePos.toString());
    }

    public static void createShip (StructureTemplate structureTemplate, ServerWorld world, BlockPos blockPos) {
        if (blacklist.contains(blockPos)) return;
        blacklist.add(blockPos);


        ServerShip newShip = VSGameUtilsKt.getShipObjectWorld(world).createNewShipAtBlock(
                VectorConversionsMCKt.toJOML(withOceanYLevel(world, blockPos).up(structureTemplate.getSize().getY()/7)),
                false,
                1.0,
                VSGameUtilsKt.getDimensionId(world));

        newShip.setStatic(true);

        BlockPos centerPos = VectorConversionsMCKt.toBlockPos(newShip.getChunkClaim().getCenterBlockCoordinates(VSGameUtilsKt.getYRange(world), new Vector3i()));

        StructurePlacementData structurePlacementData = new StructurePlacementData();
        structurePlacementData.setPosition(centerPos);
        boolean success = structureTemplate.place(world, centerPos, centerPos, structurePlacementData, Random.create(), 2);

        Pirates.LOGGER.info("new ship id: {} mass: {}", newShip.getId(), newShip.getInertiaData().getMass());
        Pirates.LOGGER.info("Template claims to have generated successfully? {}", success);
        if (newShip.getInertiaData().getMass() < 0.1) {
            System.out.println("deleting ship");
            VSGameUtilsKt.getShipObjectWorld(world).deleteShip(newShip);
        } else {
            Pirates.LOGGER.info("ship created successfully.");
            StructureTemplateManager manager = Objects.requireNonNull(world.getServer()).getStructureTemplateManager();
            if(((CanRemoveTemplate) manager).pirates$unload(structureTemplate)) {
                Pirates.LOGGER.info("templates cleaned.");
            } else {
                Pirates.LOGGER.info("template cleanup failed.");
            }


        }

    }

    private static BlockPos withOceanYLevel(ServerWorld world, BlockPos source) {
        return new BlockPos(source.getX(), world.getSeaLevel(), source.getZ());
    }
}
