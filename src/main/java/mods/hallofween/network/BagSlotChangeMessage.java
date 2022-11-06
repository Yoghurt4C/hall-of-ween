package mods.hallofween.network;

import mods.hallofween.bags.BagHolder;
import mods.hallofween.bags.BagInventory;
import mods.hallofween.client.bags.BagData;
import mods.hallofween.util.HallOfWeenUtil;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class BagSlotChangeMessage {
    public static Identifier MESSAGEID = HallOfWeenUtil.getId("bag_sync");
    public int bag;
    public int slot;
    public ItemStack stack;

    public BagSlotChangeMessage(int bag, int slot, ItemStack cursorStack) {
        this.bag = bag;
        this.slot = slot;
        this.stack = cursorStack;
    }

    private PacketByteBuf getBuf() {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(bag);
        buf.writeInt(slot);
        buf.writeItemStack(stack);
        return buf;
    }

    public void send() {
        ClientPlayNetworking.send(MESSAGEID, getBuf());
    }

    public static void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        BagHolder holder = BagData.getBagHolder(player);
    }
}
