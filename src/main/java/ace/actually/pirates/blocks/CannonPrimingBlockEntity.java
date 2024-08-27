package ace.actually.pirates.blocks;

import ace.actually.pirates.ConfigUtils;
import ace.actually.pirates.Pirates;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.RedstoneLampBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.core.api.world.ServerShipWorld;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.util.DimensionIdProvider;

import java.util.List;

public class CannonPrimingBlockEntity extends BlockEntity {

    public int cooldown = 0;

    public CannonPrimingBlockEntity(BlockPos pos, BlockState state) {
        super(Pirates.CANNON_PRIMING_BLOCK_ENTITY, pos, state);
    }

    public void tick(World world, BlockPos pos, BlockState state, CannonPrimingBlockEntity be) {
        if (state.get(RedstoneLampBlock.LIT)) {
            world.setBlockState(pos, state.with(RedstoneLampBlock.LIT, false));
        }

        if (!world.isClient && cooldown == 0) {
            if (checkShouldFire(world, pos, state)){
                world.setBlockState(pos, state.with(RedstoneLampBlock.LIT, true));
                cooldown = 40 + (int) (Math.random() * 20);
            } else {
                cooldown = 3;
            }
        } else {
            cooldown --;
        }
    }

    private static boolean checkShouldFire(World world, BlockPos pos, BlockState state) {
        Vec3i raycastStart = state.get(Properties.FACING).getVector();
        if (!world.getBlockState(pos.add(raycastStart)).isOf(Blocks.DISPENSER)){
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
}
