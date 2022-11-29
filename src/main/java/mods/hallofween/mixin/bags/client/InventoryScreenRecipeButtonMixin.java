package mods.hallofween.mixin.bags.client;

import mods.hallofween.client.bags.BagData;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenRecipeButtonMixin {
    @Inject(method = "method_19891", at = @At("TAIL"))
    public void moveWidget(CallbackInfo ctx) {
        BagData.WIDGET.init();
    }
}
