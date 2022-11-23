package mods.hallofween.network;

import it.unimi.dsi.fastutil.ints.IntArraySet;
import mods.hallofween.client.HallOfWeenClient;
import mods.hallofween.data.PlayerData;
import mods.hallofween.data.PlayerDataManager;
import mods.hallofween.util.HallOfWeenUtil;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Set;

public class PlayerDataSyncMessage extends PlayerData {
    public static Identifier MESSAGEID = HallOfWeenUtil.getId("player_data_sync");

    public PlayerDataSyncMessage(PlayerData data) {
        super(data.bagSlots, data.hasExpansions, data.titles, data.customTitles);
    }

    public PlayerDataSyncMessage(int bagSlots, boolean hasExpansions, int[] titles, int[] customTitles) {
        super(bagSlots, hasExpansions, titles, customTitles);
    }

    private PacketByteBuf getBuf() {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(bagSlots);
        buf.writeBoolean(hasExpansions);
        buf.writeInt(titles.length);
        if (titles.length > 0) {
            for (int id : titles) buf.writeInt(id);
        }

        buf.writeInt(customTitles.length);
        if (customTitles.length > 0) {
            for (int id : customTitles) buf.writeInt(id);
        }
        return buf;
    }

    public void send() {
        ClientPlayNetworking.send(MESSAGEID, getBuf());
    }

    public void send(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, MESSAGEID, getBuf());
    }

    public static void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        PlayerData data = fromBuf(buf);
        HallOfWeenClient.CLIENT_DATA = data;
    }

    public static void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        PlayerData serverData = PlayerDataManager.DATA.get(player.getUuid());
        PlayerData clientData = fromBuf(buf);
        int bagSlots = Math.max(serverData.bagSlots, clientData.bagSlots);
        boolean hasExpacs = clientData.hasExpansions || serverData.hasExpansions;
        Set<Integer> mergedTitles = new IntArraySet();
        for (int id : serverData.titles) mergedTitles.add(id);
        for (int id : clientData.titles) mergedTitles.add(id);
        int[] merged = new int[mergedTitles.size()];
        int i = 0;
        for (Integer id : mergedTitles) {
            merged[i] = id;
            i++;
        }
        //custom titles aren't updated clientside so f*ck em
        PlayerDataManager.DATA.put(player.getUuid(), new PlayerData(bagSlots, hasExpacs, merged, serverData.customTitles));
        //sync freshly updated data back to the client
        new PlayerDataSyncMessage(PlayerDataManager.DATA.get(player.getUuid())).send(player);
    }

    private static PlayerData fromBuf(PacketByteBuf buf) {
        int bagSlots = buf.readInt();
        boolean hasExpansions = buf.readBoolean();
        int[] official = PlayerDataManager.EMPTY_TITLES, custom = official;
        int size = buf.readInt();
        if (size > 0) {
            official = new int[size];
            for (int i = 0; i < size; i++) {
                official[i] = buf.readInt();
            }
        }
        size = buf.readInt();
        if (size > 0) {
            custom = new int[size];
            for (int i = 0; i < size; i++) {
                custom[i] = buf.readInt();
            }
        }
        return new PlayerData(bagSlots, hasExpansions, official, custom);
    }
}
