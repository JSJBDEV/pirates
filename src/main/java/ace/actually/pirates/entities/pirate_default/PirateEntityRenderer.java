package ace.actually.pirates.entities.pirate_default;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.IllagerEntityRenderer;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.*;
import net.minecraft.util.Identifier;

public class PirateEntityRenderer extends IllagerEntityRenderer<PirateEntity> {
    public PirateEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new IllagerEntityModel(context.getPart(EntityModelLayers.PILLAGER)), 0.5F);
        this.addFeature(new HeldItemFeatureRenderer(this, context.getHeldItemRenderer()));
    }

    @Override
    public Identifier getTexture(PirateEntity entity) {
        return new Identifier("pirates","textures/entity/pirate2.png");
    }
}
