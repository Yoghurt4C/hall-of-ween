package mods.hallofween.client.bags;

import mods.hallofween.bags.BagHolder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.slot.Slot;

import java.util.List;

public class BagData {
    public static List<Slot> slots;
    public static BagWidget widget = null;

    public static BagHolder getBagHolder(PlayerEntity player) {
        return ((BagHolder) player.inventory);
    }
}
