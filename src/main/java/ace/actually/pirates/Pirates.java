package ace.actually.pirates;

import ace.actually.pirates.blocks.*;
import ace.actually.pirates.blocks.entity.*;
import ace.actually.pirates.entities.friendly_pirate.FriendlyPirateEntity;
import ace.actually.pirates.entities.shot.ShotEntity;
import ace.actually.pirates.entities.pirate_default.PirateEntity;
import ace.actually.pirates.entities.pirate_skeleton.SkeletonPirateEntity;
import ace.actually.pirates.events.IPirateDies;
import ace.actually.pirates.items.ContractItem;
import ace.actually.pirates.items.ShipPather;
import ace.actually.pirates.items.ShipPointer;
import ace.actually.pirates.items.TestItem;
import ace.actually.pirates.sound.ModSounds;
import ace.actually.pirates.util.ConfigUtils;
import g_mungus.vlib.VLib;
import g_mungus.vlib.api.VLibGameUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
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

import java.util.Optional;
import java.util.function.Supplier;

public class Pirates implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final String MOD_ID = "pirates";
    public static final Logger LOGGER = LoggerFactory.getLogger("pirates");
	public static final Identifier CANNON_SMOKE_PACKET_ID = new Identifier(MOD_ID, "cannon_smoke");
	public static final GameRules.Key<GameRules.BooleanRule> PIRATES_IS_LIVE_WORLD =
			GameRuleRegistry.register("piratesIsLive", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(true));

	public static final RegistryKey<ItemGroup> PIRATES_ITEM_GROUP_KEY = RegistryKey.of(Registries.ITEM_GROUP.getKey(), Identifier.of(MOD_ID, "item_group"));
	public static final ItemGroup PIRATES_ITEM_GROUP = FabricItemGroup.builder()
			.icon(() -> new ItemStack(Pirates.CANNONBALL))
			.displayName(Text.of("Valkyrien Pirates"))
			.build();

	public static float baseShotPower;
	public static float cannonRange;
	public static int pursuitDistance;
	public static boolean shouldEnableFlyingPirates;
	public static Supplier<ItemStack> recruitCost;
	public static CompatTracker loadedCompats = new CompatTracker();

	@Override
	public void onInitialize() {

		Optional<ModContainer> container = FabricLoader.getInstance().getModContainer(Pirates.MOD_ID);

		if (FabricLoader.getInstance().isModLoaded("vs_sails")) {
			loadedCompats.sails = true;

			if (container.isPresent()) {
				if(ResourceManagerHelper.registerBuiltinResourcePack(
						new Identifier("sails_ships"),
						container.get(),
						ResourcePackActivationType.DEFAULT_ENABLED
				)) {
					LOGGER.info("Registered vs_sails ships data pack");
				} else {
					LOGGER.warn("vs_sails ships data pack didn't work");
				}
			} else {
				LOGGER.warn("Failed to register vs_sails ships data pack");
			}
		}
		if (FabricLoader.getInstance().isModLoaded("vs_eureka")) {
			loadedCompats.eureka = true;

			if (container.isPresent()) {
				ResourcePackActivationType eurekaActivation =
						loadedCompats.sails ? ResourcePackActivationType.NORMAL : ResourcePackActivationType.DEFAULT_ENABLED;

				if(ResourceManagerHelper.registerBuiltinResourcePack(
						new Identifier("eureka_ships"),
						container.get(),
						eurekaActivation
				)) {
					LOGGER.info("Registered vs_eureka ships data pack");
				} else {
					LOGGER.warn("vs_eureka ships data pack didn't work");
				}
			} else {
				LOGGER.warn("Failed to register vs_eureka ships data pack");
			}

			if (container.isPresent()) {
				if(ResourceManagerHelper.registerBuiltinResourcePack(
						new Identifier("flying_ships"),
						container.get(),
						ResourcePackActivationType.NORMAL
				)) {
					LOGGER.info("Registered flying ships data pack");
				} else {
					LOGGER.warn("didn't work");
				}
			} else {
				LOGGER.warn("Failed to register flying ships data pack");
			}
		}

		ConfigUtils.checkConfigs();
		baseShotPower = Float.parseFloat(ConfigUtils.config.getOrDefault("base-shot-power","2.2"));
		cannonRange = Float.parseFloat(ConfigUtils.config.getOrDefault("cannon-range","3.2"));
		pursuitDistance = Integer.parseInt(ConfigUtils.config.getOrDefault("pursuit-distance","10000"));
		shouldEnableFlyingPirates = ConfigUtils.config.getOrDefault("should-enable-flying-pirates","false").equals("true");

		String[] rc = ConfigUtils.config.getOrDefault("recruit-cost","minecraft:golden_apple,1").split(",");
		recruitCost = () -> new ItemStack(Registries.ITEM.get(Identifier.tryParse(rc[0])),Integer.parseInt(rc[1]));

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
			itemGroup.add(Pirates.FIRE_CANNONBALL);
			itemGroup.add(Pirates.WEIGHTED_CANNONBALL);
			itemGroup.add(Pirates.SHIP_ID_BLOCK);
			itemGroup.add(Pirates.CANNON_PRIMING_BLOCK.asItem());
			itemGroup.add(Pirates.CREW_SPAWNER_BLOCK.asItem());
			itemGroup.add(Pirates.MOTION_INVOKING_BLOCK.asItem());
			itemGroup.add(Pirates.SHIP_POINTER);
			itemGroup.add(Pirates.SHIP_PATHER);
			itemGroup.add(Pirates.CANNONEER_ITEM);
			itemGroup.add(Pirates.DOCTOR_ITEM);
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
	public static final ShipIdBlock SHIP_ID_BLOCK = new ShipIdBlock(AbstractBlock.Settings.create());
	public static final Block HEAVY_BLOCK = new Block(AbstractBlock.Settings.copy(Blocks.OBSIDIAN));
	public static final DamagedHullBlock DAMAGED_HULL_BLOCK = new DamagedHullBlock(
			AbstractBlock.Settings.copy(Blocks.NETHERITE_BLOCK)
					.strength(0.5f, 1200.0f)
					.noBlockBreakParticles()
					.noCollision()
					.dropsNothing()
	);
	public static final BlockEntityType<DamagedHullBlockEntity> DAMAGED_HULL_BLOCK_ENTITY = Registry.register(
			Registries.BLOCK_ENTITY_TYPE,
			new Identifier("pirates", "damaged_hull_block_entity"),
			FabricBlockEntityTypeBuilder.create(DamagedHullBlockEntity::new, DAMAGED_HULL_BLOCK).build()
	);
	private void registerBlocks()
	{
		Registry.register(Registries.BLOCK,new Identifier("pirates","cannon_priming_block"),CANNON_PRIMING_BLOCK);
		Registry.register(Registries.BLOCK,new Identifier("pirates","motion_invoking_block"),MOTION_INVOKING_BLOCK);
		Registry.register(Registries.BLOCK,new Identifier("pirates","dispenser_cannon_block"),DISPENSER_CANNON_BLOCK);
		Registry.register(Registries.BLOCK,new Identifier("pirates","crew_spawner_block"),CREW_SPAWNER_BLOCK);
		Registry.register(Registries.BLOCK,new Identifier("pirates","stable_block"),STABLE_BLOCK);
		Registry.register(Registries.BLOCK,new Identifier("pirates","ship_id_block"),SHIP_ID_BLOCK);
		Registry.register(Registries.BLOCK,new Identifier("pirates","heavy_block"),HEAVY_BLOCK);

		// my custom blocks
		Registry.register(Registries.BLOCK, new Identifier("pirates", "damaged_hull_block"), DAMAGED_HULL_BLOCK);
	}



	public static final Item CANNONBALL = new Item(new Item.Settings());
	public static final Item FIRE_CANNONBALL = new Item(new Item.Settings());
	public static final Item WEIGHTED_CANNONBALL = new Item(new Item.Settings());
	public static final Item CANNONBALL_ENT = new Item(new Item.Settings());
	public static final ShipPointer SHIP_POINTER = new ShipPointer(new Item.Settings());
	public static final ShipPather SHIP_PATHER = new ShipPather(new Item.Settings());
	public static final ContractItem CANNONEER_ITEM = new ContractItem(CANNON_PRIMING_BLOCK,"cannoneer");
	public static final ContractItem DOCTOR_ITEM = new ContractItem(Blocks.GOLD_BLOCK,"doctor");
	public static final TestItem TEST_ITEM = new TestItem(new Item.Settings());
	private void registerItems()
	{
		Registry.register(Registries.ITEM,new Identifier("pirates","cannonball"),CANNONBALL);
		Registry.register(Registries.ITEM,new Identifier("pirates","fire_cannonball"),FIRE_CANNONBALL);
		Registry.register(Registries.ITEM,new Identifier("pirates","weighted_cannonball"),WEIGHTED_CANNONBALL);
		Registry.register(Registries.ITEM,new Identifier("util_pirates","util_1"),CANNONBALL_ENT);
		Registry.register(Registries.ITEM,new Identifier("pirates","ship_pointer"),SHIP_POINTER);
		Registry.register(Registries.ITEM,new Identifier("pirates","cannoneer"),CANNONEER_ITEM);
		Registry.register(Registries.ITEM,new Identifier("pirates","doctor"),DOCTOR_ITEM);
		Registry.register(Registries.ITEM,new Identifier("pirates","ship_pather"),SHIP_PATHER);
		Registry.register(Registries.ITEM,new Identifier("pirates","stable_block"),new BlockItem(STABLE_BLOCK,new Item.Settings()));

		Registry.register(Registries.ITEM,new Identifier("pirates","cannon_priming_block"),new BlockItem(CANNON_PRIMING_BLOCK,new Item.Settings()));

		Registry.register(Registries.ITEM,new Identifier("pirates","test"),TEST_ITEM);

		Registry.register(Registries.ITEM,new Identifier("pirates","motion_invoking_block"),new BlockItem(MOTION_INVOKING_BLOCK,new Item.Settings()));
		Registry.register(Registries.ITEM,new Identifier("pirates","crew_spawner_block"),new BlockItem(CREW_SPAWNER_BLOCK,new Item.Settings()));
		Registry.register(Registries.ITEM,new Identifier("pirates","ship_id_block"),new BlockItem(SHIP_ID_BLOCK,new Item.Settings()));

		// my custom blocks
		Registry.register(Registries.ITEM, new Identifier("pirates", "damaged_hull_block"),
				new BlockItem(DAMAGED_HULL_BLOCK, new Item.Settings()));
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
	public static final BlockEntityType<ShipIdBlockEntity> SHIP_ID_BLOCK_ENTITY = Registry.register(
			Registries.BLOCK_ENTITY_TYPE,
			new Identifier("pirates", "ship_id_block_entity"),
			FabricBlockEntityTypeBuilder.create(ShipIdBlockEntity::new, SHIP_ID_BLOCK).build()
	);


	//entities
	public static final EntityType<ShotEntity> SHOT_ENTITY_TYPE =registerEntity("shot",SpawnGroup.MISC,EntityDimensions.changing(0.5f,0.5f),((type, world) -> new ShotEntity(world)));

	public static final EntityType<PirateEntity> PIRATE_ENTITY_TYPE =registerEntity("pirate",SpawnGroup.MISC,EntityDimensions.changing(0.6f,1.9f),((type, world) -> new PirateEntity(world)));

	public static final EntityType<FriendlyPirateEntity> FRIENDLY_PIRATE_TYPE =registerEntity("friendly_pirate",SpawnGroup.MISC,EntityDimensions.changing(0.6f,1.9f),((type, world) -> new FriendlyPirateEntity(world)));


	public static final EntityType<SkeletonPirateEntity> SKELETON_PIRATE_ENTITY_TYPE = null; //=registerEntity("skeleton_pirate",SpawnGroup.MISC,EntityDimensions.changing(0.6f,1.9f),((type, world) -> new SkeletonPirateEntity(world)));



	public static <T extends Entity> EntityType<T> registerEntity(String name, SpawnGroup category, EntityDimensions size, EntityType.EntityFactory<T> factory) {
		return Registry.register(Registries.ENTITY_TYPE, new Identifier("pirates", name), FabricEntityTypeBuilder.create(category, factory).dimensions(size).build());

	}

	public static class CompatTracker {
		public boolean eureka = false;
		public boolean sails = false;

	}
}