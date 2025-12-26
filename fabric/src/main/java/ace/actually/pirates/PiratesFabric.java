package ace.actually.pirates;

import ace.actually.pirates.Pirates;
import net.fabricmc.api.ModInitializer;

public class PiratesFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        Pirates.init();
    }
}
