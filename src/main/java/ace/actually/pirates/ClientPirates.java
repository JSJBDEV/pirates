package ace.actually.pirates;

import ace.actually.pirates.blocks.entity.CannonPrimingBlockEntityRenderer;
import ace.actually.pirates.blocks.entity.ShipIdBlockEntityRenderer;
import ace.actually.pirates.entities.friendly_pirate.FriendlyPirateRenderer;
import ace.actually.pirates.entities.pirate_skeleton.SkeletonPirateModel;
import ace.actually.pirates.entities.shot.ShotEntityRenderer;
import ace.actually.pirates.entities.pirate_default.PirateEntityRenderer;
import ace.actually.pirates.entities.pirate_skeleton.SkeletonPirateEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.resources.ResourceLocation;

public class ClientPirates implements ClientModInitializer {
    public static final ModelLayerLocation SKELETON_PIRATE = new ModelLayerLocation(new ResourceLocation("pirates", "skeleton_pirate"), "main");

    @Override
    public void onInitializeClient() {

        EntityRendererRegistry.register(Pirates.PIRATE_ENTITY_TYPE, PirateEntityRenderer::new);
        EntityRendererRegistry.register(Pirates.FRIENDLY_PIRATE_TYPE, FriendlyPirateRenderer::new);
        //EntityRendererRegistry.register(Pirates.SKELETON_PIRATE_ENTITY_TYPE, SkeletonPirateEntityRenderer::new);
        EntityRendererRegistry.register(Pirates.SHOT_ENTITY_TYPE, (context) -> new ShotEntityRenderer(context, 1,false));

        BlockEntityRenderers.register(Pirates.CANNON_PRIMING_BLOCK_ENTITY, CannonPrimingBlockEntityRenderer::new);
        BlockEntityRenderers.register(Pirates.SHIP_ID_BLOCK_ENTITY, ShipIdBlockEntityRenderer::new);
        BlockRenderLayerMap.INSTANCE.putBlock(Pirates.SHIP_ID_BLOCK, RenderType.translucent());

        //EntityModelLayerRegistry.registerModelLayer(SKELETON_PIRATE, SkeletonPirateModel::getTexturedModelData);

    }

}
