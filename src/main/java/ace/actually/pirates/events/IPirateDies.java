package ace.actually.pirates.events;

import ace.actually.pirates.entities.pirate_abstract.AbstractPirateEntity;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;

public interface IPirateDies {
    Event<IPirateDies> EVENT = EventFactory.createArrayBacked(IPirateDies.class,
            (listeners) -> (player, pirate) -> {
                for (IPirateDies listener : listeners) {
                    ActionResult result = listener.interact(player, pirate);

                    if(result != ActionResult.PASS) {
                        return result;
                    }
                }

                return ActionResult.PASS;
            });

    ActionResult interact(PlayerEntity player,AbstractPirateEntity pirate);
}
