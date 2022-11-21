package mods.hallofween.compat.rei;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.BaseBoundsHandler;
import me.shedaniel.rei.api.DisplayHelper;
import me.shedaniel.rei.api.plugins.REIPluginV0;
import mods.hallofween.client.bags.BagData;
import mods.hallofween.util.HallOfWeenUtil;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class GW2REIPlugin implements REIPluginV0 {

    @Override public Identifier getPluginIdentifier() {
        return HallOfWeenUtil.getId("rei_plugin");
    }

    @Override public void registerBounds(DisplayHelper displayHelper) {
        BaseBoundsHandler.getInstance().registerExclusionZones(HandledScreen.class, new Exclusion());
    }

    private static class Exclusion implements Supplier<List<Rectangle>> {

        @Override public List<Rectangle> get() {
            if (BagData.WIDGET == null) return Collections.emptyList();
            else return Collections.singletonList(BagData.WIDGET.getBounds());
        }
    }
}
