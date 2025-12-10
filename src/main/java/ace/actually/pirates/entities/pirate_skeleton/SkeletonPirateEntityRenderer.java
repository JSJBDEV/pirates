package ace.actually.pirates.entities.pirate_skeleton;

import ace.actually.pirates.ClientPirates;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class SkeletonPirateEntityRenderer extends MobRenderer<SkeletonPirateEntity, SkeletonPirateModel<SkeletonPirateEntity>> {

    public SkeletonPirateEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new SkeletonPirateModel<>(context.bakeLayer(ClientPirates.SKELETON_PIRATE)), 0.5F);
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(SkeletonPirateEntity entity) {
        return new ResourceLocation("pirates", "textures/entity/skeleton_pirate.png");
    }
}
