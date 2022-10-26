package mods.hallofween.mixin.plugins;

import mods.hallofween.Config;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class HallOfWeenMixinPlugin implements IMixinConfigPlugin {
    @Override
    public void onLoad(String mixinPackage) {
        Config.tryInit();
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String target, String mixin) {
        if (parseFeature(mixin, "containers"))
            return Config.injectLootContainers;
        else if (parseFeature(mixin, "discovery"))
            return Config.enableDiscoveryRecipes;
        else if (parseFeature(mixin, "bags"))
            return Config.enableBagInventory;
        return true;
    }

    private boolean parseFeature(String mixin, String feature) {
        return mixin.startsWith("mods.hallofween.mixin." + feature);
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
