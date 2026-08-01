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
import net.minecraft.client.render.RenderLayer;
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
        BlockEntityRendererFactories.register(Pirates.SHIP_ID_BLOCK_ENTITY, ShipIdBlockEntityRenderer::new);
        BlockRenderLayerMap.INSTANCE.putBlock(Pirates.SHIP_ID_BLOCK, RenderLayer.getTranslucent());

        //EntityModelLayerRegistry.registerModelLayer(SKELETON_PIRATE, SkeletonPirateModel::getTexturedModelData);


        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(
                Pirates.CANNON_SMOKE_PACKET_ID,
                (client, handler, buf, sender) -> {
                    double x = buf.readDouble();
                    double y = buf.readDouble();
                    double z = buf.readDouble();
                    int dirId = buf.readInt();

                    client.execute(() -> {
                        var world = client.world;
                        if (world == null) return;

                        var direction = net.minecraft.util.math.Direction.byId(dirId);
                        for (int i = 0; i < 40; ++i) {
                            world.addParticle(net.minecraft.particle.ParticleTypes.FLAME,
                                    x + direction.getOffsetX() * (0.5 + world.random.nextDouble() * 1.5),
                                    y + direction.getOffsetY() + (world.random.nextDouble() * 1.0) - 0.5,
                                    z + direction.getOffsetZ() * (0.5 + world.random.nextDouble() * 1.5),
                                    (world.random.nextDouble() * 0.3) - 0.15,
                                    (world.random.nextDouble() * 0.1) - 0.05,
                                    (world.random.nextDouble() * 0.3) - 0.15);

                            world.addParticle(net.minecraft.particle.ParticleTypes.CLOUD,
                                    x + direction.getOffsetX() * (3.5 + world.random.nextDouble() * 2) + (4 * world.random.nextDouble()) - 2,
                                    y + (world.random.nextDouble() * 3.0) - 1.0,
                                    z + direction.getOffsetZ() * (3.5 + world.random.nextDouble() * 2) + (4 * world.random.nextDouble()) - 2,
                                    0.0, 0.0, 0.0);
                        }
                    });
                }
        );
    }
}
