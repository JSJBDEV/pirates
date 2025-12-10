package ace.actually.pirates.sound;

import ace.actually.pirates.Pirates;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class ModSounds {
    public static SoundEvent CANNONBALL_SHOT = registerSoundEvent("cannonball_shot");

    private static SoundEvent registerSoundEvent(String name) {
        ResourceLocation id = new ResourceLocation("pirates", name);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    public static void registerSounds() {
        Pirates.LOGGER.info("Registering sounds");
    }
}
