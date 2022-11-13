package mods.hallofween.client;

import mods.hallofween.mixin.client.ItemRendererAccessor;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

import static mods.hallofween.registry.ContainerRegistry.CONTAINERS;

public class ContainerRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {
    public static BakedModel MISSINGNO;

    @Override
    public void render(ItemStack stack, ModelTransformation.Mode mode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        MinecraftClient mc = MinecraftClient.getInstance();
        RenderLayer layer = RenderLayers.getItemLayer(stack, true);
        BakedModel model = MISSINGNO;
        if (stack.hasTag() && stack.getTag().contains("bagId")) {
            CompoundTag tag = stack.getTag();
            String id = tag.getString("bagId");
            if (CONTAINERS.containsKey(id)) {
                model = mc.getBakedModelManager().getModel(new ModelIdentifier(CONTAINERS.get(id).modelId, "inventory"));
            }
        }
        ItemRendererAccessor ir = (ItemRendererAccessor) mc.getItemRenderer();
        ir.renderModel(model, stack, light, overlay, matrices, vertexConsumers.getBuffer(layer));
    }
}
