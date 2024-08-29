package ace.actually.pirates.entities.pirate;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.util.Identifier;

public class PirateEntityRenderer extends MobEntityRenderer<PirateEntity, BipedEntityModel<PirateEntity>> {
    public PirateEntityRenderer(EntityRendererFactory.Context context) {
        super(context,new PlayerEntityModel<>(context.getPart(EntityModelLayers.PLAYER),false),0.5f);
    }

    @Override
    public Identifier getTexture(PirateEntity entity) {
        return new Identifier("pirates","textures/entity/pirate.png");
    }
}
