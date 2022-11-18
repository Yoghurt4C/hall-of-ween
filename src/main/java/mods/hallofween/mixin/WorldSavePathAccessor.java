package mods.hallofween.mixin;

import net.minecraft.util.WorldSavePath;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(WorldSavePath.class)
public interface WorldSavePathAccessor {
    @Invoker("<init>")
    static WorldSavePath constructor(String path) {
        throw new RuntimeException("he did it again!!!");
    }
}
