package mods.hallofween.mixin.containers;

import com.google.gson.JsonElement;
import mods.hallofween.registry.ContainerRegistry;
import net.minecraft.loot.LootManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.concurrent.Executor;

@Mixin(LootManager.class)
public abstract class LootManagerMixin {

    /**
     * @reason I need the container data to be loaded before loot table modifications.
     * This is probably unavoidable without mixing into *something* because
     * the only workaround I could find would be to stuff everything into
     * the {@link net.fabricmc.fabric.api.resource.SimpleResourceReloadListener#load(ResourceManager, Profiler, Executor)}
     * and hope FAPI sorts it to run as early as possible.
     * <p>
     * Previously, an injection into {@link net.minecraft.entity.LivingEntity}
     * was used, which limited data-driven loot table modifications to entities.
     * This approach lets users append Containers to any loot table, using JSON.
     */
    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;)V", at = @At("HEAD"))
    public void forceLoadContainers(Map<Identifier, JsonElement> map, ResourceManager manager, Profiler profiler, CallbackInfo ctx) {
        ContainerRegistry.load(manager);
    }
}
