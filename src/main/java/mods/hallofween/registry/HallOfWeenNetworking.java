package mods.hallofween.registry;

import mods.hallofween.Config;
import mods.hallofween.bags.BagHandler;
import mods.hallofween.data.PlayerDataManager;
import mods.hallofween.network.*;
import mods.hallofween.util.HallOfWeenUtil;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static mods.hallofween.util.HallOfWeenUtil.L;

public class HallOfWeenNetworking {
    public static void init() {
        ServerPlayConnectionEvents.JOIN.register(S2CContainerSyncMessage::send);
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register(S2CContainerSyncMessage::send);

        ServerPlayConnectionEvents.JOIN.register(S2CSheetSyncMessage::send);
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register(S2CSheetSyncMessage::send);

        ServerPlayConnectionEvents.INIT.register((handler, server) -> {
            ServerPlayNetworking.registerReceiver(handler, PlayerDataSyncMessage.MESSAGEID, PlayerDataSyncMessage::receive);
            if (Config.enableBagInventory) {
                ServerPlayNetworking.registerReceiver(handler, BagSlotChangeMessage.MESSAGEID, BagSlotChangeMessage::receive);
            }
        });
        if (Config.enableBagInventory) {
            ServerPlayConnectionEvents.JOIN.register(((handler, sender, server) -> {
                Path path = HallOfWeenUtil.getDataPath(server);
                try {
                    if (Files.notExists(path)) Files.createDirectory(path);
                    if (PlayerDataManager.loadPlayerData(path, handler.player)) {
                        new PlayerDataSyncMessage(PlayerDataManager.DATA.get(handler.player.getUuid())).send(handler.player);
                        new BagSyncMessage(BagHandler.getBagHolder(handler.player).getBagInventory().contents).send(handler.player);
                    }
                } catch (IOException e) {
                    L.warn("[Hall of Ween] Couldn't get the gw2 data folder within the world folder!");
                    L.error(e.getMessage());
                }
            }));
        }
    }

    public static void initClient() {
        ClientPlayNetworking.registerGlobalReceiver(S2CContainerSyncMessage.MESSAGEID, S2CContainerSyncMessage::receive);
        ClientPlayNetworking.registerGlobalReceiver(S2CSheetSyncMessage.MESSAGEID, S2CSheetSyncMessage::receive);

        ClientPlayConnectionEvents.INIT.register((handler, client) -> {
            ClientPlayNetworking.registerReceiver(PlayerDataSyncMessage.MESSAGEID, PlayerDataSyncMessage::receive);

            if (Config.enableBagInventory) {
                ClientPlayNetworking.registerReceiver(BagSlotChangeMessage.MESSAGEID, BagSlotChangeMessage::receive);
                ClientPlayNetworking.registerReceiver(BagSyncMessage.MESSAGEID, BagSyncMessage::receive);
            }
        });
    }
}
