package ace.actually.pirates.mixin.compat;

import ace.actually.pirates.Pirates;
import net.minecraft.block.BlockState;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;



@Mixin(value = FallingBlockEntity.class)
public abstract class FallingBlockEntityMixin{

    /**
     @author G_Mungus
     @reason fix behavior with VS Crumbles
     **/

    @Inject(method = "spawnFromBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private static void beforeSpawnEntity(World world, BlockPos pos, BlockState state, CallbackInfoReturnable<FallingBlockEntity> cir, FallingBlockEntity fallingBlockEntity) {
        if (fallingBlockEntity.getBlockState().isOf(Pirates.CREW_SPAWNER_BLOCK) ||
                fallingBlockEntity.getBlockState().isOf(Pirates.MOTION_INVOKING_BLOCK)) {
            fallingBlockEntity.dropItem = false;
        }
    }
}
