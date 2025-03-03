package ace.actually.pirates.mixin.bugfixes;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class SpeedMixin {
    @Inject(
            at = {@At("HEAD")},
            method = {"setVelocity(Lnet/minecraft/util/math/Vec3d;)V"},
            cancellable = true
    )
    protected void init(Vec3d velocity, CallbackInfo ci) {
        if (Math.abs(velocity.x) > 100.0 || Math.abs(velocity.y) > 100.0 || Math.abs(velocity.z) > 100.0) {
            System.out.println("Tried to apply a velocity greater then 100 "+ velocity +" to an entity! cancelling.");
            ci.cancel();
        }

    }
}
