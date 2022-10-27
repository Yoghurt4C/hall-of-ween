package mods.hallofween.network;

import mods.hallofween.HallOfWeen;
import mods.hallofween.registry.ContainerRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
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

public interface S2CContainerSyncMessage {
    Identifier MESSAGEID = HallOfWeen.getId("sync_containers");

    static PacketByteBuf getBuf() {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(CONTAINERS.size());
        for (Map.Entry<String, ContainerRegistry.ContainerProperties> es : CONTAINERS.entrySet()) {
            buf.writeString(es.getKey());
            buf.writeString(es.getValue().name);
            Identifier modelId = es.getValue().modelId;
            if (modelId == null) {
                buf.writeBoolean(false);
            } else {
                buf.writeBoolean(true);
                buf.writeString(es.getValue().modelId.toString());
            }
            buf.writeInt(es.getValue().bagColor);
            buf.writeInt(es.getValue().overlayColor);
            List<String> tooltips = es.getValue().tooltips;
            if (tooltips == null) {
                buf.writeBoolean(false);
            } else {
                buf.writeBoolean(true);
                buf.writeInt(tooltips.size());
                for (String s : tooltips) {
                    buf.writeString(s);
                }
            }
        }

        return buf;
    }

    static void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        CONTAINERS.clear();
        int girth = buf.readInt();
        for (int i = 0; i < girth; i++) {
            String id = buf.readString();
            String name = buf.readString();
            Identifier modelId = null;
            List<String> tooltips = Collections.emptyList();
            if (buf.readBoolean()) {
                modelId = new Identifier(buf.readString());
            }
            int bC = buf.readInt();
            int mC = buf.readInt();
            if (buf.readBoolean()) {
                int breadth = buf.readInt();
                List<String> tt = new ArrayList<>();
                for (int k = 0; k < breadth; k++) {
                    tt.add(buf.readString());
                }
                tooltips = tt;
            }
            CONTAINERS.put(id, new ContainerRegistry.ContainerProperties(name, modelId, bC, mC, tooltips));
        }
    }

    static void send(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) {
        ServerPlayNetworking.send(handler.player, MESSAGEID, getBuf());
    }

    static void send(MinecraftServer server, ServerResourceManager serverResourceManager, boolean success) {
        PacketByteBuf buf = getBuf();
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList())
            ServerPlayNetworking.send(player, MESSAGEID, buf);
    }
}
