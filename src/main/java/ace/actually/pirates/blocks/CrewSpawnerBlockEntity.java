package ace.actually.pirates.blocks;

import ace.actually.pirates.Pirates;
import ace.actually.pirates.entities.pirate.PirateEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

public class CrewSpawnerBlockEntity extends BlockEntity {

    public int countdown = 0;

    public CrewSpawnerBlockEntity(BlockPos pos, BlockState state) {
        super(Pirates.CREW_SPAWNER_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, CrewSpawnerBlockEntity be) {
        if (Pirates.isLiveWorld) {


            if (state.get(Properties.CONDITIONAL)) {
                spawnCrewIfOnShip(world, be);
            } else {
                spawnCrew(world, be);
            }



        }
    }

    private static void spawnCrewIfOnShip(World world, CrewSpawnerBlockEntity be) {
        if (!world.isClient() && VSGameUtilsKt.isBlockInShipyard(world, be.getPos())) {
            Ship ship = VSGameUtilsKt.getShipManagingPos(world, be.getPos());

            if (ship == null) return;

            if (be.countdown > 100) {

                Entity pirate = new PirateEntity(world, checkForBlocksToCrew(world, be.getPos()));

                pirate.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
                pirate.setPosition(be.getPos().toCenterPos());
                world.spawnEntity(pirate);

                world.breakBlock(be.getPos(), false);
            } else {
                be.countdown++;
            }
        }
    }

    private static void spawnCrew(World world, CrewSpawnerBlockEntity be) {
        Entity pirate = new PirateEntity(world, checkForBlocksToCrew(world, be.getPos()));

        pirate.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
        pirate.setPosition(be.getPos().toCenterPos());
        world.spawnEntity(pirate);

        world.breakBlock(be.getPos(), false);
    }

    private static BlockPos checkForBlocksToCrew (World world, BlockPos origin) {
        BlockPos blockResult = new BlockPos(0,0,0);

        if (world.getBlockState(origin.north()).isOf(Pirates.CANNON_PRIMING_BLOCK)) {
            blockResult = origin.north();
        } else if (world.getBlockState(origin.east()).isOf(Pirates.CANNON_PRIMING_BLOCK)) {
            blockResult = origin.east();
        } else if (world.getBlockState(origin.south()).isOf(Pirates.CANNON_PRIMING_BLOCK)) {
            blockResult = origin.south();
        } else if (world.getBlockState(origin.west()).isOf(Pirates.CANNON_PRIMING_BLOCK)) {
            blockResult = origin.west();
        }

        BlockPos origin1 = origin.down();

        if (world.getBlockState(origin1.north()).isOf(Pirates.MOTION_INVOKING_BLOCK) || (world.getBlockState(origin1.north()).isOf(Pirates.CANNON_PRIMING_BLOCK))) {
            blockResult = origin1.north();
        } else if (world.getBlockState(origin1.east()).isOf(Pirates.MOTION_INVOKING_BLOCK) || (world.getBlockState(origin1.east()).isOf(Pirates.CANNON_PRIMING_BLOCK))) {
            blockResult = origin1.east();
        } else if (world.getBlockState(origin1.south()).isOf(Pirates.MOTION_INVOKING_BLOCK) || (world.getBlockState(origin1.south()).isOf(Pirates.CANNON_PRIMING_BLOCK))) {
            blockResult = origin1.south();
        } else if (world.getBlockState(origin1.west()).isOf(Pirates.MOTION_INVOKING_BLOCK) || (world.getBlockState(origin1.west()).isOf(Pirates.CANNON_PRIMING_BLOCK))) {
            blockResult = origin1.west();
        }


        return blockResult;
    }

}
