package mods.hallofween.util;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import mods.hallofween.HallOfWeen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface SheetRei {
    static void render(Rectangle bounds, Point point, @NotNull DrawableHelper helper, @NotNull MatrixStack matrices, int mouseX, int mouseY, float delta) {
        ItemStack stack = new ItemStack(HallOfWeen.getItem("recipe_sheet"));
        MinecraftClient.getInstance().getItemRenderer().renderInGui(stack, point.x + 5, point.y);
    }
}
