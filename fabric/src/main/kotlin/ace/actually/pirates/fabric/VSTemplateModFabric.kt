package ace.actually.pirates.fabric

import net.fabricmc.api.ModInitializer
import ace.actually.pirates.Pirates

/**
 * The fabric-side initializer for the mod. Used for fabric-platform-specific code.
 */
class VSTemplateModFabric : ModInitializer {
    override fun onInitialize() {
        // Put anything initialized on fabric-side here, such as platform-specific registries.
        Pirates.init()
    }
}
