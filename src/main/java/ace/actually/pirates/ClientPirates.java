package ace.actually.pirates;

import ace.actually.pirates.blocks.entity.CannonPrimingBlockEntityRenderer;
import ace.actually.pirates.entities.pirate_skeleton.SkeletonPirateModelLayer;
import ace.actually.pirates.entities.shot.ShotEntityRenderer;
import ace.actually.pirates.entities.pirate.PirateEntityRenderer;
import ace.actually.pirates.entities.pirate_skeleton.SkeletonPirateEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

public class ClientPirates implements ClientModInitializer {
    public static final EntityModelLayer MODEL_SHIP_LAYER = new EntityModelLayer(new Identifier("pirates", "ship_layer"), "main");
    public static final EntityModelLayer MODEL_CANNON_LAYER = new EntityModelLayer(new Identifier("pirates", "cannon_layer"), "main");

    @Override
    public void onInitializeClient() {

        EntityRendererRegistry.register(Pirates.PIRATE_ENTITY_TYPE, PirateEntityRenderer::new);
        EntityRendererRegistry.register(Pirates.SKELETON_PIRATE_ENTITY_TYPE, SkeletonPirateEntityRenderer::new);
        EntityRendererRegistry.register(Pirates.SHOT_ENTITY_TYPE, (context) -> new ShotEntityRenderer(context, 1,false));

        BlockEntityRendererFactories.register(Pirates.CANNON_PRIMING_BLOCK_ENTITY, CannonPrimingBlockEntityRenderer::new);

        SkeletonPirateModelLayer.registerModelLayers();

    }

}
