package mods.hallofween.mixin.client;

import com.mojang.authlib.GameProfile;
import mods.hallofween.client.HallOfWeenClient;
import mods.hallofween.data.PlayerDataManager;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends PlayerEntity {
    public ClientPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void pooba(CallbackInfo ctx) {
        if (HallOfWeenClient.CLIENT_DATA == null) {
            PlayerDataManager.DATA.put(this.getUuid(), PlayerDataManager.getDefaultPlayerData());
        } else {
            PlayerDataManager.DATA.put(this.getUuid(), HallOfWeenClient.CLIENT_DATA);
            HallOfWeenClient.CLIENT_DATA = null;
        }
    }
}
