package ace.actually.pirates.blocks.entity;

import ace.actually.pirates.Pirates;
import ace.actually.pirates.blocks.CannonPrimingBlock;
import ace.actually.pirates.entities.pirate_abstract.AbstractPirateEntity;
import ace.actually.pirates.entities.pirate_default.PirateEntity;
import ace.actually.pirates.events.IPirateSpawns;
import ace.actually.pirates.entities.CrewTypes;
import ace.actually.pirates.util.ConfigUtils;
import ace.actually.pirates.compat.MusketModCompat;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
//import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.VillagerType;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import net.minecraft.world.EntityList;

import java.util.Optional;


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
            crew.setPosition(be.getPos().toCenterPos().add(0,-0.5,0));
            world.spawnEntity(crew);
            if(crew instanceof AbstractPirateEntity ape)
            {
                IPirateSpawns.EVENT.invoker().interact(ape);
            }

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

        switch (be.getCachedState().get(CrewTypes.CREW_SPAWN_TYPE))
        {
            case PIRATE ->
            {
                crew = new PirateEntity(world, checkForBlocksToCrew(world, be.getPos()));
                MusketModCompat.equipRandomGunOrBow((AbstractPirateEntity) crew, world.random);
            }
            case VILLAGER ->  crew = new VillagerEntity(EntityType.VILLAGER, world, VillagerType.forBiome(world.getBiome(be.getPos())));
            case SKELETON_PIRATE ->
            {
                BlockPos blockToCrew = checkForBlocksToCrew(world, be.getPos());
                crew = new PirateEntity(world, blockToCrew);
                Item randomGun = MusketModCompat.randomGun(world.random);
                ItemStack itemStack = new ItemStack(randomGun == null ? net.minecraft.item.Items.BOW : randomGun);
                if (world.getBlockState(blockToCrew).isOf(Pirates.MOTION_INVOKING_BLOCK)) {
                    itemStack.addEnchantment(Enchantments.POWER, 2);
                }
                crew.equipStack(EquipmentSlot.MAINHAND, itemStack);
                if(world instanceof ServerWorld serverWorld)
                {
                    serverWorld.setWeather(0, 36000, true, true);
                }

            }
            case CUSTOM_0 ->
            {
                crew = makeCustomCrew(world,0);
            }
            case CUSTOM_1 ->
            {
                crew = makeCustomCrew(world,1);
            }
            case CUSTOM_2 ->
            {
                crew = makeCustomCrew(world,2);
            }
            case CUSTOM_3 ->
            {
                crew = makeCustomCrew(world,3);
            }
        }

        //Mixin here to add custom entities

        return crew;
    }

    public static Entity makeCustomCrew(@NotNull World world, int number)
    {
        String id = (ConfigUtils.config.getOrDefault("custom-crew-entity-"+number,"minecraft:zombie"));
        EntityType<?> j = null;
        if (EntityType.get(id).isPresent()) {
            j = EntityType.get(id).get();
        }
        assert j != null;

        Entity crew = j.create(world);
        String equipments = ConfigUtils.config.getOrDefault("custom-crew-equipment-"+number,"0,0,0,0,0,0");
        String[] parts = equipments.split(",");
        if(parts.length==6)
        {
            for (int i = 0; i < parts.length; i++) {
                if(!parts[i].equals("0") && !parts[i].equals("null") && !parts[i].isEmpty())
                {
                    Item item = Registries.ITEM.get(Identifier.tryParse(parts[i].replace(" ","")));
                    switch (i)
                    {
                        case 0 -> crew.equipStack(EquipmentSlot.MAINHAND,new ItemStack(item));
                        case 1 -> crew.equipStack(EquipmentSlot.OFFHAND,new ItemStack(item));
                        case 2 -> crew.equipStack(EquipmentSlot.HEAD,new ItemStack(item));
                        case 3 -> crew.equipStack(EquipmentSlot.CHEST,new ItemStack(item));
                        case 4 -> crew.equipStack(EquipmentSlot.LEGS,new ItemStack(item));
                        case 5 -> crew.equipStack(EquipmentSlot.FEET,new ItemStack(item));
                    }
                }

            }
        }
        return crew;
    }

}
