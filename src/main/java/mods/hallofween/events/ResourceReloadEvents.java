package mods.hallofween.events;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.resource.ResourceManager;

@Environment(EnvType.CLIENT)
public final class ResourceReloadEvents {

    public final static Event<Finish> FINISH = EventFactory.createArrayBacked(Finish.class, callbacks -> (models, loader, manager) -> {
        for (Finish callback : callbacks) {
            callback.finish(models, loader, manager);
        }
    });

    public interface Finish {
        void finish(BlockModels models, ModelLoader loader, ResourceManager manager);
    }
}
