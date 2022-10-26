package mods.hallofween.network;

import mods.hallofween.HallOfWeen;
import mods.hallofween.registry.ContainerRegistry.ContainerProperties;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.resource.ServerResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static mods.hallofween.registry.ContainerRegistry.CONTAINERS;

public interface S2CToTSyncMessage {
    Identifier MESSAGEID = HallOfWeen.getId("sync_tot");

    static void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        CONTAINERS.clear();
        int girth = buf.readInt();
        for (int i = 0; i < girth; i++) {
            String id = buf.readString();
            ModelIdentifier modelId = new ModelIdentifier(buf.readString());
            int bC = buf.readInt();
            int mC = buf.readInt();
            byte b = buf.readByte();
            if (b == 0)
                CONTAINERS.put(id, new ContainerProperties(modelId, bC, mC, Collections.emptyList()));
            else {
                int breadth = buf.readInt();
                List<String> tt = new ArrayList<>();
                for (int k = 0; k < breadth; k++) {
                    tt.add(buf.readString());
                }
                CONTAINERS.put(id, new ContainerProperties(modelId, bC, mC, tt));
            }
        }
    }

    static void send(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) {
        ServerPlayNetworking.send(handler.player, S2CToTSyncMessage.MESSAGEID, getBuf());
    }

    static void send(MinecraftServer server, ServerResourceManager serverResourceManager, boolean success) {
        PacketByteBuf buf = getBuf();
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList())
            ServerPlayNetworking.send(player, S2CToTSyncMessage.MESSAGEID, buf);
    }

    static PacketByteBuf getBuf() {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(CONTAINERS.size());
        for (Map.Entry<String, ContainerProperties> es : CONTAINERS.entrySet()) {
            buf.writeString(es.getKey());
            buf.writeString(es.getValue().modelId.toString());
            buf.writeInt(es.getValue().bagColor);
            buf.writeInt(es.getValue().overlayColor);
            List<String> tooltips = es.getValue().tooltips;
            if (tooltips == null) {
                buf.writeByte(0);
            } else {
                buf.writeByte(1);
                buf.writeInt(tooltips.size());
                for (String s : tooltips) {
                    buf.writeString(s);
                }
            }
        }

        return buf;
    }
}
