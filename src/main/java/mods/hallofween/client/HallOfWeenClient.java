package mods.hallofween.client;

import mods.hallofween.entity.ThrownRottenEgg;
import mods.hallofween.entity.ThrownToiletPaper;
import mods.hallofween.item.TrickOrTreatBagItem;
import mods.hallofween.network.S2CToTSyncMessage;
import mods.hallofween.registry.HallOfWeenEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.util.registry.Registry;

import static mods.hallofween.HallOfWeen.getId;

public class HallOfWeenClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BuiltinItemRendererRegistry.INSTANCE.register(Registry.ITEM.get(getId("testificate")), new TestificateRenderer());

        ColorProviderRegistry.ITEM.register(TrickOrTreatBagItem::getColor, Registry.ITEM.get(getId("trick_or_treat_bag")));
        ClientPlayNetworking.registerGlobalReceiver(S2CToTSyncMessage.MESSAGEID, S2CToTSyncMessage::receive);

        EntityRendererRegistry.INSTANCE.register(HallOfWeenEntities.ROTTEN_EGG, (
                (erd, ctx) -> new FlyingItemEntityRenderer<ThrownRottenEgg>(erd, ctx.getItemRenderer())));
        EntityRendererRegistry.INSTANCE.register(HallOfWeenEntities.TOILET_PAPER, (
                (erd, ctx) -> new FlyingItemEntityRenderer<ThrownToiletPaper>(erd, ctx.getItemRenderer())));
    }
}
