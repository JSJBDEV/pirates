package ace.actually.pirates.items;

import ace.actually.pirates.blocks.entity.CrewSpawnerBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class TestItem extends Item {
    public TestItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if(world instanceof ServerWorld sw && hand==Hand.MAIN_HAND)
        {
            Entity e = CrewSpawnerBlockEntity.makeCustomCrew(world,0);
            sw.spawnEntity(e);
        }
        return super.use(world, user, hand);
    }
}
