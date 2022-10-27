package mods.hallofween.bags;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;

import java.util.List;

public class BagInventory implements Inventory {
    public DefaultedList<ItemStack> contents;
    public PlayerEntity player;
    public final int contentsOrdinal = 9;

    public BagInventory(PlayerEntity player) {
        this.contents = DefaultedList.ofSize(10 + 32 * 10, ItemStack.EMPTY);
        this.contents.set(0, new ItemStack(Items.APPLE));
        for (int i = 10; i < 30; i++)
            this.contents.set(i, new ItemStack(Items.GOLD_BLOCK));
        this.player = player;
    }

    public List<ItemStack> getContents() {
        return contents.subList(10, contents.size());
    }

    public boolean isFull() {
        for (ItemStack stack : getContents()) {
            if (stack.isEmpty()) return false;
        }
        return true;
    }

    public void removeBagWithContents(int stack) {
        if (stack < 10) {
            int size = 20;
            int temp = stack * size;
            for (int i = temp; i < temp + size; i++) {
                ItemScatterer.spawn(player.world, player.getX(), player.getY(), player.getZ(), contents.get(i));
                contents.set(i, ItemStack.EMPTY);
            }
            contents.set(stack, ItemStack.EMPTY);
        }
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
    public ItemStack getStack(int stack) {
        return contents.get(stack);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return Inventories.splitStack(this.contents, slot, amount);
    }

    @Override
    public ItemStack removeStack(int slot) {
        return Inventories.removeStack(this.contents, slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        this.contents.set(slot, stack);
    }

    @Override
    public void markDirty() {
        //todo
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void clear() {
        this.contents.clear();
    }
}
