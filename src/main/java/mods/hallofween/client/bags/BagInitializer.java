package mods.hallofween.client.bags;

import me.shedaniel.cloth.api.client.events.v0.ClothClientHooks;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;

public interface BagInitializer {
    static void init() {
        ClothClientHooks.SCREEN_INIT_POST.register(((client, screen, screenHooks) -> {
            if (screen instanceof HandledScreen) {
                BagData.widget = new BagWidget(Text.of("fuckyou"), client, (HandledScreen<?>) screen);
                BagData.widget.init(client, 218, 97);
                screenHooks.cloth$getChildren().add(BagData.widget);
            }
        }));

        ClothClientHooks.SCREEN_RENDER_POST.register((matrices, minecraftClient, screen, i, i1, v) -> {
            if (screen instanceof HandledScreen) {
                BagData.widget.render(matrices, i, i1, v);
            }
        });

        ClothClientHooks.SCREEN_MOUSE_CLICKED.register((minecraftClient, screen, v, v1, i) -> {
            if (screen instanceof HandledScreen && BagData.widget.mouseClicked(v, v1, i)) {
                screen.setFocused(BagData.widget);
                if (i == 0) screen.setDragging(true);
                return ActionResult.SUCCESS;
            }
            screen.setFocused(null);
            return ActionResult.PASS;
        });
            /*
            ClothClientHooks.SCREEN_MOUSE_DRAGGED.register((minecraftClient, screen, v, v1, i, v2, v3) -> {
                if (screen instanceof HandledScreen && BagData.widget.mouseDragged(v, v1, i, v2, v3)) {
                    return ActionResult.SUCCESS;
                }
                return ActionResult.PASS;
            });
            ClothClientHooks.SCREEN_MOUSE_RELEASED.register((minecraftClient, screen, v, v1, i) -> {
                if (screen instanceof HandledScreen && BagData.widget.mouseReleased(v, v1, i)) {
                    return ActionResult.SUCCESS;
                }
                return ActionResult.PASS;
            });
             */
    }
}
