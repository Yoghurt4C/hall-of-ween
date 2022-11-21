package mods.hallofween.mixin.bags;

import mods.hallofween.bags.BagHolder;
import mods.hallofween.bags.BagInventory;
import mods.hallofween.client.bags.BagData;
import net.minecraft.entity.player.PlayerInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerInventory.class)
public abstract class PlayerInventoryBagHolder implements BagHolder {

    @Unique
    public BagInventory bagInventory;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void pooba(CallbackInfo ctx) {
        if (BagData.TEMP == null) {
            bagInventory = new BagInventory();
        } else {
            bagInventory = new BagInventory(BagData.TEMP);
        }
    }

    @Unique
    @Override
    public BagInventory getBagInventory() {
        return bagInventory;
    }

    @Unique
    @Override
    public void setBagInventory(BagInventory inv) {
        this.bagInventory = inv;
    }
}
