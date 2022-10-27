package mods.hallofween.mixin.bags;

import mods.hallofween.bags.BagHolder;
import mods.hallofween.bags.BagInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerInventory.class)
public abstract class PlayerInventoryBagHolder implements BagHolder {
    @Shadow
    @Final
    public PlayerEntity player;
    @Unique
    BagInventory bagInv;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void gfy(PlayerEntity player, CallbackInfo ctx) {
        this.bagInv = new BagInventory(player);
    }

    @Unique
    @Override
    public BagInventory getBagInventory() {
        return bagInv;
    }
}
