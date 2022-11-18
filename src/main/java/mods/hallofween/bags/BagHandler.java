package mods.hallofween.bags;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import mods.hallofween.mixin.bags.DefaultedListAccessor;
import mods.hallofween.util.ItemStackSerializer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface BagHandler {
    static BagHolder getBagHolder(PlayerEntity player) {
        return ((BagHolder) player.inventory);
    }
    static void loadPlayerData(JsonArray data, ServerPlayerEntity player) {
        BagInventory inv = new BagInventory();
        int i = 0;
        for (JsonElement e : data) {
            if (e.isJsonObject()) {
                JsonObject obj = e.getAsJsonObject();
                ItemStack bag = ItemStackSerializer.fromJson(obj.get("bag"));
                if (bag.isEmpty()) continue;
                inv.setBag(i, bag, null);
                int size = inv.getBagSize(bag);
                JsonElement ce = obj.get("contents");
                if (ce.isJsonObject()) {
                    JsonObject co = ce.getAsJsonObject();
                    List<ItemStack> view = inv.getBagView(i);
                    for (Map.Entry<String, JsonElement> kv : co.entrySet()) {
                        int slot = Integer.parseInt(kv.getKey());
                        ItemStack stack = ItemStackSerializer.fromJson(kv.getValue());
                        if (slot < size) view.set(slot, stack);
                    }
                }
                i++;
            }
        }
        getBagHolder(player).setBagInventory(inv);
    }

    static JsonElement getPlayerData(ServerPlayerEntity player) {
        JsonArray arr = new JsonArray();
        BagHolder holder = ((BagHolder) player.inventory);
        BagInventory inv = holder.getBagInventory();
        if (inv.isEmpty()) return JsonNull.INSTANCE;
        for (int i = 0; i < inv.getNonEmptyBags().size(); i++) {
            JsonObject obj = new JsonObject();
            obj.add("bag", ItemStackSerializer.toJson(inv.getStack(i)));
            JsonObject contents = new JsonObject();
            int size = inv.getBagSize(i);
            List<ItemStack> view = inv.getBagView(i);
            for (int k = 0; k < size; k++) {
                ItemStack cont = view.get(k);
                if (!cont.isEmpty()) contents.add(String.valueOf(k), ItemStackSerializer.toJson(cont));
            }
            obj.add("contents", contents);
            arr.add(obj);
        }
        return arr;
    }

    static DefaultedList<ItemStack> getInitialInventory() {
        List<ItemStack> bags = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            bags.add(ItemStack.EMPTY);
        }
        return DefaultedListAccessor.constructor(bags, ItemStack.EMPTY);
    }

    static ItemStack setCursorStack(PlayerEntity player, ItemStack stack) {
        PlayerInventory inv = player.inventory;
        if (inv.getCursorStack().isEmpty()) {
            player.inventory.setCursorStack(stack);
            return ItemStack.EMPTY;
        }
        return stack;
    }

    static ItemStack putIntoSlot(Slot slot, ItemStack stack, PlayerEntity player) {
        if (slot.hasStack()) {
            if (ScreenHandler.canStacksCombine(slot.getStack(), stack)) {
                int slotCount = slot.getStack().getCount();
                int stackCount = stack.getCount();
                slot.getStack().setCount(Math.max(slotCount + stackCount, slot.getStack().getMaxCount()));
                stack.decrement(slot.getStack().getMaxCount() - slotCount);
                return stack;
            } else {
                slot.setStack(stack);
                return ItemStack.EMPTY;
            }
        } else {
            slot.setStack(stack);
            return ItemStack.EMPTY;
        }
    }
}
