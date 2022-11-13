package mods.hallofween.mixin.bags;

import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(DefaultedList.class)
public interface DefaultedListAccessor {
    @Invoker("<init>")
    static <E> DefaultedList<E> constructor(List<E> delegate, @Nullable E initialElement) {
        throw new RuntimeException("hi");
    }
}
