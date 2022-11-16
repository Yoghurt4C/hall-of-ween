package mods.hallofween.client;

import mods.hallofween.Config;
import mods.hallofween.client.bags.BagInitializer;
import mods.hallofween.entity.ThrownRottenEgg;
import mods.hallofween.entity.ThrownToiletPaper;
import mods.hallofween.events.ResourceReloadEvents;
import mods.hallofween.item.ContainerItem;
import mods.hallofween.registry.HallOfWeenEntities;
import mods.hallofween.registry.HallOfWeenNetworking;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;

import java.util.Collection;

import static mods.hallofween.util.HallOfWeenUtil.getId;
import static mods.hallofween.util.HallOfWeenUtil.getItem;

public class HallOfWeenClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BuiltinItemRendererRegistry.INSTANCE.register(getItem("testificate"), new TestificateRenderer());

        ColorProviderRegistry.ITEM.register(ContainerItem::getColor, getItem("container"));
        ModelLoadingRegistry.INSTANCE.registerModelProvider(((manager, out) -> {
            Collection<Identifier> models = manager.findResources("models/item/container", s -> s.endsWith(".json"));
            for (Identifier model : models) {
                String path = model.getPath();
                String namespace = model.getNamespace();
                path = path.substring(12, path.length() - 5);
                ModelIdentifier id = new ModelIdentifier(namespace + ":" + path, "inventory");
                out.accept(id);
            }
        }));
        BuiltinItemRendererRegistry.INSTANCE.register(getItem("container"), new ContainerRenderer());
        HallOfWeenNetworking.initClient();

        EntityRendererRegistry.INSTANCE.register(HallOfWeenEntities.ROTTEN_EGG, (
                (erd, ctx) -> new FlyingItemEntityRenderer<ThrownRottenEgg>(erd, ctx.getItemRenderer())));
        EntityRendererRegistry.INSTANCE.register(HallOfWeenEntities.TOILET_PAPER, (
                (erd, ctx) -> new FlyingItemEntityRenderer<ThrownToiletPaper>(erd, ctx.getItemRenderer())));

        ModelLoadingRegistry.INSTANCE.registerModelProvider((manager, out) -> out.accept(new ModelIdentifier(getId("recipe_sheet_base"), "inventory")));
        BuiltinItemRendererRegistry.INSTANCE.register(getItem("recipe_sheet"), new RecipeSheetRenderer());

        ResourceReloadEvents.FINISH.register((models, loader, manager) -> {
            ContainerRenderer.MISSINGNO = models.getModelManager().getModel(new ModelIdentifier(getId("container/missingno"), "inventory"));
            RecipeSheetRenderer.sheetModel = models.getModelManager().getModel(new ModelIdentifier(getId("recipe_sheet_base"), "inventory"));
        });

        //todo
        if (Config.enableBagInventory) {
            BagInitializer.init();
        }
    }
}
