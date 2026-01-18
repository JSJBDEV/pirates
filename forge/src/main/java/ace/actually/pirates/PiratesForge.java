package ace.actually.pirates;

import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Pirates.MOD_ID)
@Mod.EventBusSubscriber
public class PiratesForge {
    public PiratesForge()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        // Submit our event bus to let Architectury API register our content on the right time.
        EventBuses.registerModEventBus(Pirates.MOD_ID, modEventBus);
        Pirates.init();
    }

}
