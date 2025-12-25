package ace.actually.pirates.forge.client

import ace.actually.pirates.client.PiratesClient
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent

class VSTemplateModForgeClient {
    companion object {
        @JvmStatic
        fun clientInit(event: FMLClientSetupEvent) {
            // Put anything initialized on forge-side client here.
            PiratesClient.initClient()
        }
    }
}
