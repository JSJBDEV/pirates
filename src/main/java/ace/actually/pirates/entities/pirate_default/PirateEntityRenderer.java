package ace.actually.pirates.entities.pirate_default;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

public class PirateEntityRenderer extends MobRenderer<PirateEntity, EntityModel<PirateEntity>> {
    public PirateEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new PirateEntityModel(context.bakeLayer(ModelLayers.PILLAGER)), 0.5F);
        this.addLayer(new ItemInHandLayer(this, context.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(PirateEntity entity) {
        return new ResourceLocation("pirates","textures/entity/pirate2.png");
    }
}
