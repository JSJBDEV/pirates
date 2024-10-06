package ace.actually.pirates.entities.pirate_skeleton;



import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

public class SkeletonPirateModelLayer {
    public static final EntityModelLayer SKELETON_PIRATE = new EntityModelLayer(new Identifier("pirates", "skeleton_pirate"), "main");

    public static void registerModelLayers() {
        EntityModelLayerRegistry.registerModelLayer(SKELETON_PIRATE, SkeletonPirateModel::getTexturedModelData);
    }
}