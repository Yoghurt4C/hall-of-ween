package mods.hallofween.client.bags;

import mods.hallofween.bags.BagHolder;
import mods.hallofween.client.bags.BagWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.collection.DefaultedList;

import java.util.List;

@Environment(EnvType.CLIENT)
public class BagData {
    public static List<Slot> slots;
    public static BagWidget widget = null;
    public static DefaultedList<ItemStack> temp = null;
}
