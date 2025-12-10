package ace.actually.pirates.entities;

import net.minecraft.world.level.block.state.properties.EnumProperty;

public interface CrewTypes {
    EnumProperty<CrewSpawnType> CREW_SPAWN_TYPE = EnumProperty.create("crew_spawn_type", CrewSpawnType.class);
}
