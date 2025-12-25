package ace.actually.pirates.client;

import ace.actually.pirates.Pirates;
import ace.actually.pirates.blocks.entity.CannonPrimingBlockEntityRenderer;
import ace.actually.pirates.blocks.entity.ShipIdBlockEntityRenderer;
import ace.actually.pirates.entities.friendly_pirate.FriendlyPirateRenderer;
import ace.actually.pirates.entities.pirate_default.PirateEntityRenderer;
import ace.actually.pirates.entities.shot.ShotEntityRenderer;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class PiratesClient {

    public static final ModelLayerLocation SKELETON_PIRATE = new ModelLayerLocation(new ResourceLocation("pirates", "skeleton_pirate"), "main");

    public static void initClientFromMain()
    {

        EntityRendererRegistry.register(Pirates.PIRATE_ENTITY_TYPE, PirateEntityRenderer::new);
        EntityRendererRegistry.register(Pirates.FRIENDLY_PIRATE_TYPE, FriendlyPirateRenderer::new);
        //EntityRendererRegistry.register(Pirates.SKELETON_PIRATE_ENTITY_TYPE, SkeletonPirateEntityRenderer::new);
        EntityRendererRegistry.register(Pirates.SHOT_ENTITY_TYPE, (context) -> new ShotEntityRenderer(context, 1,false));




        //BlockRenderLayerMap.INSTANCE.putBlock(Pirates.SHIP_ID_BLOCK, RenderType.translucent());
        //EntityModelLayerRegistry.registerModelLayer(SKELETON_PIRATE, SkeletonPirateModel::getTexturedModelData);
    }

    public static void initClient()
    {
        BlockEntityRendererRegistry.register(Pirates.CANNON_PRIMING_BLOCK_ENTITY.get(), CannonPrimingBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(Pirates.SHIP_ID_BLOCK_ENTITY.get(),ShipIdBlockEntityRenderer::new);
    }
}
