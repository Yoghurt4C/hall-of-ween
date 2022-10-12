package mods.hallofween.mixin;

import mods.hallofween.HallOfWeen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

//Imagine where our society would be if fapi merged this back in 1.15 when it was first written
@Mixin(ClientPlayNetworkHandler.class)
public abstract class EntitySpawnSyncMixin {
    @Shadow
    private ClientWorld world;

    @Inject(method = "onEntitySpawn", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/network/packet/s2c/play/EntitySpawnS2CPacket;getEntityTypeId()Lnet/minecraft/entity/EntityType;"),
            locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    public void handleEntitySpawnPacket(EntitySpawnS2CPacket packet, CallbackInfo ctx, double x, double y, double z, EntityType<?> type) {
        Identifier id = Registry.ENTITY_TYPE.getId(type);
        if (id.getNamespace().equals(HallOfWeen.DEFAULTID.getNamespace())) {
            Entity entity = type.create(world);
            if (entity == null) {
                ctx.cancel();
                return;
            }

            int networkId = packet.getId();
            entity.setPos(x, y, z);
            entity.updateTrackedPosition(x, y, z);
            entity.pitch = packet.getPitch();
            entity.yaw = packet.getYaw();
            entity.setEntityId(networkId);
            entity.setUuid(packet.getUuid());
            this.world.addEntity(networkId, entity);
            ctx.cancel();
        }
    }
}
