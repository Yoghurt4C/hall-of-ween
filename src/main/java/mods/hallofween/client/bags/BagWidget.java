package mods.hallofween.client.bags;

import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.clothconfig2.ClothConfigInitializer;
import me.shedaniel.clothconfig2.api.ScissorsHandler;
import me.shedaniel.clothconfig2.api.ScrollingContainer;
import me.shedaniel.math.Rectangle;
import mods.hallofween.Config;
import mods.hallofween.bags.BagHandler;
import mods.hallofween.bags.BagInventory;
import mods.hallofween.data.PlayerDataManager;
import mods.hallofween.mixin.bags.client.HandledScreenAccessor;
import mods.hallofween.network.BagSlotChangeMessage;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static mods.hallofween.util.HallOfWeenUtil.getId;

public class BagWidget extends Screen {
    private final Identifier GUI = getId("textures/gui/inventory.png");
    private Rectangle bounds, contBounds;
    private int rows, columns;
    private final MinecraftClient client;
    @Nullable
    protected BagSlot focusedSlot;
    @Nullable
    private Slot lastClickedSlot;
    protected int x;
    protected int y;
    private List<BagSlot> slots;
    protected ScrollingContainer scroll;

    public BagWidget(Text title, MinecraftClient client, HandledScreen<?> parent) {
        super(title);
        HandledScreenAccessor acc = (HandledScreenAccessor) parent;
        this.x = acc.getX();
        this.y = acc.getY();
        this.client = client;
    }

    @Override
    public void init() {
        int bagW = 116, bagH = 178,
                bagX = this.x - bagW,
                bagY = (this.client.getWindow().getScaledHeight() - bagH) / 2;
        this.bounds = new Rectangle(bagX, bagY, bagW, bagH);
        this.contBounds = new Rectangle(bagX + 19, bagY + 19, 97, 148);
        this.rows = 4;
        this.columns = 5;
        this.slots = new ArrayList<>();
        int row = 1, column = 0;
        if (client.player != null) {
            BagInventory bags = BagHandler.getBagHolder(client.player).getBagInventory();
            for (int i = 0; i < Config.maxBagInventorySize; i++) {
                slots.add(new BagSlot(this, bags, i, 9, 9 , i, 0));
            }
            for (int i = Config.maxBagInventorySize; i < bags.size(); i++) {
                slots.add(new BagSlot(this, bags, i, 18, 18, row, column));
                column++;
                if (column % this.columns == 0) {
                    row++;
                    column = 0;
                }
            }
        }
        this.scroll = new BagScrollingContainer(contBounds, row);
    }

    @Override
    public void removed() {
        super.removed();
        BagData.WIDGET = null;
        BagData.cachedScroll = this.scroll.scrollAmount;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        drawBackground(matrices, mouseX, mouseY);
        this.focusedSlot = null;
        drawForeground(matrices, mouseX, mouseY, delta);
        this.scroll.renderScrollBar();
        this.scroll.updatePosition(delta);
        if (this.focusedSlot != null && focusedSlot.hasStack()) {
            this.client.currentScreen.renderTooltip(matrices, this.client.currentScreen.getTooltipFromItem(focusedSlot.getStack()), mouseX, mouseY);
        }
    }

    public void drawBackground(MatrixStack matrices, int mouseX, int mouseY) {
        this.client.textRenderer.drawWithShadow(matrices, this.title, bounds.x + 24, bounds.y + 8, 0xFFFFFFFF);
        this.client.getTextureManager().bindTexture(GUI);
        RenderSystem.enableBlend();
        drawTexture(matrices, bounds.x, bounds.y, 0, 0, 116, 178, 256, 256);
        drawTexture(matrices, contBounds.x, contBounds.y, contBounds.getWidth(), contBounds.getHeight(), 145, 0, 111, 119, 256, 256);
        int gY = bounds.y + 20 + (9 * PlayerDataManager.getUnlockedBagSlotCount(client.player));
        fill(matrices, bounds.x + 10, bounds.y + 20, bounds.x + 20, gY, 0xFF000000);
        fillGradient(matrices, bounds.x + 10, gY, bounds.x + 20, bounds.y + 169, 0xFF000000, 0x00000000);
        RenderSystem.disableBlend();
    }

    public void drawForeground(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        int bX = bounds.x + 11, bY = bounds.y + 21, contX = contBounds.x + 2, contY = contBounds.y + 2;
        for (int i = 0; i < PlayerDataManager.getUnlockedBagSlotCount(client.player); i++) {
            BagSlot slot = slots.get(i);
            slot.move(bX, bY);
            slot.render(matrices, mouseX, mouseY, delta);
            bY += slot.getEntryHeight() + 1;
        }
        ScissorsHandler.INSTANCE.scissor(scroll.getScissorBounds());
        for (int i = Config.maxBagInventorySize; i < this.slots.size(); i++) {
            BagSlot slot = slots.get(i);
            boolean rendering = slot.y + slot.getEntryHeight() >= contBounds.y && slot.y <= contBounds.getMaxY();
            slot.move(contX, (int) (contY - scroll.scrollAmount));
            contX += slot.getEntryWidth();
            if (slot.column == this.columns - 1) {
                contX = contBounds.x + 2;
                contY += slot.getEntryHeight();
            }
            if (rendering) slot.render(matrices, mouseX, mouseY, delta);
        }
        ScissorsHandler.INSTANCE.removeLastScissor();
    }

    public Rectangle getBounds() {
        return this.bounds;
    }

    @Override
    public boolean isMouseOver(double pointX, double pointY) {
        return pointX >= bounds.x && pointX <= bounds.getMaxX() && pointY >= bounds.y && pointY <= bounds.getMaxY();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOver(mouseX, mouseY)) {
            if (scroll.updateDraggingState(mouseX, mouseY, button))
                return true;
            else if (focusedSlot != null) {
                int index = focusedSlot.slot;
                new BagSlotChangeMessage(index).send();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (scroll.mouseDragged(mouseX, mouseY, button, deltaX, deltaY))
            return true;
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (isMouseOver(mouseX, mouseY)) {
            scroll.offset(ClothConfigInitializer.getScrollStep() * -amount * (Screen.hasAltDown() ? 3 : 1), true);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    public static class BagScrollingContainer extends ScrollingContainer {
        private final Rectangle bounds;
        private final int rows;

        protected BagScrollingContainer(Rectangle bounds, int rows) {
            this.scrollAmount = BagData.cachedScroll;
            this.bounds = bounds;
            this.rows = rows;
        }

        @Override public Rectangle getBounds() {
            return bounds;
        }

        @Override public int getMaxScrollHeight() {
            return this.rows * 18;
        }

        @Override public boolean updateDraggingState(double mouseX, double mouseY, int button) {
            if (this.hasScrollBar()) {
                double height = this.getMaxScrollHeight();
                Rectangle bounds = this.getBounds();
                int actualHeight = bounds.height;
                if (height > (double) actualHeight && mouseY >= (double) bounds.y && mouseY <= (double) bounds.getMaxY()) {
                    double scrollbarPositionMinX = this.getScrollBarX();
                    if (mouseX >= scrollbarPositionMinX - 1.0 & mouseX <= scrollbarPositionMinX + 8.0) {
                        this.draggingScrollBar = true;
                        return true;
                    }
                }

                this.draggingScrollBar = false;
            }
            return false;
        }
    }
}
