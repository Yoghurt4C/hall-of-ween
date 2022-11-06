package mods.hallofween.compat.rei;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.gui.widget.WidgetWithBounds;
import mods.hallofween.item.RecipeSheetItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static mods.hallofween.util.HallOfWeenUtil.getId;

public class SheetWidget extends WidgetWithBounds {
    private static final Identifier INDICATOR = getId("textures/gui/sheet_indicator.png");
    private final Rectangle bounds;
    private final Point point;
    private final ItemStack sheet;
    private final Identifier advId;
    private final boolean noAdv;

    public SheetWidget(Point point, ItemStack stack, Identifier advId, boolean noAdv) {
        this.point = point;
        this.bounds = new Rectangle(point.x + 64, point.y, 16, 16);
        this.sheet = RecipeSheetItem.getSheetForItem(stack);
        this.advId = advId;
        this.noAdv = noAdv;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        int x = point.getX() + 64;
        int y = point.getY();
        MinecraftClient mc = MinecraftClient.getInstance();
        mc.getItemRenderer().renderInGuiWithOverrides(sheet, x, y);
        mc.getTextureManager().bindTexture(INDICATOR);
        matrices.push();
        matrices.translate(0, 0, 400f);
        drawTexture(matrices, x + 3, y + 23, 0, noAdv ? 0 : 16, 16, 16, 16, 32);
        matrices.pop();
        if (this.containsMouse(mouseX, mouseY)) {
            List<Text> tt = new ArrayList<>();
            tt.add(sheet.getName());
            if (mc.options.advancedItemTooltips)
                tt.add(new LiteralText(advId.toString()).formatted(Formatting.DARK_GRAY));
            if (noAdv)
                tt.add(new TranslatableText("text.hallofween.rei_advancement").formatted(Formatting.RED, Formatting.BOLD));
            MinecraftClient.getInstance().currentScreen.renderTooltip(matrices, tt, mouseX, mouseY);
        }
    }

    @Override
    public List<? extends Element> children() {
        return Collections.emptyList();
    }

    @Override
    public @NotNull Rectangle getBounds() {
        return bounds;
    }
}
