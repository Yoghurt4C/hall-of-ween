package mods.hallofween.client.bags;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

@Environment(EnvType.CLIENT)
public class BagData {
    public static BagWidget WIDGET = null;
    public static double cachedScroll;
    public static DefaultedList<ItemStack> TEMP = null;
}
