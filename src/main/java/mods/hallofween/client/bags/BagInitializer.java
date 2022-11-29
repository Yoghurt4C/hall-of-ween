package mods.hallofween.client.bags;

import me.shedaniel.cloth.api.client.events.v0.ClothClientHooks;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.util.ActionResult;

public interface BagInitializer {
    static void init() {
        ClothClientHooks.SCREEN_INIT_POST.register(((client, screen, screenHooks) -> {
            if (screen instanceof HandledScreen && BagData.WIDGET == null) {
                BagData.WIDGET = new BagWidget(client, (HandledScreen<?>) screen);
                BagData.WIDGET.init(client, 0, 0);
                screenHooks.cloth$getChildren().add(BagData.WIDGET);
            }
        }));

        ClothClientHooks.SCREEN_RENDER_POST.register((matrices, minecraftClient, screen, i, i1, v) -> {
            if (canDoThings(screen)) {
                BagData.WIDGET.render(matrices, i, i1, v);
            }
        });

        ClothClientHooks.SCREEN_MOUSE_CLICKED.register((minecraftClient, screen, v, v1, i) -> {
            if (canDoThings(screen)) {
                if (BagData.WIDGET.mouseClicked(v, v1, i)) {
                    screen.setFocused(BagData.WIDGET);
                    if (i == 0) screen.setDragging(true);
                    return ActionResult.SUCCESS;
                } else if (screen.getFocused() == BagData.WIDGET) {
                    screen.setFocused(null);
                }
            }
            return ActionResult.PASS;
        });
        ClothClientHooks.SCREEN_MOUSE_DRAGGED.register((minecraftClient, screen, v, v1, i, v2, v3) -> {
            if (canDoThings(screen) && BagData.WIDGET.mouseDragged(v, v1, i, v2, v3)) {
                return ActionResult.SUCCESS;
            }
            return ActionResult.PASS;
        });
        ClothClientHooks.SCREEN_MOUSE_SCROLLED.register((minecraftClient, screen, v, v1, v2) -> {
            if (canDoThings(screen) && BagData.WIDGET.mouseScrolled(v, v1, v2)) {
                return ActionResult.SUCCESS;
            }
            return ActionResult.PASS;
        });

        ClothClientHooks.SCREEN_KEY_PRESSED.register((minecraftClient, screen, i, i1, i2) -> {
            if (canDoThings(screen) && BagData.WIDGET.keyPressed(i, i1, i2))
                return ActionResult.SUCCESS;
            return ActionResult.PASS;
        });

        ClothClientHooks.SCREEN_CHAR_TYPED.register((minecraftClient, screen, c, i) -> {
            if (canDoThings(screen) && BagData.WIDGET.charTyped(c, i))
                return ActionResult.SUCCESS;
            return ActionResult.PASS;
        });
            /*
            ClothClientHooks.SCREEN_MOUSE_RELEASED.register((minecraftClient, screen, v, v1, i) -> {
                if (canDoThings() && BagData.widget.mouseReleased(v, v1, i)) {
                    return ActionResult.SUCCESS;
                }
                return ActionResult.PASS;
            });
             */
    }
    
    static boolean canDoThings(Screen screen) {
        return screen instanceof HandledScreen && BagData.WIDGET != null;
    }
}
