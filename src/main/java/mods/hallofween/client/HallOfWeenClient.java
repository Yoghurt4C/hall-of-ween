package mods.hallofween.client;

import me.shedaniel.cloth.api.client.events.v0.ClothClientHooks;
import mods.hallofween.Config;
import mods.hallofween.HallOfWeen;
import mods.hallofween.client.bags.BagHandler;
import mods.hallofween.client.bags.BagWidget;
import mods.hallofween.entity.ThrownRottenEgg;
import mods.hallofween.entity.ThrownToiletPaper;
import mods.hallofween.item.ContainerItem;
import mods.hallofween.network.S2CToTSyncMessage;
import mods.hallofween.registry.HallOfWeenEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.io.IOException;
import java.util.Collection;

import static mods.hallofween.HallOfWeen.L;
import static mods.hallofween.HallOfWeen.getId;

public class HallOfWeenClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BuiltinItemRendererRegistry.INSTANCE.register(HallOfWeen.getItem("testificate"), new TestificateRenderer());

        ColorProviderRegistry.ITEM.register(ContainerItem::getColor, HallOfWeen.getItem("container"));
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
        BuiltinItemRendererRegistry.INSTANCE.register(HallOfWeen.getItem("container"), new ContainerRenderer());
        ClientPlayNetworking.registerGlobalReceiver(S2CToTSyncMessage.MESSAGEID, S2CToTSyncMessage::receive);

        EntityRendererRegistry.INSTANCE.register(HallOfWeenEntities.ROTTEN_EGG, (
                (erd, ctx) -> new FlyingItemEntityRenderer<ThrownRottenEgg>(erd, ctx.getItemRenderer())));
        EntityRendererRegistry.INSTANCE.register(HallOfWeenEntities.TOILET_PAPER, (
                (erd, ctx) -> new FlyingItemEntityRenderer<ThrownToiletPaper>(erd, ctx.getItemRenderer())));

        ModelLoadingRegistry.INSTANCE.registerModelProvider((manager, out) -> out.accept(new ModelIdentifier(getId("recipe_sheet_base"), "inventory")));
        BuiltinItemRendererRegistry.INSTANCE.register(HallOfWeen.getItem("recipe_sheet"), new RecipeSheetRenderer());

        //todo
        if (Config.enableBagInventory) {
            ClothClientHooks.SCREEN_INIT_POST.register(((client, screen, screenHooks) -> {
                if (screen instanceof HandledScreen) {
                    BagHandler.widget = new BagWidget(Text.of("fuckyou"), client, (HandledScreen<?>) screen);
                    BagHandler.widget.init(client, 218, 97);
                    screenHooks.cloth$getChildren().add(BagHandler.widget);
                }
            }));

            ClothClientHooks.SCREEN_RENDER_POST.register((matrices, minecraftClient, screen, i, i1, v) -> {
                if (screen instanceof HandledScreen) {
                    BagHandler.widget.render(matrices, i, i1, v);
                }
            });
        }
    }
}
