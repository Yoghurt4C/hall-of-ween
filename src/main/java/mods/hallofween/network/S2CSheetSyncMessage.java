package mods.hallofween.network;

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

import java.util.Map;

import static mods.hallofween.HallOfWeen.DISCOVERY;
import static mods.hallofween.util.HallOfWeenUtil.getId;

public interface S2CSheetSyncMessage {
    Identifier MESSAGEID = getId("sync_sheets");

    static PacketByteBuf getBuf() {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(DISCOVERY.size());
        for (Map.Entry<Identifier, Identifier> e : DISCOVERY.entrySet()) {
            buf.writeIdentifier(e.getKey());
            buf.writeIdentifier(e.getValue());
        }
        return buf;
    }

    static void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        DISCOVERY.clear();
        int girth = buf.readInt();
        for (int i = 0; i < girth; i++) {
            Identifier k = buf.readIdentifier();
            Identifier v = buf.readIdentifier();
            DISCOVERY.put(k, v);
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
