package ace.actually.pirates.items;

import ace.actually.pirates.blocks.entity.CrewSpawnerBlockEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class TestItem extends Item {
    public TestItem(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        if(world instanceof ServerLevel sw && hand==InteractionHand.MAIN_HAND)
        {
            Entity e = CrewSpawnerBlockEntity.makeCustomCrew(world,0);
            sw.addFreshEntity(e);
        }
        return super.use(world, user, hand);
    }
}
