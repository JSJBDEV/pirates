package ace.actually.pirates.fabric.client

import ace.actually.pirates.client.PiratesClient
import net.fabricmc.api.ClientModInitializer

/**
 * The fabric-side client initializer for the mod. Used for fabric-platform-specific code that runs on the client exclusively.
 */
class VSTemplateModFabricClient : ClientModInitializer {
    override fun onInitializeClient() {
        // Put anything initialized on fabric-side client here.
        PiratesClient.initClient()
    }
}
