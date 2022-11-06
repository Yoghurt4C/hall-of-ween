package mods.hallofween.bags;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import mods.hallofween.item.BagItem;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class BagInventory implements Inventory {
    public DefaultedList<ItemStack> contents;

    public BagInventory() {
        this.contents = DefaultedList.ofSize(10, ItemStack.EMPTY);
    }

    public ItemStack addBag(int slot, ItemStack bag) {
        ItemStack stack = contents.get(slot);
        if (bag.getItem() instanceof BagItem) {
            BagItem item = (BagItem) bag.getItem();
            this.contents.set(slot, bag);
            int pointer = getBagContentsStart(slot);
            if (pointer > contents.size()) {
                contents.addAll(DefaultedList.ofSize(getBagSize(bag), ItemStack.EMPTY));
            } else {
                if (stack.getItem() instanceof BagItem && pointer + getBagSize(stack) <= contents.size()) {
                    List<ItemStack> list = DefaultedList.ofSize(getBagSize(bag), ItemStack.EMPTY);
                    List<ItemStack> sub = contents.subList(pointer, pointer + getBagSize(stack));
                    Collections.copy(list, sub);
                    sub.clear();
                    contents.addAll(pointer, list);
                }
            }
            return ItemStack.EMPTY;
        }
        return stack;
    }

    private int getBagContentsStart(int slot) {
        int pointer = 10;
        for (int i = 0; i < slot; i++) {
            ItemStack stack = contents.get(i);
            if (!stack.isEmpty() && stack.getItem() instanceof BagItem) {
                BagItem bi = (BagItem) stack.getItem();
                pointer += bi.getSlotCount();
            } else break;
        }
        return pointer;
    }

    private int getBagSize(int slot) {
        ItemStack stack = this.contents.get(slot);
        return getBagSize(stack);
    }

    private int getBagSize(ItemStack stack) {
        if (!stack.isEmpty() && stack.getItem() instanceof BagItem) {
            BagItem bi = (BagItem) stack.getItem();
            return bi.getSlotCount();
        }
        return 0;
    }

    public void removeBag(ItemStack stack) {
    }

    //valid = not full
    public int getFirstValidBagSlot() {
        for (int i = 0; i < 10; i++) {
            int start = getBagContentsStart(i);
            int end = getBagSize(i);
            if (!isBagFull(getBagView(start, end))) return i;
        }
        return -1;
    }

    @Nullable
    public List<ItemStack> getFirstValidBagView() {
        for (int i = 0; i < 10; i++) {
            int start = getBagContentsStart(i);
            int end = getBagSize(i);
            List<ItemStack> list = getBagView(start, end);
            if (!isBagFull(list)) return list;
        }
        return null;
    }

    public ItemStack putStack(ItemStack stack) {
        while (!stack.isEmpty()) {
            List<ItemStack> list = getFirstValidBagView();
            if (list == null) break;
            stack = this.tryAdd(list, stack);
        }
        return stack;
    }

    private List<ItemStack> getBagView(int start, int end) {
        return contents.subList(start, end);
    }

    private boolean isBagFull(List<ItemStack> list) {
        for (ItemStack stack : list) {
            if (stack.isEmpty()) return false;
        }
        return true;
    }

    private ItemStack tryAdd(List<ItemStack> contents, ItemStack stack) {
        for (int i = 0; i < contents.size(); i++) {
            ItemStack slot = contents.get(i);
            if (canMergeStacks(slot, stack)) {
                int target = slot.getMaxCount() - slot.getCount();
                slot.setCount(slot.getCount() + stack.getCount());
                stack = stack.split(target);
                if (stack.isEmpty()) break;
            }
        }
        return stack;
    }

    private boolean canMergeStacks(ItemStack first, ItemStack second) {
        return first.getItem() == second.getItem() && ItemStack.areTagsEqual(first, second);
    }

    public DefaultedList<ItemStack> getContents() {
        return this.contents;
    }

    @Override
    public int size() {
        return contents.size();
    }

    @Override
    public boolean isEmpty() {
        return contents.isEmpty();
    }

    @Override
    public ItemStack getStack(int slot) {
        return contents.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return contents.get(slot).split(amount);
    }

    @Override
    public ItemStack removeStack(int slot) {
        ItemStack stack = contents.get(slot);
        contents.set(slot, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        contents.set(slot, stack);
    }

    @Override
    public void markDirty() {

    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void clear() {
        contents.subList(10, contents.size()).clear();
    }
}