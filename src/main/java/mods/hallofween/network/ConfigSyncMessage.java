package mods.hallofween.network;

import mods.hallofween.Config;
import mods.hallofween.bags.BagHandler;
import mods.hallofween.bags.BagInventory;
import mods.hallofween.client.bags.BagData;
import mods.hallofween.mixin.bags.DefaultedListAccessor;
import mods.hallofween.util.HallOfWeenUtil;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

import java.util.ArrayList;
import java.util.List;

public class ConfigSyncMessage {
    public static Identifier MESSAGEID = HallOfWeenUtil.getId("config_sync");

    private static PacketByteBuf getBuf() {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(Config.maxBagInventorySize);
        buf.writeInt(Config.startingBagInventorySize);
        return buf;
    }

    public static void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        Config.maxBagInventorySize = buf.readInt();
        Config.startingBagInventorySize = buf.readInt();
    }

    public static void send(ServerPlayNetworkHandler handler, PacketSender packetSender, MinecraftServer minecraftServer) {
        ServerPlayNetworking.send(handler.player, MESSAGEID, getBuf());
    }
}
