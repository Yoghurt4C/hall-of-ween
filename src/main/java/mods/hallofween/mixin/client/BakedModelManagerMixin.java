package mods.hallofween.mixin.client;

import mods.hallofween.events.ResourceReloadEvents;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BakedModelManager.class)
public abstract class BakedModelManagerMixin {

    @Shadow @Final private BlockModels blockModelCache;

    /**
     * Relieves the headache of having to query the model manager for static models
     * every frame by letting me just cache them at the end of resource reload.
     */
    @Inject(method = "apply(Lnet/minecraft/client/render/model/ModelLoader;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;)V", at = @At("TAIL"))
    public void internalModelSetter(ModelLoader loader, ResourceManager manager, Profiler profiler, CallbackInfo ctx) {
        ResourceReloadEvents.FINISH.invoker().finish(this.blockModelCache, loader, manager);
    }
}
