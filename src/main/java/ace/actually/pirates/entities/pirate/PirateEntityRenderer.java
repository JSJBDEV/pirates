package ace.actually.pirates.entities.pirate;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;

public class PirateEntityRenderer extends MobEntityRenderer<PirateEntity, EntityModel<PirateEntity>> {
    public PirateEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new PirateEntityModel(context.getPart(EntityModelLayers.PILLAGER)), 0.5F);
        this.addFeature(new HeldItemFeatureRenderer(this, context.getHeldItemRenderer()));
    }

    @Override
    public Identifier getTexture(PirateEntity entity) {
        return new Identifier("pirates","textures/entity/pirate2.png");
    }
}
