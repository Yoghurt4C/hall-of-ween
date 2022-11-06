package mods.hallofween.mixin.bags.client;

import mods.hallofween.client.bags.BagData;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {
    @Inject(method = "closeScreen", at = @At("TAIL"))
    public void cleanup(CallbackInfo ctx) {
        BagData.bagSlots = null;
        BagData.contentSlots = null;
    }
}
