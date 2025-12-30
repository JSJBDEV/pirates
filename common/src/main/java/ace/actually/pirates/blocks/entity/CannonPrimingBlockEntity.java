package ace.actually.pirates.blocks.entity;

import ace.actually.pirates.Pirates;
import ace.actually.pirates.blocks.CannonPrimingBlock;
import ace.actually.pirates.util.ConfigUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

public class CannonPrimingBlockEntity extends BlockEntity {

    public int cooldown = 0;
    private int lastCooldown = 40;
    public final double randomRotation;
    private static int cooldownConfig = -5;


    public CannonPrimingBlockEntity(BlockPos pos, BlockState state) {
        super(Pirates.CANNON_PRIMING_BLOCK_ENTITY.get(), pos, state);
        randomRotation = Math.random() * 2.5;
    }

    public void tick(Level world, BlockPos pos, BlockState state, CannonPrimingBlockEntity be) {


        if (world instanceof ServerLevel sw && cooldown == 0) {

            if ((checkShouldFire(sw, pos, state) && !state.getValue(CannonPrimingBlock.DISARMED))){


                fire(sw, pos, state);
            } else {
                cooldown = 3;
            }
        } else if (cooldown == 4) {
            if (!world.hasNeighborSignal(pos)) {
                cooldown --;
            }
        } else  {
            cooldown --;
        }

        if (state.getValue(RedstoneLampBlock.LIT) && lastCooldown - cooldown == 10) {
            world.setBlockAndUpdate(pos, state.setValue(RedstoneLampBlock.LIT, false));
        }
    }

    public void fire(Level world, BlockPos pos, BlockState state, int cooldown) {
        if (this.cooldown > 3) return;
        world.setBlockAndUpdate(pos, state.setValue(RedstoneLampBlock.LIT, true));
        this.cooldown = cooldown;
        lastCooldown = this.cooldown;

        BlockPos ahead = pos.offset(state.getValue(BlockStateProperties.FACING).getNormal());
        if (world.getBlockState(ahead).is(Pirates.DISPENSER_CANNON_BLOCK.get())) {
            world.scheduleTick(ahead, world.getBlockState(ahead).getBlock(), 4);
        }
    }

    public void fire(ServerLevel world, BlockPos pos, BlockState state) {
        if(cooldownConfig==-5)
        {
            cooldownConfig = Integer.parseInt(ConfigUtils.config.getOrDefault("cannon-firing-pause","40"));
        }
        if(world.getGameRules().getBoolean(Pirates.PIRATES_IS_LIVE_WORLD)) {
            fire(world, pos, state, cooldownConfig + (int) (Math.random() * 20));
        }
    }


    private static boolean checkShouldFire(ServerLevel world, BlockPos pos, BlockState state) {
        Vec3i raycastStart = state.getValue(BlockStateProperties.FACING).getNormal();
        if(!(world.getBlockState(pos.offset(raycastStart)).getBlock() instanceof DispenserBlock)|| world.getBlockState(pos.offset(raycastStart)).getValue(BlockStateProperties.FACING) != state.getValue(BlockStateProperties.FACING)){
            return false;
        }

        ClipContext context = new ClipContext(
                VSGameUtilsKt.toWorldCoordinates(world, Vec3.atCenterOf(pos.offset(raycastStart.multiply(2)))),
                VSGameUtilsKt.toWorldCoordinates(world, Vec3.atCenterOf(pos.offset(raycastStart.multiply(32)))),
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                null);

        BlockHitResult result = world.clip(context);
        if(VSGameUtilsKt.isBlockInShipyard(world, result.getBlockPos()))
        {
            ServerShip thisShip = VSGameUtilsKt.getShipManagingPos(world,result.getBlockPos());
            PlayerTeam team = world.getScoreboard().getPlayersTeam(thisShip.getSlug());
            if(team!=null)
            {
                ServerShip otherShip = VSGameUtilsKt.getShipManagingPos(world,result.getBlockPos());
                PlayerTeam otherTeam = world.getScoreboard().getPlayersTeam(otherShip.getSlug());
                if(otherTeam!=null)
                {
                    return !team.isAlliedTo(otherTeam);
                }
            }
            return true;
        }
        return false;
    }

}
