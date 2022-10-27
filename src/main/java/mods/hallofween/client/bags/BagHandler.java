package mods.hallofween.client.bags;

import mods.hallofween.bags.BagHolder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.slot.Slot;

import java.util.List;

public class BagHandler {
    public static List<Slot> bagSlots;
    public static List<Slot> contentSlots;

    public static BagWidget widget = null;

    public static BagHolder getBagHolder(PlayerEntity player) {
        return ((BagHolder) player.inventory);
    }
}
