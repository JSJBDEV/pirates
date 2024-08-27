package ace.actually.pirates;

import ace.actually.pirates.blocks.*;
import ace.actually.pirates.entities.ShotEntity;
import ace.actually.pirates.entities.pirate.PirateEntity;
import ace.actually.pirates.items.RaycastingItem;
import ace.actually.pirates.items.TestingStickItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.dispenser.ProjectileDispenserBehavior;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
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
		registerDispenserThings();
		PatternProcessor.setupBasicPatterns();
		//ConfigUtils.checkConfigs();
		LOGGER.info("Let there be motion!");

		//BiomeModifications.addSpawn(BiomeSelectors.categories(Biome.Category.OCEAN), SpawnGroup.WATER_CREATURE, Pirates.SHIP, 3, 1, 1);


	}

	private void registerEntityThings()
	{

		FabricDefaultAttributeRegistry.register(PIRATE_ENTITY_TYPE, PirateEntity.attributes());
	}


	public static final MotionInvokingBlock MOTION_INVOKING_BLOCK = new MotionInvokingBlock(AbstractBlock.Settings.copy(Blocks.CRYING_OBSIDIAN).noBlockBreakParticles().noCollision().sounds(BlockSoundGroup.AMETHYST_BLOCK));
	public static final CannonPrimingBlock CANNON_PRIMING_BLOCK = new CannonPrimingBlock(AbstractBlock.Settings.copy(Blocks.DISPENSER).hardness(5));
	public static final CaptainHeadBlock CAPTAIN_HEAD_BLOCK = new CaptainHeadBlock(AbstractBlock.Settings.copy(Blocks.STONE));
	private void registerBlocks()
	{
		Registry.register(Registries.BLOCK,new Identifier("pirates","cannon_priming_block"),CANNON_PRIMING_BLOCK);
		Registry.register(Registries.BLOCK,new Identifier("pirates","motion_invoking_block"),MOTION_INVOKING_BLOCK);
		Registry.register(Registries.BLOCK,new Identifier("pirates","captain_head_block"),CAPTAIN_HEAD_BLOCK);

	}


	public static final TestingStickItem TESTING_STICK_ITEM = new TestingStickItem(new Item.Settings());
	public static final RaycastingItem RAYCASTING_ITEM = new RaycastingItem(new Item.Settings());
	public static final Item CANNONBALL = new Item(new Item.Settings().fireproof());
	private void registerItems()
	{
		Registry.register(Registries.ITEM,new Identifier("pirates","testing_stick"),TESTING_STICK_ITEM);
		Registry.register(Registries.ITEM,new Identifier("pirates","raycaster"),RAYCASTING_ITEM);
		Registry.register(Registries.ITEM,new Identifier("pirates","cannonball"),CANNONBALL);

		Registry.register(Registries.ITEM,new Identifier("pirates","cannon_priming_block"),new BlockItem(CANNON_PRIMING_BLOCK,new Item.Settings()));
	}


	private void registerDispenserThings()
	{
		DispenserBlock.registerBehavior(Pirates.CANNONBALL, new DispenserBehavior() {
			public ItemStack dispense(BlockPointer blockPointer, ItemStack itemStack) {
				return (new ProjectileDispenserBehavior() {
					protected ProjectileEntity createProjectile(World world, Position position, ItemStack stack) {
						ShotEntity qentity = Util.make(new ShotEntity(Pirates.SHOT_ENTITY_TYPE,world,null,Pirates.CANNONBALL,2,""), (entity) -> {
							entity.setItem(stack);
						});
						qentity.setPosition(new Vec3d(position.getX(),position.getY(),position.getZ()));
						return qentity;
					}

					protected float getVariation() {
						return super.getVariation() * 0.5F;
					}

					protected float getForce() {
						return super.getForce() * 1.25F;
					}
				}).dispense(blockPointer, itemStack);
			}
		});
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


	//entities
	public static final EntityType<ShotEntity> SHOT_ENTITY_TYPE =registerEntity("shot",SpawnGroup.MISC,EntityDimensions.changing(0.5f,0.5f),((type, world) -> new ShotEntity(world)));

	public static final EntityType<PirateEntity> PIRATE_ENTITY_TYPE =registerEntity("pirate",SpawnGroup.MISC,EntityDimensions.changing(0.6f,1.7f),((type, world) -> new PirateEntity(world)));


	public static <T extends Entity> EntityType<T> registerEntity(String name, SpawnGroup category, EntityDimensions size, EntityType.EntityFactory<T> factory) {
		return Registry.register(Registries.ENTITY_TYPE, new Identifier("pirates", name), FabricEntityTypeBuilder.create(category, factory).dimensions(size).build());
	}


}