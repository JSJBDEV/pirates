package ace.actually.pirates.events;

import ace.actually.pirates.entities.pirate_abstract.AbstractPirateEntity;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;

public interface IPirateDies {
    Event<IPirateDies> EVENT = EventFactory.createArrayBacked(IPirateDies.class,
            (listeners) -> (player, pirate) -> {
                for (IPirateDies listener : listeners) {
                    InteractionResult result = listener.interact(player, pirate);

                    if(result != InteractionResult.PASS) {
                        return result;
                    }
                }

                return InteractionResult.PASS;
            });

    InteractionResult interact(Player player,AbstractPirateEntity pirate);
}
