package mods.hallofween.client;

import com.mojang.blaze3d.systems.RenderSystem;
import mods.hallofween.Config;
import mods.hallofween.mixin.client.ItemRendererAccessor;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.render.model.json.ModelTransformation.Mode;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.registry.Registry;

import static mods.hallofween.util.HallOfWeenUtil.getId;

public class RecipeSheetRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {

    private final ModelIdentifier sheetModel = new ModelIdentifier(getId("recipe_sheet_base"), "inventory");

    @Override
    public void render(ItemStack stack, Mode mode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        MinecraftClient mc = MinecraftClient.getInstance();
        BakedModel sheet = mc.getBakedModelManager().getModel(sheetModel);
        VertexConsumer vc = vertexConsumers.getBuffer(RenderLayers.getItemLayer(stack, true));
        ItemRendererAccessor ir = (ItemRendererAccessor) mc.getItemRenderer();
        ir.renderModel(sheet, stack, light, overlay, matrices, vc);
        if (stack.hasTag() && stack.getTag().contains("targetItem")) {
            Item item = Registry.ITEM.get(new Identifier(stack.getTag().getString("targetItem")));
            ItemStack render = new ItemStack(item);
            matrices.push();
            BakedModel model = mc.getItemRenderer().getHeldItemModel(render, null, null);
            if (Config.faithfulRecipeSheets) {
                RenderSystem.disableLighting();
                matrices.scale(0.45f, 0.45f, 0.01f);
                matrices.translate(1f, 0.93f, 53.5f);
                mc.getItemRenderer().renderItem(new ItemStack(item), ModelTransformation.Mode.GUI, false, matrices, vertexConsumers, 0, overlay, model);
                RenderSystem.enableLighting();
                matrices.pop();
            } else {
                if (mode != Mode.GUI) {
                    matrices.peek().getModel().multiply(Matrix4f.scale(0.45f, 0.45f, 0.01f));
                    Quaternion quaternion;
                    if (model.isSideLit()) {
                        quaternion = new Quaternion(Vector3f.POSITIVE_X, -15f, true);
                        quaternion.hamiltonProduct(new Quaternion(Vector3f.POSITIVE_Y, 45f, true));
                        matrices.peek().getNormal().multiply(quaternion);
                    } else {
                        quaternion = new Quaternion(Vector3f.POSITIVE_X, -5f, true);
                        matrices.peek().getNormal().multiply(quaternion);
                    }
                } else {
                    matrices.scale(0.45f, 0.45f, 0.01f);
                }
                matrices.translate(1f, 0.93f, 53.5f);
                mc.getItemRenderer().renderItem(new ItemStack(item), ModelTransformation.Mode.GUI, false, matrices, vertexConsumers, light, overlay, model);
                matrices.pop();
            }
        }
    }
}
