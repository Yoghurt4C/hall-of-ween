package mods.hallofween.network;

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
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

import java.util.ArrayList;
import java.util.List;

/**
 * Packet for full or partial Bag synchronization.
 * Specific slot sniping is handled in {@link BagSlotChangeMessage}.
 */
public class BagSyncMessage {
    public final static Identifier MESSAGEID = HallOfWeenUtil.getId("bag_sync");
    private final byte type;
    private final int bagSlot;
    private final List<ItemStack> contents;

    public BagSyncMessage(List<ItemStack> contents) {
        this.bagSlot = this.type = 0;
        this.contents = contents;
    }

    public BagSyncMessage(int bagSlot, List<ItemStack> section) {
        this.type = 1;
        this.bagSlot = bagSlot;
        this.contents = section;
    }

    private PacketByteBuf getBuf() {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeByte(this.type);
        switch (this.type) {
            case 0:
                buf.writeInt(this.contents.size());
                for (ItemStack stack : this.contents) {
                    buf.writeItemStack(stack);
                }
                break;
            case 1:
                buf.writeInt(this.bagSlot);
                buf.writeInt(this.contents.size());
                for (ItemStack stack : this.contents) {
                    buf.writeItemStack(stack);
                }
                break;
        }
        return buf;
    }

    public void send(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, MESSAGEID, getBuf());
    }

    public static void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        byte type = buf.readByte();
        int slot, size;
        List<ItemStack> list = new ArrayList<>();
        switch (type) {
            case 0:
                size = buf.readInt();
                for (int i = 0; i < size; i++) list.add(buf.readItemStack());
                DefaultedList<ItemStack> fin = DefaultedListAccessor.constructor(list, ItemStack.EMPTY);
                if (client.player == null) {
                    BagData.TEMP = fin;
                } else {
                    BagHandler.getBagHolder(client.player).setBagInventory(new BagInventory(fin));
                }
                if (BagData.WIDGET != null) BagData.WIDGET.init();
                break;
            case 1:
                slot = buf.readInt();
                size = buf.readInt();
                for (int i = 0; i < size; i++) list.add(buf.readItemStack());
                //BagInventory inv = holder.getBagInventory();
                break;
        }
    }
}
