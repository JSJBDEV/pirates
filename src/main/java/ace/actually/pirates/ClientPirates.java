package ace.actually.pirates;

import ace.actually.pirates.entities.ShotEntityRenderer;
import ace.actually.pirates.entities.legacy.PirateShipModel;
import ace.actually.pirates.entities.legacy.ShipEntityRenderer;
import ace.actually.pirates.entities.pirate.PirateEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

public class ClientPirates implements ClientModInitializer {
    public static final EntityModelLayer MODEL_SHIP_LAYER = new EntityModelLayer(new Identifier("pirates", "ship_layer"), "main");
    public static final EntityModelLayer MODEL_CANNON_LAYER = new EntityModelLayer(new Identifier("pirates", "cannon_layer"), "main");

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(Pirates.SHIP, ShipEntityRenderer::new);
        EntityRendererRegistry.register(Pirates.PIRATE_ENTITY_TYPE, PirateEntityRenderer::new);
        EntityRendererRegistry.register(Pirates.SHOT_ENTITY_TYPE, (context) -> new ShotEntityRenderer(context, 1,false));

        EntityModelLayerRegistry.registerModelLayer(MODEL_SHIP_LAYER, PirateShipModel::getTexturedModelData);


    }

}
