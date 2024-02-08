package ace.actually.pirates.items;

import ace.actually.pirates.Pirates;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.core.api.world.ServerShipWorld;
import org.valkyrienskies.mod.api.SeatedControllingPlayer;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.util.DimensionIdProvider;

public class TestingStickItem extends Item {
    public TestingStickItem(Settings settings) {
        super(settings);
    }


    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        ServerShipWorld serverShipWorld = (ServerShipWorld) ValkyrienSkiesMod.getVsCore().getHooks().getCurrentShipServerWorld();
        ChunkPos pos = context.getWorld().getChunk(context.getBlockPos()).getPos();
        DimensionIdProvider provider = (DimensionIdProvider) context.getWorld();
        LoadedServerShip ship = serverShipWorld.getLoadedShips().getByChunkPos(pos.x,pos.z, provider.getDimensionId());

        SeatedControllingPlayer seatedControllingPlayer = ship.getAttachment(SeatedControllingPlayer.class);
        if(seatedControllingPlayer==null)
        {
            seatedControllingPlayer = new SeatedControllingPlayer(Direction.NORTH);
            ship.setAttachment(SeatedControllingPlayer.class,seatedControllingPlayer);
        }

        seatedControllingPlayer.setForwardImpulse(1);
        seatedControllingPlayer.setLeftImpulse(-0.1f);



        Vector3d a = ship.getTransform().getShipToWorldRotation().getEulerAnglesXYZ(new Vector3d());

        Pirates.LOGGER.info(Math.toDegrees(a.x)+" "+Math.toDegrees(a.y)+" "+Math.toDegrees(a.z));

        return super.useOnBlock(context);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if(!world.isClient && hand==Hand.MAIN_HAND)
        {
            Pirates.isLiveWorld = user.isSneaking();
        }
        return super.use(world, user, hand);
    }
}
