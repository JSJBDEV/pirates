package ace.actually.pirates.blocks;

import ace.actually.pirates.Pirates;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.PillagerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.event.BlockPositionSource;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.PositionSource;
import net.minecraft.world.event.listener.GameEventListener;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

public class CannonPrimingBlockEntity extends BlockEntity {

    public int cooldown = 0;
    private int lastCooldown = 40;

    private final PirateDeathListener eventListener;

    public CannonPrimingBlockEntity(BlockPos pos, BlockState state) {
        super(Pirates.CANNON_PRIMING_BLOCK_ENTITY, pos, state);
        this.eventListener = new PirateDeathListener(state, new BlockPositionSource(pos), pos);
    }


    public void tick(World world, BlockPos pos, BlockState state, CannonPrimingBlockEntity be) {


        if (!world.isClient && cooldown == 0) {

            if (checkShouldFire(world, pos, state) && !state.get(CannonPrimingBlock.DISARMED)){
                world.setBlockState(pos, state.with(RedstoneLampBlock.LIT, true));
                cooldown = 40 + (int) (Math.random() * 20);
                lastCooldown = cooldown;
            } else {
                cooldown = 3;
            }
        } else {
            cooldown --;
        }

        if (state.get(RedstoneLampBlock.LIT) && lastCooldown - cooldown == 10) {
            world.setBlockState(pos, state.with(RedstoneLampBlock.LIT, false));
        }
    }

    private static boolean checkShouldFire(World world, BlockPos pos, BlockState state) {
        Vec3i raycastStart = state.get(Properties.FACING).getVector();
        if(!(world.getBlockState(pos.add(raycastStart)).getBlock() instanceof DispenserBlock)|| world.getBlockState(pos.add(raycastStart)).get(Properties.FACING) != state.get(Properties.FACING)){
            return false;
        }

        RaycastContext context = new RaycastContext(
                VSGameUtilsKt.toWorldCoordinates(world, Vec3d.ofCenter(pos.add(raycastStart.multiply(2)))),
                VSGameUtilsKt.toWorldCoordinates(world, Vec3d.ofCenter(pos.add(raycastStart.multiply(32)))),
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                null);

        BlockHitResult result = world.raycast(context);
        return  (VSGameUtilsKt.isBlockInShipyard(world, result.getBlockPos()));
    }

    public static class PirateDeathListener implements GameEventListener {
        private static final int RANGE = 16;
        private final BlockState state;
        private final BlockPos listenerPos;
        private final PositionSource positionSource;

        public PirateDeathListener(BlockState state, PositionSource positionSource, BlockPos listenerPos) {
            this.state = state;
            this.listenerPos = listenerPos;
            this.positionSource = positionSource;
        }

        @Override
        public PositionSource getPositionSource() {
            return this.positionSource;
        }

        @Override
        public int getRange() {
            return RANGE;
        }

        @Override
        public GameEventListener.TriggerOrder getTriggerOrder() {
            return TriggerOrder.BY_DISTANCE;
        }

        @Override
        public boolean listen(ServerWorld world, GameEvent event, GameEvent.Emitter emitter, Vec3d emitterPos) {
            // Only handle ENTITY_DIE event
            if (event == GameEvent.ENTITY_DIE) {
                Entity sourceEntity = emitter.sourceEntity();
                if (sourceEntity instanceof PillagerEntity) {
                    // Handle the Pillager's death here
                    this.triggerAction(world, BlockPos.ofFloored(emitterPos), state);
                    return true;
                }
            }
            return false;
        }

        private void triggerAction(ServerWorld world, BlockPos pos, BlockState state) {
            // Custom action when a Pillager dies nearby
            System.out.println("A Pillager died near " + pos);

            // Example: Set block state, play sound, spawn particles, etc.
            world.setBlockState(listenerPos, state.with(CannonPrimingBlock.DISARMED, true), 3);
        }
    }

}
