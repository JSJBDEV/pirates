package ace.actually.pirates;

import ace.actually.pirates.blocks.entity.CannonPrimingBlockEntityRenderer;
import ace.actually.pirates.entities.friendly_pirate.FriendlyPirateRenderer;
import ace.actually.pirates.entities.pirate_skeleton.SkeletonPirateModel;
import ace.actually.pirates.entities.shot.ShotEntityRenderer;
import ace.actually.pirates.entities.pirate_default.PirateEntityRenderer;
import ace.actually.pirates.entities.pirate_skeleton.SkeletonPirateEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

public class ClientPirates implements ClientModInitializer {
    public static final EntityModelLayer SKELETON_PIRATE = new EntityModelLayer(new Identifier("pirates", "skeleton_pirate"), "main");

    @Override
    public void onInitializeClient() {

        EntityRendererRegistry.register(Pirates.PIRATE_ENTITY_TYPE, PirateEntityRenderer::new);
        EntityRendererRegistry.register(Pirates.FRIENDLY_PIRATE_TYPE, FriendlyPirateRenderer::new);
        //EntityRendererRegistry.register(Pirates.SKELETON_PIRATE_ENTITY_TYPE, SkeletonPirateEntityRenderer::new);
        EntityRendererRegistry.register(Pirates.SHOT_ENTITY_TYPE, (context) -> new ShotEntityRenderer(context, 1,false));

        BlockEntityRendererFactories.register(Pirates.CANNON_PRIMING_BLOCK_ENTITY, CannonPrimingBlockEntityRenderer::new);

        //EntityModelLayerRegistry.registerModelLayer(SKELETON_PIRATE, SkeletonPirateModel::getTexturedModelData);

    }

}
