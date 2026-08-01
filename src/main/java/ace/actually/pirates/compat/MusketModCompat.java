package ace.actually.pirates.compat;

import ace.actually.pirates.entities.pirate_abstract.AbstractPirateEntity;
import ace.actually.pirates.entities.pirate_abstract.PirateBowAttackGoal;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

import java.lang.reflect.Constructor;

public final class MusketModCompat {
    public static final String MOD_ID = "musketmod";
    private static final Identifier[] GUN_IDS = {
            id("musket"), id("musket_with_bayonet"), id("blunderbuss"), id("pistol"), id("musket_with_scope")
    };
    private static final Identifier PISTOL_ID = id("pistol");

    private MusketModCompat() {}

    public static boolean isLoaded() {
        return FabricLoader.getInstance().isModLoaded(MOD_ID);
    }

    public static Item randomGun(Random random) {
        if (!isLoaded()) return null;
        Item item = Registries.ITEM.get(GUN_IDS[random.nextInt(GUN_IDS.length)]);
        return item == Items.AIR ? null : item;
    }

    public static void equipRandomGunOrBow(AbstractPirateEntity pirate, Random random) {
        Item gun = randomGun(random);
        pirate.equipStack(EquipmentSlot.MAINHAND, new ItemStack(gun == null ? Items.BOW : gun));
        if (gun != null && Registries.ITEM.getId(gun).equals(PISTOL_ID)) {
            pirate.equipStack(EquipmentSlot.OFFHAND, new ItemStack(gun));
        }
    }

    public static boolean isHoldingGun(AbstractPirateEntity pirate) {
        return isLoaded() && Registries.ITEM.getId(pirate.getMainHandStack().getItem()).getNamespace().equals(MOD_ID);
    }

    public static boolean isHoldingPistol(AbstractPirateEntity pirate) {
        return isLoaded() && Registries.ITEM.getId(pirate.getMainHandStack().getItem()).equals(PISTOL_ID);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <T extends HostileEntity & RangedAttackMob> Goal createRangedGoal(T pirate) {
        if (isLoaded()) {
            try {
                Class<?> type = Class.forName("ace.actually.pirates.entities.pirate_abstract.PirateGunAttackGoal");
                Constructor<?> constructor = type.getConstructor(HostileEntity.class, double.class, int.class, float.class);
                return (Goal) constructor.newInstance(pirate, 1.0D, 20, 20.0F);
            } catch (ReflectiveOperationException | LinkageError ignored) {
                // Missing or incompatible optional dependency: retain bow behavior.
            }
        }
        return new PirateBowAttackGoal(pirate, 1.0D, 20, 20.0F);
    }

    private static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }
}
