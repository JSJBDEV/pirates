package ace.actually.pirates.entities;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;

public class ShotEntityRenderer extends FlyingItemEntityRenderer<ShotEntity> {
    public ShotEntityRenderer(EntityRendererFactory.Context ctx, float scale, boolean lit) {
        super(ctx, scale, lit);
    }
}
