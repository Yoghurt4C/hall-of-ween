package mods.hallofween.item;

import net.minecraft.item.Item;

public class BagItem extends Item {
    public BagItem(Settings settings) {
        super(settings);
    }

    public int getSlotCount() {
        return 32;
    }
}
