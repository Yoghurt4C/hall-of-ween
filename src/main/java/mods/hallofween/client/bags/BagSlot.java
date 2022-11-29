package mods.hallofween.client.bags;

import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.rei.gui.widget.Widget;
import mods.hallofween.Config;
import mods.hallofween.bags.BagInventory;
import mods.hallofween.util.HallOfWeenUtil;
import net.minecraft.client.gui.Element;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.List;

public class BagSlot extends Widget {
    public static final Identifier BACKGROUND = HallOfWeenUtil.getId("textures/gui/slot.png");
    public BagInventory inv;
    public int slot, x, y, w, h;
    public byte row, column;
    private boolean containsMouse;

    public BagSlot(BagInventory inv, int slot, int width, int height, int row, int column) {
        this.inv = inv;
        this.slot = slot;
        this.w = width;
        this.h = height;
        this.row = (byte) row;
        this.column = (byte) column;
    }

    public int getEntryWidth() {
        return this.w;
    }

    public int getEntryHeight() {
        return this.h;
    }

    public void move(int xPos, int yPos, boolean containsMouse) {
        this.x = xPos;
        this.y = yPos;
        this.containsMouse = containsMouse;
    }

    public ItemStack getStack() {
        return this.inv.getStack(this.slot);
    }

    public void setStack(ItemStack stack) {
        this.inv.setStack(slot, stack);
    }

    public boolean hasStack() {
        return !this.getStack().isEmpty();
    }

    @Override public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        RenderSystem.pushMatrix();
        matrices.push();
        RenderSystem.disableLighting();
        RenderSystem.disableDepthTest();
        ItemRenderer ir = this.minecraft.getItemRenderer();

        ItemStack itemStack = getStack();
        int x = this.x, y = this.y;
        if (slot < Config.maxBagInventorySize) {
            RenderSystem.scalef(0.5f, 0.5f, 1f);
            x *= 2;
            y *= 2;
        }
        this.setZOffset(100);
        this.minecraft.getItemRenderer().zOffset = 100.0F;
        RenderSystem.enableBlend();
        this.minecraft.getTextureManager().bindTexture(BACKGROUND);
        drawTexture(matrices, x - 1, y - 1, this.getZOffset(), 0, 0, 18, 18, 18, 18);
        RenderSystem.disableBlend();

        RenderSystem.enableDepthTest();
        this.minecraft.getItemRenderer().renderInGuiWithOverrides(this.minecraft.player, itemStack, x, y);
        this.minecraft.getItemRenderer().renderGuiItemOverlay(this.font, itemStack, x, y, null);

        this.minecraft.getItemRenderer().zOffset = 0.0F;
        this.setZOffset(0);

        if (this.containsMouse) {
            RenderSystem.disableDepthTest();
            RenderSystem.colorMask(true, true, true, false);
            this.fillGradient(matrices, x, y, x + 16, y + 16, -2130706433, -2130706433);
            RenderSystem.colorMask(true, true, true, true);
        }
        matrices.pop();
        RenderSystem.popMatrix();
    }

    @Override public boolean containsMouse(double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }

    @Override public List<? extends Element> children() {
        return Collections.emptyList();
    }
}
