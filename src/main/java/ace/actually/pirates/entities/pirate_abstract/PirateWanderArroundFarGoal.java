package ace.actually.pirates.entities.pirate_abstract;

import net.minecraft.entity.ai.FuzzyTargeting;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

public class PirateWanderArroundFarGoal extends WanderAroundGoal {
    public static final float CHANCE = 0.001F;
    protected final float probability;

    public PirateWanderArroundFarGoal(PathAwareEntity pathAwareEntity, double d) {
        this(pathAwareEntity, d, 0.001F);
    }

    public PirateWanderArroundFarGoal(PathAwareEntity mob, double speed, float probability) {
        super(mob, speed);
        this.probability = probability;
    }

    @Nullable
    protected Vec3d getWanderTarget() {
        if (VSGameUtilsKt.getShipManaging(this.mob) != null) {
            return null;
        } else if (this.mob.isInsideWaterOrBubbleColumn()) {
            Vec3d vec3d = FuzzyTargeting.find(this.mob, 15, 7);
            return vec3d == null ? super.getWanderTarget() : vec3d;
        } else {
            return this.mob.getRandom().nextFloat() >= this.probability ? FuzzyTargeting.find(this.mob, 10, 7) : super.getWanderTarget();
        }
    }
}

