package ace.actually.pirates.shipai;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import org.valkyrienskies.core.api.ships.ServerShip;

@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE
)
public class PirateShipEvents {
    boolean canCannonFire;
    public PirateShipEvents()
    {
        canCannonFire = false;
    }
    public PirateShipEvents(boolean canCannonFire)
    {
        this.canCannonFire=canCannonFire;
    }

    public static PirateShipEvents getOrCreate(ServerShip ship)
    {
        PirateShipEvents pirateShipEvents = ship.getAttachment(PirateShipEvents.class);
        if(pirateShipEvents==null)
        {
            pirateShipEvents=new PirateShipEvents();
            ship.saveAttachment(PirateShipEvents.class,pirateShipEvents);
        }
        return pirateShipEvents;
    }
}
