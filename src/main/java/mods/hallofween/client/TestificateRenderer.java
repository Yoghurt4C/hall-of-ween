package mods.hallofween.client;

import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.VillagerResemblingModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class TestificateRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {
    private static final Identifier TEXTURE = new Identifier("textures/entity/villager/villager.png");
    private static final VillagerResemblingModel<VillagerEntity> model = new VillagerResemblingModel<>(1f);

    @Override
    public void render(ItemStack stack, ModelTransformation.Mode mode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        model.render(matrices, vertexConsumers.getBuffer(RenderLayer.getEntityCutout(TEXTURE)), light, overlay, 1f, 1f, 1f, 1f);
    }
}
