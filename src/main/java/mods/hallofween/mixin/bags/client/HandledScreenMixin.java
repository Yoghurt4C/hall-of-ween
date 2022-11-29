package mods.hallofween.mixin.bags.client;

import mods.hallofween.client.bags.BagData;
import mods.hallofween.client.bags.BagWidget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin extends Screen {
    protected HandledScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "onMouseClick", at = @At("HEAD"), cancellable = true)
    public void interceptClick(Slot slot, int invSlot, int clickData, SlotActionType actionType, CallbackInfo ctx) {
        if (this.getFocused() instanceof BagWidget) ctx.cancel();
    }

    @Inject(method = "onClose", at = @At("RETURN"))
    public void cleanup(CallbackInfo ctx) {
        BagData.WIDGET.onClose();
    }
}
