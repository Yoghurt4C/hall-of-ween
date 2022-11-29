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
import mods.hallofween.mixin.bags.client.RecipeBookWidgetAccessor;
import mods.hallofween.network.BagSlotChangeMessage;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookProvider;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static mods.hallofween.util.HallOfWeenUtil.L;
import static mods.hallofween.util.HallOfWeenUtil.getId;

public class BagWidget extends Screen {
    private final Identifier GUI = getId("textures/gui/inventory.png"),
            DEPOSIT = getId("textures/gui/deposit.png"),
            COMPACT = getId("textures/gui/compact.png"),
            OPTIONS = getId("textures/gui/options.png"),
            TOGGLE = getId("textures/gui/toggle.png");
    private boolean visible;
    private Rectangle bounds, contBounds;
    public int rows, columns, maxRows;
    public int stackCount = 0, maxStackCount = 0;
    private final MinecraftClient client;
    @Nullable
    protected BagSlot focusedSlot;
    protected int x, y, bagW, bagH;
    private final HandledScreen<?> parent;
    private List<BagSlot> slots;
    private TextFieldWidget search;
    public ScrollingContainer scroll;

    public BagWidget(MinecraftClient client, HandledScreen<?> parent) {
        super(new TranslatableText("text.hallofween.inventory"));
        this.parent = parent;
        this.client = client;
    }

    @Override
    public void init() {
        HandledScreenAccessor acc = (HandledScreenAccessor) this.parent;
        this.x = acc.getX();
        this.y = acc.getY();
        if (this.parent instanceof RecipeBookProvider) {
            RecipeBookProvider rbp = (RecipeBookProvider) parent;
            RecipeBookWidgetAccessor racc = (RecipeBookWidgetAccessor) rbp.getRecipeBookWidget();
            if (racc.getRecipeBook() != null && rbp.getRecipeBookWidget().isOpen()) this.x -= 180;
        }
        this.visible = true;
        this.rows = 8;
        this.columns = 5;
        this.width = columns * 18;
        this.height = rows * 18;
        this.bagW = 16 + width;
        this.bagH = 44 + height;
        int bagX = this.x - bagW, bagY = (this.client.getWindow().getScaledHeight() - bagH) / 2;
        this.bounds = new Rectangle(bagX, bagY, this.bagW, this.bagH);
        this.contBounds = new Rectangle(bagX + 9, bagY + 44, this.width + 7, this.height);
        this.addButton(new TexturedButtonWidget(bounds.getMaxX() - 13, bounds.y + 4, 16, 16, 0, 0, 0, this.TOGGLE,16, 16, (button -> {
            this.client.inGameHud.getChatHud().addMessage(Text.of("Hiding the widget isn't implemented yet."));
        })));
        this.addButton(new TexturedButtonWidget(bounds.getMaxX() - 16, contBounds.y - 27, 16, 16, 0, 0, 0, this.OPTIONS, 16 ,16, button -> {
            this.client.inGameHud.getChatHud().addMessage(Text.of("Options aren't implemented yet."));
        }));
        this.addButton(new TexturedButtonWidget(bounds.getMaxX() - 32, contBounds.y - 27, 16, 16, 0, 0, 0, this.COMPACT, 16 ,16, button -> {
            this.client.inGameHud.getChatHud().addMessage(Text.of("Compacting isn't implemented yet."));
        }));
        this.addButton(new TexturedButtonWidget(bounds.getMaxX() - 48, contBounds.y - 27, 16, 16, 0, 0, 0, this.DEPOSIT, 16 ,16, button -> {
            this.client.inGameHud.getChatHud().addMessage(Text.of("Depositing isn't implemented yet."));
        }));
        this.search = new TextFieldWidget(this.textRenderer, contBounds.x + 4, contBounds.y - 12, contBounds.width - 4, 8, new TranslatableText("text.hallofween.search").formatted(Formatting.ITALIC));
        this.search.setDrawsBackground(false);
        if (client.player != null) this.updateContents();
        this.scroll = new BagScrollingContainer(contBounds, this.maxRows);
    }

    public void updateContents() {
        this.slots = new ArrayList<>();
        BagInventory bags = BagHandler.getBagHolder(client.player).getBagInventory();
        this.stackCount = 0;
        this.maxStackCount = bags.size() - Config.maxBagInventorySize;
        for (int i = 0; i < Config.maxBagInventorySize; i++) {
            slots.add(new BagSlot(bags, i, 9, 9, i, 0));
        }
        int row = 1, column = 0;
        for (int i = Config.maxBagInventorySize; i < bags.size(); i++) {
            ItemStack stack = bags.getStack(i);
            boolean bl = !stack.isEmpty();
            if (bl) this.stackCount++;
            if (search.getText().isEmpty() || bl && stack.getName().getString().toLowerCase().contains(search.getText().toLowerCase())) {
                slots.add(new BagSlot(bags, i, 18, 18, row, column));
                column++;
                if (column % this.columns == 0) {
                    row++;
                    column = 0;
                }
            }
        }
        this.maxRows = row;
    }

    @Override public void onClose() {
        BagData.cachedScroll = this.scroll.scrollAmount;
        BagData.WIDGET = null;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (visible) {
            drawBackground(matrices, mouseX, mouseY);
            this.focusedSlot = null;
            drawForeground(matrices, mouseX, mouseY, delta);
            this.search.render(matrices, mouseX, mouseY, delta);
            this.scroll.renderScrollBar();
            this.scroll.updatePosition(delta);
            if (this.focusedSlot != null && focusedSlot.hasStack()) {
                this.client.currentScreen.renderTooltip(matrices, this.client.currentScreen.getTooltipFromItem(focusedSlot.getStack()), mouseX, mouseY);
            }
            super.render(matrices, mouseX, mouseY, delta);
        }
    }

    public void drawBackground(MatrixStack matrices, int mouseX, int mouseY) {
        //todo remove
        //fill(matrices, bounds.x, bounds.y, bounds.getMaxX(), bounds.getMaxY(), 0xFFFFFFFF);
        //drawTexture(matrices, bounds.x, bounds.y, 0, 0, 120, 188, 256, 256);
        //title background
        horizontalGradient(matrices, bounds.x, bounds.y + 4, bounds.getMaxX(), bounds.y + 20, 0xFF23251E, 0x0);
        this.textRenderer.drawWithShadow(matrices, this.title, bounds.x + 23, bounds.y + 8, 0xFFEEBB);
        this.client.getTextureManager().bindTexture(GUI);
        //contents background
        RenderSystem.enableBlend();
        drawTexture(matrices, contBounds.x, contBounds.y - 24, contBounds.getWidth(), contBounds.getHeight() + 24, 145, 0, 111, 119, 256, 256);
        RenderSystem.disableBlend();
        //bag sprite
        drawTexture(matrices, bounds.x - 4, bounds.y + 2, 0, 0, 26, 19, 256, 256);
        //bags background
        int gY = bounds.y + 20 + (9 * PlayerDataManager.getUnlockedBagSlotCount(client.player));
        fill(matrices, bounds.x, bounds.y + 20, bounds.x + 10, gY, 0xFF000000);
        fillGradient(matrices, bounds.x, gY, bounds.x + 10, bounds.y + 169, 0xFF000000, 0x00000000);
        //search background
        fillGradient(matrices, contBounds.x + 2, contBounds.y - 13, contBounds.getMaxX() - 4, contBounds.y - 2, 0x80000000, 0x80000000);
        if (!search.isFocused() && search.getText().isEmpty())
            this.textRenderer.drawWithShadow(matrices, search.getMessage(), contBounds.x + 4, contBounds.y - 11, 0x4DFFFFFF);
        //counter
        matrices.push();
        matrices.scale(0.75f, 0.75f, 1f);
        this.textRenderer.draw(matrices, this.stackCount + "/" + this.maxStackCount, (contBounds.x + 4) / 0.75f, (contBounds.y - 20) / 0.75f, 0xFFFFFFFF);
        matrices.pop();
    }

    public void drawForeground(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        int bX = bounds.x + 1, bY = bounds.y + 21, contX = contBounds.x + 2, contY = contBounds.y + 1;
        for (int i = 0; i < PlayerDataManager.getUnlockedBagSlotCount(client.player); i++) {
            boolean contains = false;
            BagSlot slot = slots.get(i);
            if (slot.containsMouse(mouseX, mouseY)) {
                this.focusedSlot = slot;
                contains = true;
            }
            slot.move(bX, bY, contains);
            slot.render(matrices, mouseX, mouseY, delta);
            bY += slot.getEntryHeight() + 1;
        }
        if (scroll != null) {
            ScissorsHandler.INSTANCE.scissor(scroll.getScissorBounds());
            for (int i = Config.maxBagInventorySize; i < this.slots.size(); i++) {
                boolean contains = false;
                BagSlot slot = slots.get(i);
                boolean rendering = slot.y + slot.getEntryHeight() >= contBounds.y && slot.y <= contBounds.getMaxY();
                if (scroll.getScissorBounds().contains(mouseX, mouseY) && slot.containsMouse(mouseX, mouseY)) {
                    this.focusedSlot = slot;
                    contains = true;
                }
                slot.move(contX, (int) (contY - scroll.scrollAmount), contains);
                contX += slot.getEntryWidth();
                if (slot.column == this.columns - 1) {
                    contX = contBounds.x + 2;
                    contY += slot.getEntryHeight();
                }
                if (rendering) slot.render(matrices, mouseX, mouseY, delta);
            }
            ScissorsHandler.INSTANCE.removeLastScissor();
        }
    }

    private void horizontalGradient(MatrixStack matrices, int xStart, int yStart, int xEnd, int yEnd, int colorStart, int colorEnd) {
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.disableAlphaTest();
        RenderSystem.defaultBlendFunc();
        RenderSystem.shadeModel(7425);
        Matrix4f matrix = matrices.peek().getModel();
        int z = this.getZOffset();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(7, VertexFormats.POSITION_COLOR);
        float a1 = (float) (colorStart >> 24 & 255) / 255.0F;
        float r1 = (float) (colorStart >> 16 & 255) / 255.0F;
        float g1 = (float) (colorStart >> 8 & 255) / 255.0F;
        float b1 = (float) (colorStart & 255) / 255.0F;
        float a2 = (float) (colorEnd >> 24 & 255) / 255.0F;
        float r2 = (float) (colorEnd >> 16 & 255) / 255.0F;
        float g2 = (float) (colorEnd >> 8 & 255) / 255.0F;
        float b2 = (float) (colorEnd & 255) / 255.0F;
        bufferBuilder.vertex(matrix, (float) xEnd, (float) yStart, (float) z).color(r2, g2, b2, a2).next();
        bufferBuilder.vertex(matrix, (float) xStart, (float) yStart, (float) z).color(r1, g1, b1, a1).next();
        bufferBuilder.vertex(matrix, (float) xStart, (float) yEnd, (float) z).color(r1, g1, b1, a1).next();
        bufferBuilder.vertex(matrix, (float) xEnd, (float) yEnd, (float) z).color(r2, g2, b2, a2).next();
        tessellator.draw();
        RenderSystem.shadeModel(7424);
        RenderSystem.disableBlend();
        RenderSystem.enableAlphaTest();
        RenderSystem.enableTexture();
    }

    public Rectangle getBounds() {
        return this.bounds;
    }

    @Override
    public boolean isMouseOver(double pointX, double pointY) {
        return pointX >= bounds.x && pointX <= bounds.getMaxX() && pointY >= bounds.y && pointY <= bounds.getMaxY();
    }

    @Override public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOver(mouseX, mouseY)) {
            if (search.mouseClicked(mouseX, mouseY, button))
                return true;
            else if (scroll.updateDraggingState(mouseX, mouseY, button))
                return true;
            else if (focusedSlot != null) {
                int index = focusedSlot.slot;
                new BagSlotChangeMessage(index).send();
            } else {
                return super.mouseClicked(mouseX, mouseY, button);
            }
            return true;
        } else if (search.isActive()) search.setTextFieldFocused(false);
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

    @Override public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (search.keyPressed(keyCode, scanCode, modifiers)) {
            if (keyCode == 259) {
                updateContents();
            }
            return true;
        } else if (search.isActive() && keyCode == 69) //haha funnii numpter
            return true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override public boolean charTyped(char chr, int modifiers) {
        if (search.charTyped(chr, modifiers)) {
            this.updateContents();
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    public static class BagScrollingContainer extends ScrollingContainer {
        private final Rectangle bounds;
        private final int rows;

        protected BagScrollingContainer(Rectangle bounds, int rows) {
            this.scrollAmount = BagData.cachedScroll;
            this.scrollTarget = BagData.cachedScroll;
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
