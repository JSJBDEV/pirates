package ace.actually.pirates.entities.shot;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

public class ShotEntityRenderer extends ThrownItemRenderer<ShotEntity> {
    public ShotEntityRenderer(EntityRendererProvider.Context ctx, float scale, boolean lit) {
        super(ctx, scale, lit);
    }
}
