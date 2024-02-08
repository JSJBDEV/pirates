package ace.actually.pirates.entities.legacy;

import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;

// Made with Blockbench 4.5.2
// Exported for Minecraft version 1.17+ for Yarn
// Paste this class into your mod and generate all required imports
public class PirateShipModel extends EntityModel<PirateShipEntity> {
	private final ModelPart bb_main;

	public PirateShipModel(ModelPart root) {
		this.bb_main = root.getChild("bb_main");
	}




	public static TexturedModelData getTexturedModelData() {
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		ModelPartData bb_main = modelPartData.addChild("bb_main", ModelPartBuilder.create().uv(0, 0).cuboid(-8.0F, -2.0F, -48.0F, 16.0F, 2.0F, 80.0F, new Dilation(0.0F))
		.uv(0, 0).cuboid(-10.0F, -4.0F, -48.0F, 20.0F, 2.0F, 80.0F, new Dilation(0.0F))
		.uv(0, 0).cuboid(-10.0F, -4.0F, -48.0F, 20.0F, 2.0F, 80.0F, new Dilation(0.0F))
		.uv(0, 0).cuboid(-10.0F, -17.0F, 30.0F, 19.0F, 14.0F, 2.0F, new Dilation(0.0F))
		.uv(0, 0).cuboid(-10.0F, -17.0F, -47.0F, 19.0F, 14.0F, 2.0F, new Dilation(0.0F))
		.uv(0, 0).cuboid(-3.0F, -56.0F, -30.0F, 6.0F, 53.0F, 6.0F, new Dilation(0.0F))
		.uv(0, 0).cuboid(-16.0F, -56.0F, -35.0F, 31.0F, 29.0F, 6.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 24.0F, 0.0F));

		ModelPartData cube_r1 = bb_main.addChild("cube_r1", ModelPartBuilder.create().uv(0, 0).cuboid(5.0F, 7.0F, -48.0F, 14.0F, 2.0F, 80.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -1.3526F));

		ModelPartData cube_r2 = bb_main.addChild("cube_r2", ModelPartBuilder.create().uv(0, 0).cuboid(5.0F, -9.0F, -48.0F, 14.0F, 2.0F, 80.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -1.8326F));
		return TexturedModelData.of(modelData, 128, 128);
	}
	@Override
	public void setAngles(PirateShipEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
	}
	@Override
	public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
		bb_main.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
	}
}