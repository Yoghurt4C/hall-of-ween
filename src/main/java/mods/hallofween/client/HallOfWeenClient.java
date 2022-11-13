package mods.hallofween.client;

import me.shedaniel.cloth.api.client.events.v0.ClothClientHooks;
import mods.hallofween.Config;
import mods.hallofween.client.bags.BagData;
import mods.hallofween.client.bags.BagWidget;
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
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
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
            ClothClientHooks.SCREEN_INIT_POST.register(((client, screen, screenHooks) -> {
                if (screen instanceof HandledScreen) {
                    BagData.widget = new BagWidget(Text.of("fuckyou"), client, (HandledScreen<?>) screen);
                    BagData.widget.init(client, 218, 97);
                    screenHooks.cloth$getChildren().add(BagData.widget);
                }
            }));

            ClothClientHooks.SCREEN_RENDER_POST.register((matrices, minecraftClient, screen, i, i1, v) -> {
                if (screen instanceof HandledScreen) {
                    BagData.widget.render(matrices, i, i1, v);
                }
            });

            ClothClientHooks.SCREEN_MOUSE_CLICKED.register((minecraftClient, screen, v, v1, i) -> {
                if (screen instanceof HandledScreen && BagData.widget.mouseClicked(v, v1, i)) {
                    screen.setFocused(BagData.widget);
                    if (i == 0) screen.setDragging(true);
                    return ActionResult.SUCCESS;
                }
                screen.setFocused(null);
                return ActionResult.PASS;
            });
            /*
            ClothClientHooks.SCREEN_MOUSE_DRAGGED.register((minecraftClient, screen, v, v1, i, v2, v3) -> {
                if (screen instanceof HandledScreen && BagData.widget.mouseDragged(v, v1, i, v2, v3)) {
                    return ActionResult.SUCCESS;
                }
                return ActionResult.PASS;
            });
            ClothClientHooks.SCREEN_MOUSE_RELEASED.register((minecraftClient, screen, v, v1, i) -> {
                if (screen instanceof HandledScreen && BagData.widget.mouseReleased(v, v1, i)) {
                    return ActionResult.SUCCESS;
                }
                return ActionResult.PASS;
            });
             */
        }
    }
}
