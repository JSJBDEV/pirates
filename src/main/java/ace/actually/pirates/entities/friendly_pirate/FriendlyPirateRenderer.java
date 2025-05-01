package ace.actually.pirates.entities.friendly_pirate;

import ace.actually.pirates.entities.pirate_default.PirateEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.util.Identifier;

public class FriendlyPirateRenderer extends MobEntityRenderer<FriendlyPirateEntity, EntityModel<FriendlyPirateEntity>> {
    public FriendlyPirateRenderer(EntityRendererFactory.Context context) {
        super(context, new FriendlyPirateModel(context.getPart(EntityModelLayers.PILLAGER)), 0.5F);
        this.addFeature(new HeldItemFeatureRenderer(this, context.getHeldItemRenderer()));
    }

    @Override
    public Identifier getTexture(FriendlyPirateEntity entity) {
        return new Identifier("pirates","textures/entity/pirate2.png");
    }
}
