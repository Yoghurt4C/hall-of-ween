package mods.hallofween.network;

import mods.hallofween.HallOfWeen;
import mods.hallofween.registry.ToTRegistry.ToTBagProperties;
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
import java.util.List;
import java.util.Map;

import static mods.hallofween.registry.ToTRegistry.BAGS;

public interface S2CToTSyncMessage {
    Identifier MESSAGEID = HallOfWeen.getId("sync_tot");

    static void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        BAGS.clear();
        int girth = buf.readInt();
        for (int i = 0; i < girth; i++) {
            String id = buf.readString();
            int bC = buf.readInt();
            int mC = buf.readInt();
            byte b = buf.readByte();
            if (b == 0)
                BAGS.put(id, new ToTBagProperties(bC, mC));
            else {
                int breadth = buf.readInt();
                List<String> tt = new ArrayList<>();
                for (int k = 0; k < breadth; k++) {
                    tt.add(buf.readString());
                }
                BAGS.put(id, new ToTBagProperties(bC, mC, tt));
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
        buf.writeInt(BAGS.size());
        for (Map.Entry<String, ToTBagProperties> es : BAGS.entrySet()) {
            buf.writeString(es.getKey());
            buf.writeInt(es.getValue().bagColor);
            buf.writeInt(es.getValue().magicColor);
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
