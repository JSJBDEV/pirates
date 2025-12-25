package ace.actually.pirates.entities.friendly_pirate;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

public class FriendlyPirateRenderer extends MobRenderer<FriendlyPirateEntity, EntityModel<FriendlyPirateEntity>> {
    public FriendlyPirateRenderer(EntityRendererProvider.Context context) {
        super(context, new FriendlyPirateModel(context.bakeLayer(ModelLayers.PILLAGER)), 0.5F);
        this.addLayer(new ItemInHandLayer(this, context.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(FriendlyPirateEntity entity) {
        return new ResourceLocation("pirates","textures/entity/pirate1.png");
    }
}
