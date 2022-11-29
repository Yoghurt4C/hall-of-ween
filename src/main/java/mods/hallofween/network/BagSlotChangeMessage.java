package mods.hallofween.network;

import mods.hallofween.bags.BagHandler;
import mods.hallofween.bags.BagHolder;
import mods.hallofween.client.bags.BagData;
import mods.hallofween.util.HallOfWeenUtil;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
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

public class BagSlotChangeMessage {
    public static Identifier MESSAGEID = HallOfWeenUtil.getId("bag_slot_sync");
    private final byte type;
    private final int slot;
    private final ItemStack stack, cursor;

    public BagSlotChangeMessage(int slot) {
        this.type = 0;
        this.slot = slot;
        this.stack = null;
        this.cursor = null;
    }

    public BagSlotChangeMessage(int slot, ItemStack stack) {
        this.type = 1;
        this.slot = slot;
        this.stack = stack;
        this.cursor = null;
    }

    public BagSlotChangeMessage(int slot, ItemStack stack, ItemStack cursorStack) {
        this.type = 2;
        this.slot = slot;
        this.stack = stack;
        this.cursor = cursorStack;
    }

    private PacketByteBuf getBuf() {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeByte(type);
        buf.writeInt(slot);
        switch (type) {
            case 1:
                buf.writeItemStack(stack);
                break;
            case 2:
                buf.writeItemStack(stack);
                buf.writeItemStack(cursor);
                break;
            default:
                break;
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
        BagHolder holder = BagHandler.getBagHolder(client.player);
        byte type = buf.readByte();
        int slot = buf.readInt();
        ItemStack stack, cursor;
        if (type == 2) {
            stack = buf.readItemStack();
            cursor = buf.readItemStack();
            holder.getBagInventory().resolveSetStack(slot, stack, client.player);
            client.player.inventory.setCursorStack(cursor);
            if (slot < 10) {
                BagData.WIDGET.updateContents();
            } else if (stack.getItem() != cursor.getItem()) {
                if (stack.isEmpty()) BagData.WIDGET.stackCount--;
                else if (cursor.isEmpty()) BagData.WIDGET.stackCount++;
            }
        }
    }

    public static void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        BagHolder holder = BagHandler.getBagHolder(player);
        byte type = buf.readByte();
        int slot = buf.readInt();
        ItemStack stack, cursor;
        if (type == 0) {
            stack = holder.getBagInventory().getStack(slot).copy();
            cursor = player.inventory.getCursorStack().copy();
            slot = holder.getBagInventory().resolveSetStack(slot, cursor, player);
            player.inventory.setCursorStack(stack);

            new BagSlotChangeMessage(slot, holder.getBagInventory().getStack(slot), player.inventory.getCursorStack()).send(player);
        }
    }
}
