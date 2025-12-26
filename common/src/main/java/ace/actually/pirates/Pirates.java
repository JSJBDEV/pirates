package ace.actually.pirates;

import ace.actually.pirates.blocks.*;
import ace.actually.pirates.blocks.entity.*;
import ace.actually.pirates.client.PiratesClient;
import ace.actually.pirates.entities.friendly_pirate.FriendlyPirateEntity;
import ace.actually.pirates.entities.pirate_default.PirateEntity;
import ace.actually.pirates.entities.pirate_skeleton.SkeletonPirateEntity;
import ace.actually.pirates.entities.shot.ShotEntity;
import ace.actually.pirates.items.ContractItem;
import ace.actually.pirates.items.ShipPather;
import ace.actually.pirates.items.ShipPointer;
import ace.actually.pirates.items.TestItem;
import ace.actually.pirates.util.ConfigUtils;
import com.google.common.base.Suppliers;
import dev.architectury.platform.Platform;
import dev.architectury.registry.level.entity.EntityAttributeRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrarManager;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
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

import java.util.function.Supplier;

public class Pirates {

    public static final String MOD_ID = "pirates";

    public static final Supplier<RegistrarManager> MANAGER = Suppliers.memoize(() -> RegistrarManager.get(MOD_ID));


    public static final Logger LOGGER = LoggerFactory.getLogger("pirates");

    public static final GameRules.Key<GameRules.BooleanValue> PIRATES_IS_LIVE_WORLD =
            GameRules.register("piratesIsLive", GameRules.Category.MISC, GameRules.BooleanValue.create(true));

    public static final ResourceKey<CreativeModeTab> PIRATES_ITEM_GROUP_KEY = ResourceKey.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), ResourceLocation.tryBuild(MOD_ID, "item_group"));

    public static float baseShotPower;
    public static float cannonRange;
    public static int pursuitDistance;
    public static boolean shouldEnableFlyingPirates;
    public static Supplier<ItemStack> recruitCost;
    public static CompatTracker loadedCompats = new CompatTracker();
    private static final SoundType Silent = new SoundType(0, 0, SoundEvents.COD_AMBIENT, SoundEvents.COD_AMBIENT, SoundEvents.COD_AMBIENT, SoundEvents.COD_AMBIENT, SoundEvents.COD_AMBIENT);



    public static void init()
    {

        if (Platform.isModLoaded("vs_sails")) {
            loadedCompats.sails = true;

        }
        if (Platform.isModLoaded("vs_eureka")) {
            loadedCompats.eureka = true;
        }


        ConfigUtils.checkConfigs();
        baseShotPower = Float.parseFloat(ConfigUtils.config.getOrDefault("base-shot-power","2.2"));
        cannonRange = Float.parseFloat(ConfigUtils.config.getOrDefault("cannon-range","1.7"));
        pursuitDistance = Integer.parseInt(ConfigUtils.config.getOrDefault("pursuit-distance","10000"));
        shouldEnableFlyingPirates = ConfigUtils.config.getOrDefault("should-enable-flying-pirates","false").equals("true");

        String[] rc = ConfigUtils.config.getOrDefault("recruit-cost","minecraft:golden_apple,1").split(",");
        recruitCost = () -> new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(rc[0])),Integer.parseInt(rc[1]));

        registerBlocks();
        registerBlockEntities();
        registerItems();
        registerEntities();

        //thank you https://github.com/dayofpi/mob-catalog/blob/master/common/src/main/java/com/dayofpi/mobcatalog/MobCatalog.java
        //it works without this on fabric by default...
        EnvExecutor.runInEnv(Env.CLIENT, () -> PiratesClient::initClientFromMain);
    }


    public static RegistrySupplier<MotionInvokingBlock> MOTION_INVOKING_BLOCK;
    public static RegistrySupplier<CannonPrimingBlock> CANNON_PRIMING_BLOCK;
    public static RegistrySupplier<DispenserCannonBlock> DISPENSER_CANNON_BLOCK;
    public static RegistrySupplier<CrewSpawnerBlock> CREW_SPAWNER_BLOCK;
    public static RegistrySupplier<StableBlock> STABLE_BLOCK;
    public static RegistrySupplier<ShipIdBlock> SHIP_ID_BLOCK;
    public static RegistrySupplier<Block> HEAVY_BLOCK;
    private static void registerBlocks()
    {
        Registrar<Block> blocks = MANAGER.get().get(Registries.BLOCK);

        CANNON_PRIMING_BLOCK = blocks.register(new ResourceLocation("pirates","cannon_priming_block"), ()->new CannonPrimingBlock(BlockBehaviour.Properties.copy(Blocks.DISPENSER).destroyTime(5)));
        MOTION_INVOKING_BLOCK = blocks.register(new ResourceLocation("pirates","motion_invoking_block"), ()-> new MotionInvokingBlock(BlockBehaviour.Properties.copy(Blocks.BIRCH_WOOD).noParticlesOnBreak().destroyTime(7).noLootTable()));
        DISPENSER_CANNON_BLOCK = blocks.register(new ResourceLocation("pirates","dispenser_cannon_block"), ()->new DispenserCannonBlock(BlockBehaviour.Properties.copy(Blocks.DISPENSER).destroyTime(5)));
        CREW_SPAWNER_BLOCK = blocks.register(new ResourceLocation("pirates","crew_spawner_block"), ()->new CrewSpawnerBlock(BlockBehaviour.Properties.copy(Blocks.BIRCH_WOOD).noParticlesOnBreak().noCollission().noLootTable().sound(Silent)));
        STABLE_BLOCK = blocks.register(new ResourceLocation("pirates","stable_block"), ()->new StableBlock(BlockBehaviour.Properties.of()));
        SHIP_ID_BLOCK = blocks.register(new ResourceLocation("pirates","ship_id_block"), ()->new ShipIdBlock(BlockBehaviour.Properties.of()));
        HEAVY_BLOCK = blocks.register(new ResourceLocation("pirates","heavy_block"), ()->new Block(BlockBehaviour.Properties.copy(Blocks.OBSIDIAN)));

    }


    public static RegistrySupplier<BlockEntityType<MotionInvokingBlockEntity>> MOTION_INVOKING_BLOCK_ENTITY;
    public static RegistrySupplier<BlockEntityType<CannonPrimingBlockEntity>> CANNON_PRIMING_BLOCK_ENTITY;
    public static RegistrySupplier<BlockEntityType<CrewSpawnerBlockEntity>> CREW_SPAWNER_BLOCK_ENTITY;
    public static RegistrySupplier<BlockEntityType<StableBlockEntity>> STABLE_BLOCK_ENTITY;
    public static RegistrySupplier<BlockEntityType<ShipIdBlockEntity>> SHIP_ID_BLOCK_ENTITY;

    private static void registerBlockEntities()
    {
        Registrar<BlockEntityType<?>> bes = MANAGER.get().get(Registries.BLOCK_ENTITY_TYPE);
        MOTION_INVOKING_BLOCK_ENTITY = bes.register(new ResourceLocation(MOD_ID, "motion_invoking_block_entity"),()->BlockEntityType.Builder.of(MotionInvokingBlockEntity::new,MOTION_INVOKING_BLOCK.get()).build(null));
        CANNON_PRIMING_BLOCK_ENTITY = bes.register(new ResourceLocation(MOD_ID, "cannon_priming_block_entity"),()->BlockEntityType.Builder.of(CannonPrimingBlockEntity::new,CANNON_PRIMING_BLOCK.get()).build(null));
        CREW_SPAWNER_BLOCK_ENTITY = bes.register(new ResourceLocation(MOD_ID, "crew_spawner_block_entity"),()->BlockEntityType.Builder.of(CrewSpawnerBlockEntity::new,CREW_SPAWNER_BLOCK.get()).build(null));
        STABLE_BLOCK_ENTITY = bes.register(new ResourceLocation(MOD_ID, "stable_block_entity"),()->BlockEntityType.Builder.of(StableBlockEntity::new,STABLE_BLOCK.get()).build(null));
        SHIP_ID_BLOCK_ENTITY = bes.register(new ResourceLocation(MOD_ID, "ship_id_block_entity"),()->BlockEntityType.Builder.of(ShipIdBlockEntity::new,SHIP_ID_BLOCK.get()).build(null));

    }



    public static RegistrySupplier<Item> CANNONBALL;
    public static RegistrySupplier<Item> FIRE_CANNONBALL;
    public static RegistrySupplier<Item> WEIGHTED_CANNONBALL;
    public static RegistrySupplier<Item> CANNONBALL_ENT;
    public static RegistrySupplier<ShipPointer> SHIP_POINTER;
    public static RegistrySupplier<ShipPather> SHIP_PATHER;
    public static RegistrySupplier<ContractItem> CANNONEER_ITEM;
    public static RegistrySupplier<ContractItem> DOCTOR_ITEM;
    public static RegistrySupplier<TestItem> TEST_ITEM;
    private static void registerItems()
    {
        Registrar<Item> items = MANAGER.get().get(Registries.ITEM);

        CANNONBALL = items.register(new ResourceLocation("pirates","cannonball"),()->new Item(new Item.Properties()));
        FIRE_CANNONBALL = items.register(new ResourceLocation("pirates","fire_cannonball"),()->new Item(new Item.Properties()));
        WEIGHTED_CANNONBALL = items.register(new ResourceLocation("pirates","weighted_cannonball"),()->new Item(new Item.Properties()));
        CANNONBALL_ENT = items.register(new ResourceLocation("util_pirates","util_1"),()->new Item(new Item.Properties()));
        SHIP_POINTER = items.register(new ResourceLocation("pirates","ship_pointer"),()->new ShipPointer(new Item.Properties()));
        CANNONEER_ITEM = items.register(new ResourceLocation("pirates","cannoneer"),()->new ContractItem(CANNON_PRIMING_BLOCK.get(),"cannoneer"));
        DOCTOR_ITEM = items.register(new ResourceLocation("pirates","doctor"),()-> new ContractItem(Blocks.GOLD_BLOCK,"doctor"));
        SHIP_PATHER = items.register(new ResourceLocation("pirates","ship_pather"),()-> new ShipPather(new Item.Properties()));

        items.register(new ResourceLocation("pirates","stable_block"),()->new BlockItem(STABLE_BLOCK.get(),new Item.Properties()));

        items.register(new ResourceLocation("pirates","cannon_priming_block"),()->new BlockItem(CANNON_PRIMING_BLOCK.get(),new Item.Properties()));

        TEST_ITEM = items.register(new ResourceLocation("pirates","test"),()->new TestItem(new Item.Properties()));

        items.register(new ResourceLocation("pirates","motion_invoking_block"),()->new BlockItem(MOTION_INVOKING_BLOCK.get(),new Item.Properties()));
        items.register(new ResourceLocation("pirates","crew_spawner_block"),()->new BlockItem(CREW_SPAWNER_BLOCK.get(),new Item.Properties()));
        items.register(new ResourceLocation("pirates","ship_id_block"),()->new BlockItem(SHIP_ID_BLOCK.get(),new Item.Properties()));


    }

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(MOD_ID, Registries.ENTITY_TYPE);

    public static final RegistrySupplier<EntityType<ShotEntity>> SHOT_ENTITY_TYPE = ENTITY_TYPES.register("shot",()->EntityType.Builder.of((EntityType.EntityFactory<ShotEntity>) ShotEntity::new,MobCategory.MISC).sized(0.5f,0.5f).build("shot"));

    public static final RegistrySupplier<EntityType<PirateEntity>> PIRATE_ENTITY_TYPE = ENTITY_TYPES.register("pirate",()->EntityType.Builder.of((EntityType.EntityFactory<PirateEntity>) PirateEntity::new, MobCategory.MISC).sized(0.6f,1.9f).build("pirate"));

    public static final RegistrySupplier<EntityType<FriendlyPirateEntity>> FRIENDLY_PIRATE_TYPE = ENTITY_TYPES.register("friendly_pirate",()->EntityType.Builder.of((EntityType.EntityFactory<FriendlyPirateEntity>) FriendlyPirateEntity::new,MobCategory.MISC).sized(0.6f,1.9f).build("friendly_pirate"));

    public static RegistrySupplier<EntityType<SkeletonPirateEntity>> SKELETON_PIRATE_ENTITY_TYPE = null; //=registerEntity("skeleton_pirate",SpawnGroup.MISC,EntityDimensions.changing(0.6f,1.9f),((type, world) -> new SkeletonPirateEntity(world)));

    private static void registerEntities()
    {
        ENTITY_TYPES.register();
        EntityAttributeRegistry.register(PIRATE_ENTITY_TYPE, PirateEntity::attributes);
        EntityAttributeRegistry.register(FRIENDLY_PIRATE_TYPE, FriendlyPirateEntity::attributes);
    }




    public static class CompatTracker {
        public boolean eureka = false;
        public boolean sails = false;

    }
}
