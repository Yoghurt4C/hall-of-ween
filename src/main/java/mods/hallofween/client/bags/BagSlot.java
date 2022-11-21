package mods.hallofween.client.bags;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import me.shedaniel.rei.gui.widget.Widget;
import mods.hallofween.bags.BagInventory;
import net.minecraft.client.gui.Element;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.List;

public class BagSlot extends Widget {
    public BagWidget parent;
    public BagInventory inv;
    public int slot, x, y, w, h;
    public byte row, column;

    public BagSlot(BagWidget parent, BagInventory inv, int slot, int width, int height, int row, int column) {
        this.parent = parent;
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

    public void updateInformation(int xPos, int yPos, boolean selected, boolean containsMouse, boolean rendering, int width) {
        this.x = xPos;
        this.y = yPos;
        /*this.selected = selected;
        this.containsMouse = containsMouse;
        this.rendering = rendering;
        this.width = width;
         */
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
        matrices.push();
        RenderSystem.disableLighting();
        RenderSystem.disableDepthTest();

        ItemStack itemStack = getStack();

        this.setZOffset(100);
        this.minecraft.getItemRenderer().zOffset = 100.0F;
        if (itemStack.isEmpty()) {
            Pair<Identifier, Identifier> pair = getBackgroundSprite();
            if (pair != null) {
                Sprite sprite = this.minecraft.getSpriteAtlas(pair.getFirst()).apply(pair.getSecond());
                this.minecraft.getTextureManager().bindTexture(sprite.getAtlas().getId());
                drawSprite(matrices, x, y, this.getZOffset(), 16, 16, sprite);
            }
        }

        RenderSystem.enableDepthTest();
        this.minecraft.getItemRenderer().renderInGuiWithOverrides(this.minecraft.player, itemStack, x, y);
        this.minecraft.getItemRenderer().renderGuiItemOverlay(this.font, itemStack, x, y, null);

        this.minecraft.getItemRenderer().zOffset = 0.0F;
        this.setZOffset(0);

        if (slot > 9 && parent.scroll.getScissorBounds().contains(mouseX, mouseY) && this.containsMouse(mouseX, mouseY) || slot < 10 && this.containsMouse(mouseX, mouseY)) {
            this.parent.focusedSlot = this;
            RenderSystem.disableDepthTest();
            RenderSystem.colorMask(true, true, true, false);
            this.fillGradient(matrices, x, y, x + 16, y + 16, -2130706433, -2130706433);
            RenderSystem.colorMask(true, true, true, true);
        }
        matrices.pop();
    }

    @Override public boolean containsMouse(double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }

    @Override public List<? extends Element> children() {
        return Collections.emptyList();
    }

    private Pair<Identifier, Identifier> getBackgroundSprite() {
        return null;
    }
}
