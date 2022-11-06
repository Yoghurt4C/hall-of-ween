package mods.hallofween.registry;

import mods.hallofween.network.S2CContainerSyncMessage;
import mods.hallofween.network.S2CSheetSyncMessage;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class HallOfWeenNetworking {
    public static void init() {
        ServerPlayConnectionEvents.JOIN.register(S2CContainerSyncMessage::send);
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register(S2CContainerSyncMessage::send);

        ServerPlayConnectionEvents.JOIN.register(S2CSheetSyncMessage::send);
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register(S2CSheetSyncMessage::send);
    }

    public static void initClient() {
        ClientPlayNetworking.registerGlobalReceiver(S2CContainerSyncMessage.MESSAGEID, S2CContainerSyncMessage::receive);
        ClientPlayNetworking.registerGlobalReceiver(S2CSheetSyncMessage.MESSAGEID, S2CSheetSyncMessage::receive);
    }
}
