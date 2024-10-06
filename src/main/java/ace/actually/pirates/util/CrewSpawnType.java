package ace.actually.pirates.util;


import net.minecraft.util.StringIdentifiable;

public enum CrewSpawnType implements StringIdentifiable {
    PIRATE("pirate"),
    VILLAGER("villager"),
    SKELETON_PIRATE("skeleton_pirate"),
    CUSTOM_0("custom_0"),
    CUSTOM_1("custom_1"),
    CUSTOM_2("custom_2"),
    CUSTOM_3("custom_3");

    private final String name;

    CrewSpawnType(String name) {
        this.name = name;
    }

    @Override
    public String asString() {
        return this.name;
    }
}
