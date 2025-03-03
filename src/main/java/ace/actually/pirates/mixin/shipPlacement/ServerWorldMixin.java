package ace.actually.pirates.mixin.shipPlacement;

import ace.actually.pirates.structures.ShipStructurePlacementHelper;
import kotlin.Triple;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

import static ace.actually.pirates.structures.ShipStructurePlacementHelper.shipQueue;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    protected void tickMixin(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        if (!shipQueue.isEmpty()) {
            Triple<StructureTemplate, ServerWorld, BlockPos> structureData = shipQueue.poll();
            assert structureData != null;
            ShipStructurePlacementHelper.createShip(structureData.getFirst(), structureData.getSecond(), structureData.getThird());
        }
    }
}
