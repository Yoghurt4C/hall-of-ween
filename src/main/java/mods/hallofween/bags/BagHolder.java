package mods.hallofween.bags;

import net.minecraft.item.ItemStack;

import java.util.List;

public interface BagHolder {
    BagInventory getBagInventory();

    default ItemStack getBag(int bag) {
        return getBagInventory().getStack(bag);
    }

    default List<ItemStack> getContents(ItemStack bag) {
        return getBagInventory().getContents();
    }

    /*
    default List<ItemStack> getFirstValidBagContents() {
        for (ItemStack bag : getBagInventory().bags) {
            if (!getBagInventory().contentsFull(bag)) return getContents(bag);
        }
        return DefaultedList.of();
    }
     */
    default void removeBag(int bag) {
        getBagInventory().removeBagWithContents(bag);
    }
}
