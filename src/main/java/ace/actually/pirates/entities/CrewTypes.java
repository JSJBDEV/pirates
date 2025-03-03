package ace.actually.pirates.entities;

import net.minecraft.state.property.EnumProperty;

public interface CrewTypes {
    EnumProperty<CrewSpawnType> CREW_SPAWN_TYPE = EnumProperty.of("crew_spawn_type", CrewSpawnType.class);
}
