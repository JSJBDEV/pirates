package ace.actually.pirates.entities.legacy;

import ace.actually.pirates.ClientPirates;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;

public class ShipEntityRenderer extends MobEntityRenderer<PirateShipEntity,PirateShipModel> {
    public ShipEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new PirateShipModel(context.getPart(ClientPirates.MODEL_SHIP_LAYER)), 0.5f);
    }

    @Override
    public Identifier getTexture(PirateShipEntity entity) {
        return new Identifier("pirates", "textures/entity/ship/ship.png");
    }
}
