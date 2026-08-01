package ace.actually.pirates.blocks.entity;

import ace.actually.pirates.Pirates;
import ace.actually.pirates.blocks.CannonPrimingBlock;
import ace.actually.pirates.util.ConfigUtils;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.GameRules;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

public class CannonPrimingBlockEntity extends BlockEntity {

    public int cooldown = 0;
    private int lastCooldown = 40;
    public final double randomRotation;
    private static int cooldownConfig = -5;


    public CannonPrimingBlockEntity(BlockPos pos, BlockState state) {
        super(Pirates.CANNON_PRIMING_BLOCK_ENTITY, pos, state);
        randomRotation = Math.random() * 2.5;
    }

    public void tick(World world, BlockPos pos, BlockState state, CannonPrimingBlockEntity be) {


        if (world instanceof ServerWorld sw && cooldown == 0) {

            if ((checkShouldFire(sw, pos, state) && !state.get(CannonPrimingBlock.DISARMED))){


                fire(sw, pos, state);
            } else {
                cooldown = 3;
            }
        } else if (cooldown == 4) {
            if (!world.isReceivingRedstonePower(pos)) {
                cooldown --;
            }
        } else  {
            cooldown --;
        }

        if (state.get(RedstoneLampBlock.LIT) && lastCooldown - cooldown == 10) {
            world.setBlockState(pos, state.with(RedstoneLampBlock.LIT, false));
        }
    }

    public void fire(World world, BlockPos pos, BlockState state, int cooldown) {
        if (this.cooldown > 3) return;
        world.setBlockState(pos, state.with(RedstoneLampBlock.LIT, true));
        this.cooldown = cooldown;
        lastCooldown = this.cooldown;

        BlockPos ahead = pos.add(state.get(Properties.FACING).getVector());
        if (world.getBlockState(ahead).isOf(Pirates.DISPENSER_CANNON_BLOCK)) {
            world.scheduleBlockTick(ahead, world.getBlockState(ahead).getBlock(), 4);
        }
    }

    public void fire(ServerWorld world, BlockPos pos, BlockState state) {
        if(cooldownConfig==-5)
        {
            cooldownConfig = Integer.parseInt(ConfigUtils.config.getOrDefault("cannon-firing-pause","40"));
        }
        if(world.getGameRules().getBoolean(Pirates.PIRATES_IS_LIVE_WORLD)) {
            fire(world, pos, state, cooldownConfig + (int) (Math.random() * 20));
        }
    }


    private static boolean checkShouldFire(ServerWorld world, BlockPos pos, BlockState state) {
        Vec3i raycastStart = state.get(Properties.FACING).getVector();
        if(!(world.getBlockState(pos.add(raycastStart)).getBlock() instanceof DispenserBlock)|| world.getBlockState(pos.add(raycastStart)).get(Properties.FACING) != state.get(Properties.FACING)){
            return false;
        }

        // original is (2;32)
        RaycastContext context = new RaycastContext(
                VSGameUtilsKt.toWorldCoordinates(world, Vec3d.ofCenter(pos.add(raycastStart.multiply(30)))),
                VSGameUtilsKt.toWorldCoordinates(world, Vec3d.ofCenter(pos.add(raycastStart.multiply(80)))),
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                null);

        BlockHitResult result = world.raycast(context);
        ServerShip thisShip = VSGameUtilsKt.getShipManagingPos(world,result.getBlockPos());
        if(thisShip!=null)
        {
            Team team = world.getScoreboard().getPlayerTeam(thisShip.getSlug());
            if(team!=null)
            {
                ServerShip otherShip = VSGameUtilsKt.getShipManagingPos(world,result.getBlockPos());
                Team otherTeam = world.getScoreboard().getPlayerTeam(otherShip.getSlug());
                if(otherTeam!=null)
                {
                    return !team.isEqual(otherTeam);
                }
            }
            return true;
        }
        return false;
    }

}
