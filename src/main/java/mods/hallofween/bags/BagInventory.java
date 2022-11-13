package mods.hallofween.bags;

import mods.hallofween.item.BagItem;
import mods.hallofween.mixin.bags.DefaultedListAccessor;
import mods.hallofween.network.BagSyncMessage;
import mods.hallofween.util.HallOfWeenUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BagInventory implements Inventory {
    public DefaultedList<ItemStack> contents;

    public BagInventory() {
        List<ItemStack> bags = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            bags.add(ItemStack.EMPTY);
        }
        this.contents = DefaultedListAccessor.constructor(bags, ItemStack.EMPTY);
    }

    private void setBag(int slot, ItemStack bag, PlayerEntity player) {
        ItemStack stack = contents.get(slot);
        if (bag.getItem() instanceof BagItem) {
            this.contents.set(slot, bag);
            int pointer = getBagContentsStart(slot);
            if (pointer > contents.size() - 1) {
                contents.addAll(pointer, DefaultedList.ofSize(getBagSize(bag), ItemStack.EMPTY));
            } else {
                if (stack.getItem() instanceof BagItem && pointer + getBagSize(stack) <= contents.size()) {
                    List<ItemStack> list = DefaultedList.ofSize(getBagSize(bag), ItemStack.EMPTY);
                    List<ItemStack> sub = contents.subList(pointer, pointer + getBagSize(stack));
                    Collections.copy(list, sub);
                    sub.clear();
                    contents.addAll(pointer, list);
                }
            }
        } else if (bag.equals(ItemStack.EMPTY) && !player.world.isClient()) {
            this.removeBag(slot, player);
        }
    }

    public void resolveSetStack(int slot, ItemStack stack, PlayerEntity player) {
        if (slot < 10) setBag(slot, stack, player);
        else setStack(slot, stack);
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

    public void removeBag(int slot, PlayerEntity player) {
        List<ItemStack> bag = getBagView(slot);
        if (player != null) {
            while (!bag.isEmpty()) {
                int i = 0;
                ItemStack stack = bag.remove(0);
                if (!stack.isEmpty())
                    ItemScatterer.spawn(player.world, player.getX(), player.getY(), player.getZ(), stack);
                HallOfWeenUtil.L.info(String.format("%dth operation on stack %s", i, stack.getName().asString()));
                i++;
            }
        }
        this.contents.remove(slot);
        this.contents.add(9, ItemStack.EMPTY);
        new BagSyncMessage(this.contents).send((ServerPlayerEntity) player);
    }

    //valid = not full
    public int getFirstValidBagSlot() {
        for (int i = 0; i < 10; i++) {
            int start = getBagContentsStart(i);
            int end = getBagSize(i);
            if (!isBagFull(getBagViewInternal(start, end))) return i;
        }
        return -1;
    }

    @Nullable
    public List<ItemStack> getFirstValidBagView() {
        for (int i = 0; i < 10; i++) {
            int start = getBagContentsStart(i);
            int end = start + getBagSize(i);
            List<ItemStack> list = getBagViewInternal(start, end);
            if (!isBagFull(list)) return list;
        }
        return null;
    }

    public List<ItemStack> getBagView(int bag) {
        int start = getBagContentsStart(bag);
        int end = start + getBagSize(bag);
        return getBagViewInternal(start, end);
    }

    public ItemStack putStack(ItemStack stack) {
        while (!stack.isEmpty()) {
            List<ItemStack> list = getFirstValidBagView();
            if (list == null) break;
            stack = this.tryAdd(list, stack);
        }
        return stack;
    }

    private List<ItemStack> getBagViewInternal(int start, int end) {
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