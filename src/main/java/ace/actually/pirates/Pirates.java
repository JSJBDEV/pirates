package ace.actually.pirates;

import ace.actually.pirates.blocks.*;
import ace.actually.pirates.blocks.entity.CannonPrimingBlockEntity;
import ace.actually.pirates.blocks.entity.CrewSpawnerBlockEntity;
import ace.actually.pirates.blocks.entity.MotionInvokingBlockEntity;
import ace.actually.pirates.blocks.entity.StableBlockEntity;
import ace.actually.pirates.entities.friendly_pirate.FriendlyPirateEntity;
import ace.actually.pirates.entities.shot.ShotEntity;
import ace.actually.pirates.entities.pirate_default.PirateEntity;
import ace.actually.pirates.entities.pirate_skeleton.SkeletonPirateEntity;
import ace.actually.pirates.events.IPirateDies;
import ace.actually.pirates.items.CannoneerItem;
import ace.actually.pirates.items.ShipPather;
import ace.actually.pirates.items.ShipPointer;
import ace.actually.pirates.sound.ModSounds;
import ace.actually.pirates.util.ConfigUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
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
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Pirates implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final String MOD_ID = "pirates";
    public static final Logger LOGGER = LoggerFactory.getLogger("pirates");

	public static final GameRules.Key<GameRules.BooleanRule> PIRATES_IS_LIVE_WORLD =
			GameRuleRegistry.register("piratesIsLive", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(true));

	public static final RegistryKey<ItemGroup> PIRATES_ITEM_GROUP_KEY = RegistryKey.of(Registries.ITEM_GROUP.getKey(), Identifier.of(MOD_ID, "item_group"));
	public static final ItemGroup PIRATES_ITEM_GROUP = FabricItemGroup.builder()
			.icon(() -> new ItemStack(Pirates.CANNONBALL))
			.displayName(Text.of("Valkyrien Pirates"))
			.build();

	public static float baseShotPower;
	public static int pursuitDistance;
	public static boolean shouldEnableFlyingPirates;

	@Override
	public void onInitialize() {

		ConfigUtils.checkConfigs();
		baseShotPower = Float.parseFloat(ConfigUtils.config.getOrDefault("base-shot-power","2.2"));
		pursuitDistance = Integer.parseInt(ConfigUtils.config.getOrDefault("pursuit-distance","10000"));
		shouldEnableFlyingPirates = ConfigUtils.config.getOrDefault("should-enable-flying-pirates","false").equals("true");
		registerEntityThings();
		//entity types do it themselves
		registerBlocks();
		registerItems();
		//block entities do it themselves
		//registerDispenserThings();
		ModSounds.registerSounds();
		LOGGER.info("Let there be motion!");

		Registry.register(Registries.ITEM_GROUP, PIRATES_ITEM_GROUP_KEY, PIRATES_ITEM_GROUP);

		ItemGroupEvents.modifyEntriesEvent(PIRATES_ITEM_GROUP_KEY).register(itemGroup -> {
			itemGroup.add(Pirates.CANNONBALL);
			itemGroup.add(Pirates.CANNON_PRIMING_BLOCK.asItem());
			itemGroup.add(Pirates.CREW_SPAWNER_BLOCK.asItem());
			itemGroup.add(Pirates.MOTION_INVOKING_BLOCK.asItem());
			itemGroup.add(Pirates.SHIP_POINTER);
			itemGroup.add(Pirates.SHIP_PATHER);
		});

		IPirateDies.EVENT.register((player, pirate) ->
		{
			//System.out.println(pirate.getUuidAsString());
			return ActionResult.PASS;
		});


	}


	private void registerEntityThings()
	{

		FabricDefaultAttributeRegistry.register(PIRATE_ENTITY_TYPE, PirateEntity.attributes());
		FabricDefaultAttributeRegistry.register(FRIENDLY_PIRATE_TYPE, FriendlyPirateEntity.attributes());
		//FabricDefaultAttributeRegistry.register(SKELETON_PIRATE_ENTITY_TYPE, SkeletonPirateEntity.attributes());

	}

	private static final BlockSoundGroup Silent = new BlockSoundGroup(0, 0, SoundEvents.ENTITY_COD_AMBIENT, SoundEvents.ENTITY_COD_AMBIENT, SoundEvents.ENTITY_COD_AMBIENT, SoundEvents.ENTITY_COD_AMBIENT, SoundEvents.ENTITY_COD_AMBIENT);

	public static final MotionInvokingBlock MOTION_INVOKING_BLOCK = new MotionInvokingBlock(AbstractBlock.Settings.copy(Blocks.BIRCH_WOOD).noBlockBreakParticles().hardness(7).dropsNothing());
	public static final CannonPrimingBlock CANNON_PRIMING_BLOCK = new CannonPrimingBlock(AbstractBlock.Settings.copy(Blocks.DISPENSER).hardness(5));
	public static final DispenserCannonBlock DISPENSER_CANNON_BLOCK = new DispenserCannonBlock(AbstractBlock.Settings.copy(Blocks.DISPENSER).hardness(5));
	public static final CrewSpawnerBlock CREW_SPAWNER_BLOCK = new CrewSpawnerBlock(AbstractBlock.Settings.copy(Blocks.BIRCH_WOOD).noBlockBreakParticles().noCollision().dropsNothing().sounds(Silent));
	public static final StableBlock STABLE_BLOCK = new StableBlock(AbstractBlock.Settings.create());
	private void registerBlocks()
	{
		Registry.register(Registries.BLOCK,new Identifier("pirates","cannon_priming_block"),CANNON_PRIMING_BLOCK);
		Registry.register(Registries.BLOCK,new Identifier("pirates","motion_invoking_block"),MOTION_INVOKING_BLOCK);
		Registry.register(Registries.BLOCK,new Identifier("pirates","dispenser_cannon_block"),DISPENSER_CANNON_BLOCK);
		Registry.register(Registries.BLOCK,new Identifier("pirates","crew_spawner_block"),CREW_SPAWNER_BLOCK);
		Registry.register(Registries.BLOCK,new Identifier("pirates","stable_block"),STABLE_BLOCK);

	}



	public static final Item CANNONBALL = new Item(new Item.Settings());
	public static final Item CANNONBALL_ENT = new Item(new Item.Settings());
	public static final ShipPointer SHIP_POINTER = new ShipPointer(new Item.Settings());
	public static final ShipPather SHIP_PATHER = new ShipPather(new Item.Settings());
	public static final CannoneerItem CANNONEER_ITEM = new CannoneerItem(new Item.Settings());
	private void registerItems()
	{
		Registry.register(Registries.ITEM,new Identifier("pirates","cannonball"),CANNONBALL);
		Registry.register(Registries.ITEM,new Identifier("util_pirates","util_1"),CANNONBALL_ENT);
		Registry.register(Registries.ITEM,new Identifier("pirates","ship_pointer"),SHIP_POINTER);
		Registry.register(Registries.ITEM,new Identifier("pirates","cannoneer"),CANNONEER_ITEM);
		Registry.register(Registries.ITEM,new Identifier("pirates","ship_pather"),SHIP_PATHER);
		Registry.register(Registries.ITEM,new Identifier("pirates","stable_block"),new BlockItem(STABLE_BLOCK,new Item.Settings()));

		Registry.register(Registries.ITEM,new Identifier("pirates","cannon_priming_block"),new BlockItem(CANNON_PRIMING_BLOCK,new Item.Settings()));

		Registry.register(Registries.ITEM,new Identifier("pirates","motion_invoking_block"),new BlockItem(MOTION_INVOKING_BLOCK,new Item.Settings()));
		Registry.register(Registries.ITEM,new Identifier("pirates","crew_spawner_block"),new BlockItem(CREW_SPAWNER_BLOCK,new Item.Settings()));

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
	public static final BlockEntityType<StableBlockEntity> STABLE_BLOCK_ENTITY = Registry.register(
			Registries.BLOCK_ENTITY_TYPE,
			new Identifier("pirates", "stable_block_entity"),
			FabricBlockEntityTypeBuilder.create(StableBlockEntity::new, STABLE_BLOCK).build()
	);


	//entities
	public static final EntityType<ShotEntity> SHOT_ENTITY_TYPE =registerEntity("shot",SpawnGroup.MISC,EntityDimensions.changing(0.5f,0.5f),((type, world) -> new ShotEntity(world)));

	public static final EntityType<PirateEntity> PIRATE_ENTITY_TYPE =registerEntity("pirate",SpawnGroup.MISC,EntityDimensions.changing(0.6f,1.9f),((type, world) -> new PirateEntity(world)));

	public static final EntityType<FriendlyPirateEntity> FRIENDLY_PIRATE_TYPE =registerEntity("friendly_pirate",SpawnGroup.MISC,EntityDimensions.changing(0.6f,1.9f),((type, world) -> new FriendlyPirateEntity(world)));


	public static final EntityType<SkeletonPirateEntity> SKELETON_PIRATE_ENTITY_TYPE = null; //=registerEntity("skeleton_pirate",SpawnGroup.MISC,EntityDimensions.changing(0.6f,1.9f),((type, world) -> new SkeletonPirateEntity(world)));



	public static <T extends Entity> EntityType<T> registerEntity(String name, SpawnGroup category, EntityDimensions size, EntityType.EntityFactory<T> factory) {
		return Registry.register(Registries.ENTITY_TYPE, new Identifier("pirates", name), FabricEntityTypeBuilder.create(category, factory).dimensions(size).build());

	}


}