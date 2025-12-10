package ace.actually.pirates.events;

import ace.actually.pirates.entities.pirate_abstract.AbstractPirateEntity;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.InteractionResult;

public interface IPirateSpawns {
    Event<IPirateSpawns> EVENT = EventFactory.createArrayBacked(IPirateSpawns.class,
            (listeners) -> (pirate) -> {
                for (IPirateSpawns listener : listeners) {
                    InteractionResult result = listener.interact(pirate);

                    if(result != InteractionResult.PASS) {
                        return result;
                    }
                }

                return InteractionResult.PASS;
            });

    InteractionResult interact(AbstractPirateEntity pirate);
}
