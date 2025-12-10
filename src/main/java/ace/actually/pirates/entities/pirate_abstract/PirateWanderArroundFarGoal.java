package ace.actually.pirates.entities.pirate_abstract;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

public class PirateWanderArroundFarGoal extends RandomStrollGoal {
    public static final float CHANCE = 0.001F;
    protected final float probability;

    public PirateWanderArroundFarGoal(PathfinderMob pathAwareEntity, double d) {
        this(pathAwareEntity, d, 0.001F);
    }

    public PirateWanderArroundFarGoal(PathfinderMob mob, double speed, float probability) {
        super(mob, speed);
        this.probability = probability;
    }

    @Nullable
    protected Vec3 getPosition() {
        if (VSGameUtilsKt.getShipManaging(this.mob) != null) {
            return null;
        } else if (this.mob.isInWaterOrBubble()) {
            Vec3 vec3d = LandRandomPos.getPos(this.mob, 15, 7);
            return vec3d == null ? super.getPosition() : vec3d;
        } else {
            return this.mob.getRandom().nextFloat() >= this.probability ? LandRandomPos.getPos(this.mob, 10, 7) : super.getPosition();
        }
    }
}

