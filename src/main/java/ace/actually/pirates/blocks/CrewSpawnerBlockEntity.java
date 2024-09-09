package ace.actually.pirates.blocks;

import ace.actually.pirates.PatternProcessor;
import ace.actually.pirates.Pirates;
import ace.actually.pirates.entities.pirate.PirateEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.eureka.block.ShipHelmBlock;
import org.valkyrienskies.eureka.util.ShipAssembler;
import org.valkyrienskies.mod.api.SeatedControllingPlayer;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.util.DimensionIdProvider;

import static net.minecraft.state.property.Properties.HORIZONTAL_FACING;

public class CrewSpawnerBlockEntity extends BlockEntity {

    public CrewSpawnerBlockEntity(BlockPos pos, BlockState state) {
        super(Pirates.CREW_SPAWNER_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, CrewSpawnerBlockEntity be) {

        if (!world.isClient() && VSGameUtilsKt.isBlockInShipyard(world, pos)) {
            Ship ship = VSGameUtilsKt.getShipManagingPos(world, pos);

            if (ship == null) return;

            long shipID = ship.getId();

            Entity pirate = new PirateEntity(world, shipID);
            pirate.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
            pirate.setPosition(pos.toCenterPos());
            world.spawnEntity(pirate);

            world.breakBlock(pos, false);
        }
    }

}
