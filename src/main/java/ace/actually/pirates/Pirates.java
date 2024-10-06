package ace.actually.pirates;

import ace.actually.pirates.blocks.*;
import ace.actually.pirates.entities.ShotEntity;
import ace.actually.pirates.entities.pirate.PirateEntity;
import ace.actually.pirates.items.RaycastingItem;
import ace.actually.pirates.items.TestingStickItem;
import ace.actually.pirates.sound.ModSounds;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Pirates implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("pirates");






	public static boolean isLiveWorld = true;
	@Override
	public void onInitialize() {
		registerEntityThings();
		//entity types do it themselves
		registerBlocks();
		registerItems();
		//block entities do it themselves
		//registerDispenserThings();
		PatternProcessor.setupBasicPatterns();
		//ConfigUtils.checkConfigs();
		ModSounds.registerSounds();
		LOGGER.info("Let there be motion!");


		//BiomeModifications.addSpawn(BiomeSelectors.categories(Biome.Category.OCEAN), SpawnGroup.WATER_CREATURE, Pirates.SHIP, 3, 1, 1);


	}


	private void registerEntityThings()
	{

		FabricDefaultAttributeRegistry.register(PIRATE_ENTITY_TYPE, PirateEntity.attributes());
	}


	public static final MotionInvokingBlock MOTION_INVOKING_BLOCK = new MotionInvokingBlock(AbstractBlock.Settings.copy(Blocks.BIRCH_WOOD).noBlockBreakParticles().hardness(7));
	public static final CannonPrimingBlock CANNON_PRIMING_BLOCK = new CannonPrimingBlock(AbstractBlock.Settings.copy(Blocks.DISPENSER).hardness(5));
	public static final DispenserCannonBlock DISPENSER_CANNON_BLOCK = new DispenserCannonBlock(AbstractBlock.Settings.copy(Blocks.DISPENSER).hardness(5));
	public static final CrewSpawnerBlock CREW_SPAWNER_BLOCK = new CrewSpawnerBlock(AbstractBlock.Settings.copy(Blocks.BIRCH_WOOD).noBlockBreakParticles().noCollision());
	private void registerBlocks()
	{
		Registry.register(Registries.BLOCK,new Identifier("pirates","cannon_priming_block"),CANNON_PRIMING_BLOCK);
		Registry.register(Registries.BLOCK,new Identifier("pirates","motion_invoking_block"),MOTION_INVOKING_BLOCK);
		Registry.register(Registries.BLOCK,new Identifier("pirates","dispenser_cannon_block"),DISPENSER_CANNON_BLOCK);
		Registry.register(Registries.BLOCK,new Identifier("pirates","crew_spawner_block"),CREW_SPAWNER_BLOCK);

	}


	public static final TestingStickItem TESTING_STICK_ITEM = new TestingStickItem(new Item.Settings());
	public static final RaycastingItem RAYCASTING_ITEM = new RaycastingItem(new Item.Settings());
	public static final Item CANNONBALL = new Item(new Item.Settings());
	public static final Item CANNONBALL_ENT = new Item(new Item.Settings());
	private void registerItems()
	{
		Registry.register(Registries.ITEM,new Identifier("pirates","testing_stick"),TESTING_STICK_ITEM);
		Registry.register(Registries.ITEM,new Identifier("pirates","raycaster"),RAYCASTING_ITEM);
		Registry.register(Registries.ITEM,new Identifier("pirates","cannonball"),CANNONBALL);
		Registry.register(Registries.ITEM,new Identifier("minecraft","p_util"),CANNONBALL_ENT);

		Registry.register(Registries.ITEM,new Identifier("pirates","cannon_priming_block"),new BlockItem(CANNON_PRIMING_BLOCK,new Item.Settings()));
//		Registry.register(Registries.ITEM,new Identifier("pirates","motion_invoking_block"),new BlockItem(MOTION_INVOKING_BLOCK,new Item.Settings()));
//		Registry.register(Registries.ITEM,new Identifier("pirates","crew_spawner_block"),new BlockItem(CREW_SPAWNER_BLOCK,new Item.Settings()));

	}


	//block entities
	public static final BlockEntityType<MotionInvokingBlockEntity> MOTION_INVOKING_BLOCK_ENTITY = Registry.register(
			Registries.BLOCK_ENTITY_TYPE,
			new Identifier("pirates", "motion_invoking_block_entity"),
			FabricBlockEntityTypeBuilder.create(MotionInvokingBlockEntity::new, MOTION_INVOKING_BLOCK).build()
	);
	public static final BlockEntityType<CannonPrimingBlockEntity> CANNON_PRIMING_BLOCK_ENTITY = Registry.register(
			Registries.BLOCK_ENTITY_TYPE,
			new Identifier("pirates", "cannon_priming_block_entity"),
			FabricBlockEntityTypeBuilder.create(CannonPrimingBlockEntity::new, CANNON_PRIMING_BLOCK).build()
	);
	public static final BlockEntityType<CrewSpawnerBlockEntity> CREW_SPAWNER_BLOCK_ENTITY = Registry.register(
			Registries.BLOCK_ENTITY_TYPE,
			new Identifier("pirates", "crew_spawner_block_entity"),
			FabricBlockEntityTypeBuilder.create(CrewSpawnerBlockEntity::new, CREW_SPAWNER_BLOCK).build()
	);


	//entities
	public static final EntityType<ShotEntity> SHOT_ENTITY_TYPE =registerEntity("shot",SpawnGroup.MISC,EntityDimensions.changing(0.5f,0.5f),((type, world) -> new ShotEntity(world)));

	public static final EntityType<PirateEntity> PIRATE_ENTITY_TYPE =registerEntity("pirate",SpawnGroup.MISC,EntityDimensions.changing(0.6f,1.9f),((type, world) -> new PirateEntity(world)));


	public static <T extends Entity> EntityType<T> registerEntity(String name, SpawnGroup category, EntityDimensions size, EntityType.EntityFactory<T> factory) {
		return Registry.register(Registries.ENTITY_TYPE, new Identifier("pirates", name), FabricEntityTypeBuilder.create(category, factory).dimensions(size).build());
	}


}