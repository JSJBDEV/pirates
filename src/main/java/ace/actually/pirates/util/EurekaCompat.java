package ace.actually.pirates.util;

import ace.actually.pirates.blocks.entity.MotionInvokingBlockEntity;
import com.quintonc.vs_sails.registration.SailsBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.eureka.EurekaBlocks;
import org.valkyrienskies.eureka.block.ShipHelmBlock;
import org.valkyrienskies.eureka.ship.EurekaShipControl;
import org.valkyrienskies.mod.api.SeatedControllingPlayer;

public class EurekaCompat {
    private static int flipflop = 1;
    public static void moveTowards(MotionInvokingBlockEntity be, SeatedControllingPlayer power, LoadedServerShip ship)
    {
        if(power==null) return;
        if(be.getTarget().length!=3) return;
        if(be.getTarget()[0]==0 && be.getTarget()[1]==0 && be.getTarget()[2]==0)
        {
            power.setForwardImpulse(1);
            power.setLeftImpulse(1);
            return;
        }


        power.setForwardImpulse(1);
        Vector3dc v3d = ship.getTransform().getPositionInWorld();

        EurekaShipControl shipControl  = ship.getAttachment(EurekaShipControl.class);
        if(shipControl!=null && shipControl.getBalloons()>0)
        {
            if(be.getTarget()[1]>v3d.y()-1)
            {
                power.setUpImpulse(1);
            }
            else if(be.getTarget()[1]<v3d.y()+1)
            {
                power.setUpImpulse(-1);
            }
            else
            {
                power.setUpImpulse(0);
            }
        }


        if(be.getLdx()==-1)
        {
            //lastDistance = v3d.distanceSquared(target[0],target[1],target[2]);
            be.setLdx(vdis(be.getTarget()[0],v3d.x()));
            be.setLdz(vdis(be.getTarget()[2],v3d.z()));
        }
        else
        {
            //double currentDistance = v3d.distanceSquared(target[0],target[1],target[2]);
            double cdx = vdis(be.getTarget()[0],v3d.x());
            double cdz = vdis(be.getTarget()[2],v3d.z());
            //System.out.println(lastDistance+" -> "+currentDistance);
            if(cdx>=be.getLdx() || cdz>= be.getLdz())
            {
                power.setLeftImpulse(flipflop);

            }
            else
            {
                power.setLeftImpulse(0);
                flipflop = -flipflop;
            }
            be.setLdx(cdx);
            be.setLdz(cdz);
        }

    }

    public static void stopMotion(LoadedServerShip ship)
    {
        SeatedControllingPlayer seatedControllingPlayer = ship.getAttachment(SeatedControllingPlayer.class);
        if (seatedControllingPlayer == null) return;
        seatedControllingPlayer.setLeftImpulse(0);
        seatedControllingPlayer.setForwardImpulse(0);
        seatedControllingPlayer.setCruise(false);
        seatedControllingPlayer.setUpImpulse(0);
        ship.setAttachment(SeatedControllingPlayer.class, seatedControllingPlayer);
    }

    private static double vdis(double x, double xto) {
        return Math.abs(x-xto);
    }

    public static boolean checkHelm(Level world, BlockPos pos) {
        return world.getBlockState(pos.above()).getBlock() instanceof ShipHelmBlock;
    }
}
