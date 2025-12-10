package ace.actually.pirates.blocks.entity;

import ace.actually.pirates.Pirates;
import ace.actually.pirates.entities.pirate_abstract.AbstractPirateEntity;
import ace.actually.pirates.entities.pirate_default.PirateEntity;
import ace.actually.pirates.events.IPirateSpawns;
import ace.actually.pirates.entities.CrewTypes;
import ace.actually.pirates.util.ConfigUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

public class CrewSpawnerBlockEntity extends BlockEntity {

    public int countdown = 0;

    public CrewSpawnerBlockEntity(BlockPos pos, BlockState state) {
        super(Pirates.CREW_SPAWNER_BLOCK_ENTITY, pos, state);
    }

    public static void tick(Level world, BlockPos pos, BlockState state, CrewSpawnerBlockEntity be) {
        if (world.getGameRules().getBoolean(Pirates.PIRATES_IS_LIVE_WORLD)) {
            if (state.getValue(BlockStateProperties.CONDITIONAL)) {
                spawnCrewIfOnShip(world, be);
            } else {
                spawnCrew(world, be);
            }
        }
    }

    private static void spawnCrewIfOnShip(Level world, CrewSpawnerBlockEntity be) {
        if (!world.isClientSide() && VSGameUtilsKt.isBlockInShipyard(world, be.getBlockPos())) {
            Ship ship = VSGameUtilsKt.getShipManagingPos(world, be.getBlockPos());

            if (ship == null) return;

            if (be.countdown > 100) {

                spawnCrew(world, be);
            } else {
                be.countdown++;
            }
        }
    }

    private static void spawnCrew(Level world, CrewSpawnerBlockEntity be) {
        Entity crew = getEntityFromState(world, be);

        if (crew != null) {
            crew.setPos(be.getBlockPos().getCenter().add(0,-0.5,0));
            world.addFreshEntity(crew);
            if(crew instanceof AbstractPirateEntity ape)
            {
                IPirateSpawns.EVENT.invoker().interact(ape);
            }

        }

        world.destroyBlock(be.getBlockPos(), false);
    }

    private static BlockPos checkForBlocksToCrew (Level world, BlockPos origin) {
        BlockPos blockResult = new BlockPos(0,0,0);

        if (world.getBlockState(origin.north()).is(Pirates.CANNON_PRIMING_BLOCK)) {
            blockResult = origin.north();
        } else if (world.getBlockState(origin.east()).is(Pirates.CANNON_PRIMING_BLOCK)) {
            blockResult = origin.east();
        } else if (world.getBlockState(origin.south()).is(Pirates.CANNON_PRIMING_BLOCK)) {
            blockResult = origin.south();
        } else if (world.getBlockState(origin.west()).is(Pirates.CANNON_PRIMING_BLOCK)) {
            blockResult = origin.west();
        }

        BlockPos origin1 = origin.below();

        if (world.getBlockState(origin1.north()).is(Pirates.MOTION_INVOKING_BLOCK) || (world.getBlockState(origin1.north()).is(Pirates.CANNON_PRIMING_BLOCK))) {
            blockResult = origin1.north();
        } else if (world.getBlockState(origin1.east()).is(Pirates.MOTION_INVOKING_BLOCK) || (world.getBlockState(origin1.east()).is(Pirates.CANNON_PRIMING_BLOCK))) {
            blockResult = origin1.east();
        } else if (world.getBlockState(origin1.south()).is(Pirates.MOTION_INVOKING_BLOCK) || (world.getBlockState(origin1.south()).is(Pirates.CANNON_PRIMING_BLOCK))) {
            blockResult = origin1.south();
        } else if (world.getBlockState(origin1.west()).is(Pirates.MOTION_INVOKING_BLOCK) || (world.getBlockState(origin1.west()).is(Pirates.CANNON_PRIMING_BLOCK))) {
            blockResult = origin1.west();
        }


        return blockResult;
    }

    private static Entity getEntityFromState(Level world, BlockEntity be) {
        Entity crew = null;
        switch (be.getBlockState().getValue(CrewTypes.CREW_SPAWN_TYPE))
        {
            case PIRATE ->
            {
                crew = new PirateEntity(world, checkForBlocksToCrew(world, be.getBlockPos()));
                crew.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
            }
            case VILLAGER ->  crew = new Villager(EntityType.VILLAGER, world, VillagerType.byBiome(world.getBiome(be.getBlockPos())));
            case SKELETON_PIRATE ->
            {
                BlockPos blockToCrew = checkForBlocksToCrew(world, be.getBlockPos());
                crew = new PirateEntity(world, blockToCrew);
                ItemStack itemStack = new ItemStack(Items.BOW);
                if (world.getBlockState(blockToCrew).is(Pirates.MOTION_INVOKING_BLOCK)) {
                    itemStack.enchant(Enchantments.POWER_ARROWS, 2);
                }
                crew.setItemSlot(EquipmentSlot.MAINHAND, itemStack);
                if(world instanceof ServerLevel serverWorld)
                {
                    serverWorld.setWeatherParameters(0, 36000, true, true);
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

    public static Entity makeCustomCrew(@NotNull Level world, int number)
    {
        String id = (ConfigUtils.config.getOrDefault("custom-crew-entity-"+number,"minecraft:zombie"));
        EntityType<?> j = null;
        if (EntityType.byString(id).isPresent()) {
            j = EntityType.byString(id).get();
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
                    Item item = BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(parts[i].replace(" ","")));
                    switch (i)
                    {
                        case 0 -> crew.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(item));
                        case 1 -> crew.setItemSlot(EquipmentSlot.OFFHAND,new ItemStack(item));
                        case 2 -> crew.setItemSlot(EquipmentSlot.HEAD,new ItemStack(item));
                        case 3 -> crew.setItemSlot(EquipmentSlot.CHEST,new ItemStack(item));
                        case 4 -> crew.setItemSlot(EquipmentSlot.LEGS,new ItemStack(item));
                        case 5 -> crew.setItemSlot(EquipmentSlot.FEET,new ItemStack(item));
                    }
                }

            }
        }
        return crew;
    }

}
