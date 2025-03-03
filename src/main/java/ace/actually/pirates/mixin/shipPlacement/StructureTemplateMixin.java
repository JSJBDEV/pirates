package ace.actually.pirates.mixin.shipPlacement;

import ace.actually.pirates.structures.ShipStructurePlacementHelper;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.valkyrienskies.mod.common.VSGameUtilsKt;


@Mixin(value = StructureTemplate.class)
public abstract class StructureTemplateMixin {

    @Shadow private String author;

    @Shadow public abstract void setAuthor(String author);

    @Inject(method = "place", at = @At("HEAD"), cancellable = true)
    public void placeMixin(ServerWorldAccess world, BlockPos pos, BlockPos pivot, StructurePlacementData placementData, Random random, int flags, CallbackInfoReturnable<Boolean> cir) {
        try {
            if (VSGameUtilsKt.isBlockInShipyard(world.toServerWorld(), pos)) return;

            if (this.author.equals("pirate-ship")) {
                ShipStructurePlacementHelper.placeShipTemplate(
                        (StructureTemplate) (Object) this,
                        world.toServerWorld(),
                        pos);

                this.setAuthor("dirty");
                cir.setReturnValue(true);
                cir.cancel();
            } else if (this.author.equals("dirty")) {
                cir.setReturnValue(false);
                cir.cancel();
            }
        }
        catch (IllegalStateException ignored)
        {

        }


    }
}
