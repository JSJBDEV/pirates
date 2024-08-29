package ace.actually.pirates.sound;

import ace.actually.pirates.Pirates;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class ModSounds {
    public static SoundEvent CANNONBALL_SHOT = registerSoundEvent("cannonball_shot");

    private static SoundEvent registerSoundEvent(String name) {
        Identifier id = new Identifier("pirates", name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void registerSounds() {
        Pirates.LOGGER.info("Registering sounds");
    }
}
