package ace.actually.pirates.blocks.entity;

import ace.actually.pirates.Pirates;
import ace.actually.pirates.entities.pirate_default.PirateEntity;
import ace.actually.pirates.entities.pirate_skeleton.SkeletonPirateEntity;
import ace.actually.pirates.util.CrewSpawnType;
import ace.actually.pirates.util.ModProperties;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.VillagerType;
import net.minecraft.world.World;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

public class CrewSpawnerBlockEntity extends BlockEntity {

    public int countdown = 0;

    public CrewSpawnerBlockEntity(BlockPos pos, BlockState state) {
        super(Pirates.CREW_SPAWNER_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, CrewSpawnerBlockEntity be) {
        if (world.getGameRules().getBoolean(Pirates.PIRATES_IS_LIVE_WORLD)) {
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

                spawnCrew(world, be);
            } else {
                be.countdown++;
            }
        }
    }

    private static void spawnCrew(World world, CrewSpawnerBlockEntity be) {
        Entity crew = getEntityFromState(world, be);

        if (crew != null) {
            crew.setPosition(be.getPos().toCenterPos());
            world.spawnEntity(crew);
        }

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

    private static Entity getEntityFromState(World world, BlockEntity be) {
        Entity crew = null;
        if (be.getCachedState().get(ModProperties.CREW_SPAWN_TYPE) == CrewSpawnType.PIRATE) {
            crew = new PirateEntity(world, checkForBlocksToCrew(world, be.getPos()));
            crew.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
        } else if (be.getCachedState().get(ModProperties.CREW_SPAWN_TYPE) == CrewSpawnType.VILLAGER) {
            crew = new VillagerEntity(EntityType.VILLAGER, world, VillagerType.forBiome(world.getBiome(be.getPos())));
        } else if (be.getCachedState().get(ModProperties.CREW_SPAWN_TYPE) == CrewSpawnType.SKELETON_PIRATE) {
            BlockPos blockToCrew = checkForBlocksToCrew(world, be.getPos());
            crew = new SkeletonPirateEntity(world, blockToCrew);
            ItemStack itemStack = new ItemStack(Items.BOW);
            if (world.getBlockState(blockToCrew).isOf(Pirates.MOTION_INVOKING_BLOCK)) {
                itemStack.addEnchantment(Enchantments.POWER, 2);
            }
            crew.equipStack(EquipmentSlot.MAINHAND, itemStack);
        }

        //Mixin here to add custom entities

        return crew;
    }

}
