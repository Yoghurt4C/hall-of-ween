package mods.hallofween.bags;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public interface BagHandler {
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
