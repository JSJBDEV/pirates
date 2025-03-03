package ace.actually.pirates.events;

import ace.actually.pirates.entities.pirate_abstract.AbstractPirateEntity;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;

public interface IPirateSpawns {
    Event<IPirateSpawns> EVENT = EventFactory.createArrayBacked(IPirateSpawns.class,
            (listeners) -> (pirate) -> {
                for (IPirateSpawns listener : listeners) {
                    ActionResult result = listener.interact(pirate);

                    if(result != ActionResult.PASS) {
                        return result;
                    }
                }

                return ActionResult.PASS;
            });

    ActionResult interact(AbstractPirateEntity pirate);
}
