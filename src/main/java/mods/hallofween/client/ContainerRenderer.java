package mods.hallofween.client;

import mods.hallofween.HallOfWeen;
import mods.hallofween.registry.ContainerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

import static mods.hallofween.registry.ContainerRegistry.CONTAINERS;

public class ContainerRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {
    public final ModelIdentifier MISSINGNO = new ModelIdentifier(HallOfWeen.getId("item/container/missingno"), "inventory");
    @Override
    public void render(ItemStack stack, ModelTransformation.Mode mode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        MinecraftClient mc = MinecraftClient.getInstance();
        RenderLayer layer = mode == ModelTransformation.Mode.FIXED ? TexturedRenderLayers.getEntityCutout() : RenderLayers.getItemLayer(stack, true);
        BakedModel model;
        if (stack.hasTag() && stack.getTag().contains("bagId")) {
            CompoundTag tag = stack.getTag();
            String id = tag.getString("bagId");
            model = mc.getBakedModelManager().getModel(CONTAINERS.get(id).modelId);
        } else {
            model = mc.getBakedModelManager().getModel(MISSINGNO);
        }
        mc.getBlockRenderManager().getModelRenderer().render(matrices.peek(), vertexConsumers.getBuffer(layer), null, model, 1f, 1f, 1f, light, overlay);
    }
}