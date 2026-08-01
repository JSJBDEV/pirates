package ace.actually.pirates.entities.pirate_skeleton;

import ace.actually.pirates.ClientPirates;
import ace.actually.pirates.entities.pirate_default.PirateEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.IllagerEntityRenderer;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.IllagerEntityModel;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class SkeletonPirateEntityRenderer extends IllagerEntityRenderer<SkeletonPirateEntity> {

    public SkeletonPirateEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new IllagerEntityModel<>(context.getPart(ClientPirates.SKELETON_PIRATE)), 0.5F);
        this.addFeature(new HeldItemFeatureRenderer<>(this, context.getHeldItemRenderer()));
    }

    @Override
    public Identifier getTexture(SkeletonPirateEntity entity) {
        return new Identifier("pirates", "textures/entity/skeleton_pirate.png");
    }
}
