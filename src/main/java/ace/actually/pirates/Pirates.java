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
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
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

	public static final GameRules.Key<GameRules.BooleanValue> PIRATES_IS_LIVE_WORLD =
			GameRuleRegistry.register("piratesIsLive", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(true));

	public static final ResourceKey<CreativeModeTab> PIRATES_ITEM_GROUP_KEY = ResourceKey.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), ResourceLocation.tryBuild(MOD_ID, "item_group"));
	public static final CreativeModeTab PIRATES_ITEM_GROUP = FabricItemGroup.builder()
			.icon(() -> new ItemStack(Pirates.CANNONBALL))
			.title(Component.nullToEmpty("Valkyrien Pirates"))
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
						new ResourceLocation("sails_ships"),
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
						new ResourceLocation("eureka_ships"),
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
						new ResourceLocation("flying_ships"),
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
		cannonRange = Float.parseFloat(ConfigUtils.config.getOrDefault("cannon-range","1.7"));
		pursuitDistance = Integer.parseInt(ConfigUtils.config.getOrDefault("pursuit-distance","10000"));
		shouldEnableFlyingPirates = ConfigUtils.config.getOrDefault("should-enable-flying-pirates","false").equals("true");

		String[] rc = ConfigUtils.config.getOrDefault("recruit-cost","minecraft:golden_apple,1").split(",");
		recruitCost = () -> new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(rc[0])),Integer.parseInt(rc[1]));

		registerEntityThings();
		//entity types do it themselves
		registerBlocks();
		registerItems();
		//block entities do it themselves
		//registerDispenserThings();
		ModSounds.registerSounds();
		LOGGER.info("Let there be motion!");

		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, PIRATES_ITEM_GROUP_KEY, PIRATES_ITEM_GROUP);

		ItemGroupEvents.modifyEntriesEvent(PIRATES_ITEM_GROUP_KEY).register(itemGroup -> {
			itemGroup.accept(Pirates.CANNONBALL);
			itemGroup.accept(Pirates.FIRE_CANNONBALL);
			itemGroup.accept(Pirates.WEIGHTED_CANNONBALL);
			itemGroup.accept(Pirates.SHIP_ID_BLOCK);
			itemGroup.accept(Pirates.CANNON_PRIMING_BLOCK.asItem());
			itemGroup.accept(Pirates.CREW_SPAWNER_BLOCK.asItem());
			itemGroup.accept(Pirates.MOTION_INVOKING_BLOCK.asItem());
			itemGroup.accept(Pirates.SHIP_POINTER);
			itemGroup.accept(Pirates.SHIP_PATHER);
			itemGroup.accept(Pirates.CANNONEER_ITEM);
			itemGroup.accept(Pirates.DOCTOR_ITEM);
		});

		IPirateDies.EVENT.register((player, pirate) ->
		{
			//System.out.println(pirate.getUuidAsString());
			return InteractionResult.PASS;
		});


	}


	private void registerEntityThings()
	{

		FabricDefaultAttributeRegistry.register(PIRATE_ENTITY_TYPE, PirateEntity.attributes());
		FabricDefaultAttributeRegistry.register(FRIENDLY_PIRATE_TYPE, FriendlyPirateEntity.attributes());
		//FabricDefaultAttributeRegistry.register(SKELETON_PIRATE_ENTITY_TYPE, SkeletonPirateEntity.attributes());

	}

	private static final SoundType Silent = new SoundType(0, 0, SoundEvents.COD_AMBIENT, SoundEvents.COD_AMBIENT, SoundEvents.COD_AMBIENT, SoundEvents.COD_AMBIENT, SoundEvents.COD_AMBIENT);

	public static final MotionInvokingBlock MOTION_INVOKING_BLOCK = new MotionInvokingBlock(BlockBehaviour.Properties.copy(Blocks.BIRCH_WOOD).noParticlesOnBreak().destroyTime(7).noLootTable());
	public static final CannonPrimingBlock CANNON_PRIMING_BLOCK = new CannonPrimingBlock(BlockBehaviour.Properties.copy(Blocks.DISPENSER).destroyTime(5));
	public static final DispenserCannonBlock DISPENSER_CANNON_BLOCK = new DispenserCannonBlock(BlockBehaviour.Properties.copy(Blocks.DISPENSER).destroyTime(5));
	public static final CrewSpawnerBlock CREW_SPAWNER_BLOCK = new CrewSpawnerBlock(BlockBehaviour.Properties.copy(Blocks.BIRCH_WOOD).noParticlesOnBreak().noCollission().noLootTable().sound(Silent));
	public static final StableBlock STABLE_BLOCK = new StableBlock(BlockBehaviour.Properties.of());
	public static final ShipIdBlock SHIP_ID_BLOCK = new ShipIdBlock(BlockBehaviour.Properties.of());
	public static final Block HEAVY_BLOCK = new Block(BlockBehaviour.Properties.copy(Blocks.OBSIDIAN));
	private void registerBlocks()
	{
		Registry.register(BuiltInRegistries.BLOCK,new ResourceLocation("pirates","cannon_priming_block"),CANNON_PRIMING_BLOCK);
		Registry.register(BuiltInRegistries.BLOCK,new ResourceLocation("pirates","motion_invoking_block"),MOTION_INVOKING_BLOCK);
		Registry.register(BuiltInRegistries.BLOCK,new ResourceLocation("pirates","dispenser_cannon_block"),DISPENSER_CANNON_BLOCK);
		Registry.register(BuiltInRegistries.BLOCK,new ResourceLocation("pirates","crew_spawner_block"),CREW_SPAWNER_BLOCK);
		Registry.register(BuiltInRegistries.BLOCK,new ResourceLocation("pirates","stable_block"),STABLE_BLOCK);
		Registry.register(BuiltInRegistries.BLOCK,new ResourceLocation("pirates","ship_id_block"),SHIP_ID_BLOCK);
		Registry.register(BuiltInRegistries.BLOCK,new ResourceLocation("pirates","heavy_block"),HEAVY_BLOCK);

	}



	public static final Item CANNONBALL = new Item(new Item.Properties());
	public static final Item FIRE_CANNONBALL = new Item(new Item.Properties());
	public static final Item WEIGHTED_CANNONBALL = new Item(new Item.Properties());
	public static final Item CANNONBALL_ENT = new Item(new Item.Properties());
	public static final ShipPointer SHIP_POINTER = new ShipPointer(new Item.Properties());
	public static final ShipPather SHIP_PATHER = new ShipPather(new Item.Properties());
	public static final ContractItem CANNONEER_ITEM = new ContractItem(CANNON_PRIMING_BLOCK,"cannoneer");
	public static final ContractItem DOCTOR_ITEM = new ContractItem(Blocks.GOLD_BLOCK,"doctor");
	public static final TestItem TEST_ITEM = new TestItem(new Item.Properties());
	private void registerItems()
	{
		Registry.register(BuiltInRegistries.ITEM,new ResourceLocation("pirates","cannonball"),CANNONBALL);
		Registry.register(BuiltInRegistries.ITEM,new ResourceLocation("pirates","fire_cannonball"),FIRE_CANNONBALL);
		Registry.register(BuiltInRegistries.ITEM,new ResourceLocation("pirates","weighted_cannonball"),WEIGHTED_CANNONBALL);
		Registry.register(BuiltInRegistries.ITEM,new ResourceLocation("util_pirates","util_1"),CANNONBALL_ENT);
		Registry.register(BuiltInRegistries.ITEM,new ResourceLocation("pirates","ship_pointer"),SHIP_POINTER);
		Registry.register(BuiltInRegistries.ITEM,new ResourceLocation("pirates","cannoneer"),CANNONEER_ITEM);
		Registry.register(BuiltInRegistries.ITEM,new ResourceLocation("pirates","doctor"),DOCTOR_ITEM);
		Registry.register(BuiltInRegistries.ITEM,new ResourceLocation("pirates","ship_pather"),SHIP_PATHER);
		Registry.register(BuiltInRegistries.ITEM,new ResourceLocation("pirates","stable_block"),new BlockItem(STABLE_BLOCK,new Item.Properties()));

		Registry.register(BuiltInRegistries.ITEM,new ResourceLocation("pirates","cannon_priming_block"),new BlockItem(CANNON_PRIMING_BLOCK,new Item.Properties()));

		Registry.register(BuiltInRegistries.ITEM,new ResourceLocation("pirates","test"),TEST_ITEM);

		Registry.register(BuiltInRegistries.ITEM,new ResourceLocation("pirates","motion_invoking_block"),new BlockItem(MOTION_INVOKING_BLOCK,new Item.Properties()));
		Registry.register(BuiltInRegistries.ITEM,new ResourceLocation("pirates","crew_spawner_block"),new BlockItem(CREW_SPAWNER_BLOCK,new Item.Properties()));
		Registry.register(BuiltInRegistries.ITEM,new ResourceLocation("pirates","ship_id_block"),new BlockItem(SHIP_ID_BLOCK,new Item.Properties()));

	}


	//block entities
	public static final BlockEntityType<MotionInvokingBlockEntity> MOTION_INVOKING_BLOCK_ENTITY = Registry.register(
			BuiltInRegistries.BLOCK_ENTITY_TYPE,
			new ResourceLocation("pirates", "motion_invoking_block_entity"),
			FabricBlockEntityTypeBuilder.create(MotionInvokingBlockEntity::new, MOTION_INVOKING_BLOCK).build()
	);
	public static final BlockEntityType<CannonPrimingBlockEntity> CANNON_PRIMING_BLOCK_ENTITY = Registry.register(
			BuiltInRegistries.BLOCK_ENTITY_TYPE,
			new ResourceLocation("pirates", "cannon_priming_block_entity"),
			FabricBlockEntityTypeBuilder.create(CannonPrimingBlockEntity::new, CANNON_PRIMING_BLOCK).build()
	);
	public static final BlockEntityType<CrewSpawnerBlockEntity> CREW_SPAWNER_BLOCK_ENTITY = Registry.register(
			BuiltInRegistries.BLOCK_ENTITY_TYPE,
			new ResourceLocation("pirates", "crew_spawner_block_entity"),
			FabricBlockEntityTypeBuilder.create(CrewSpawnerBlockEntity::new, CREW_SPAWNER_BLOCK).build()
	);
	public static final BlockEntityType<StableBlockEntity> STABLE_BLOCK_ENTITY = Registry.register(
			BuiltInRegistries.BLOCK_ENTITY_TYPE,
			new ResourceLocation("pirates", "stable_block_entity"),
			FabricBlockEntityTypeBuilder.create(StableBlockEntity::new, STABLE_BLOCK).build()
	);
	public static final BlockEntityType<ShipIdBlockEntity> SHIP_ID_BLOCK_ENTITY = Registry.register(
			BuiltInRegistries.BLOCK_ENTITY_TYPE,
			new ResourceLocation("pirates", "ship_id_block_entity"),
			FabricBlockEntityTypeBuilder.create(ShipIdBlockEntity::new, SHIP_ID_BLOCK).build()
	);


	//entities
	public static final EntityType<ShotEntity> SHOT_ENTITY_TYPE =registerEntity("shot",MobCategory.MISC,EntityDimensions.scalable(0.5f,0.5f),((type, world) -> new ShotEntity(world)));

	public static final EntityType<PirateEntity> PIRATE_ENTITY_TYPE =registerEntity("pirate",MobCategory.MISC,EntityDimensions.scalable(0.6f,1.9f),((type, world) -> new PirateEntity(world)));

	public static final EntityType<FriendlyPirateEntity> FRIENDLY_PIRATE_TYPE =registerEntity("friendly_pirate",MobCategory.MISC,EntityDimensions.scalable(0.6f,1.9f),((type, world) -> new FriendlyPirateEntity(world)));


	public static final EntityType<SkeletonPirateEntity> SKELETON_PIRATE_ENTITY_TYPE = null; //=registerEntity("skeleton_pirate",SpawnGroup.MISC,EntityDimensions.changing(0.6f,1.9f),((type, world) -> new SkeletonPirateEntity(world)));



	public static <T extends Entity> EntityType<T> registerEntity(String name, MobCategory category, EntityDimensions size, EntityType.EntityFactory<T> factory) {
		return Registry.register(BuiltInRegistries.ENTITY_TYPE, new ResourceLocation("pirates", name), FabricEntityTypeBuilder.create(category, factory).dimensions(size).build());

	}

	public static class CompatTracker {
		public boolean eureka = false;
		public boolean sails = false;

	}
}