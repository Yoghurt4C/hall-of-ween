package mods.hallofween.client.bags;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import me.shedaniel.clothconfig2.ClothConfigInitializer;
import me.shedaniel.clothconfig2.api.ScrollingContainer;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.gui.modules.MenuEntry;
import me.shedaniel.rei.gui.modules.entries.SubSubsetsMenuEntry;
import mods.hallofween.bags.BagHandler;
import mods.hallofween.bags.BagHolder;
import mods.hallofween.bags.BagInventory;
import mods.hallofween.mixin.bags.client.HandledScreenAccessor;
import mods.hallofween.mixin.bags.client.SlotAccessor;
import mods.hallofween.network.BagSlotChangeMessage;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import static mods.hallofween.client.bags.BagData.slots;
import static mods.hallofween.client.bags.BagData.widget;
import static mods.hallofween.util.HallOfWeenUtil.getId;

/**
 * Most of this class is just vanilla copy with minor edits and shortcuts here and there.
 * Why is everything in HandledScreen private?
 */
public class BagWidget extends Screen {
    private final Identifier GUI = getId("textures/gui/inventory.png");
    private final int bagW = 218;
    private final int bagH = 97;
    private int bagX, bagY;
    private final MinecraftClient client;
    @Nullable
    protected Slot focusedSlot;
    @Nullable
    private Slot lastClickedSlot;
    protected int x;
    protected int y;
    protected final Set<Slot> cursorDragSlots;
    protected boolean cursorDragging;

    private final ScrollingContainer scroll = new ScrollingContainer() {
        @Override public Rectangle getBounds() {
            return new Rectangle(bagX, bagY, bagW, bagH);
        }

        @Override public int getMaxScrollHeight() {
            return 500;
        }
    };

    public BagWidget(Text title, MinecraftClient client, HandledScreen<?> parent) {
        super(title);
        HandledScreenAccessor acc = (HandledScreenAccessor) parent;
        this.x = acc.getX();
        this.y = acc.getY();
        this.client = client;
        this.cursorDragSlots = Sets.newHashSet();
    }

    @Override
    public void init() {
        bagX = this.x - this.bagW;
        bagY = (this.client.getWindow().getScaledHeight() - this.bagH) / 2;
        int bX = bagX + 4, bY = bagY + 23, contX = bX + 23, contY = bY;
        slots = new ArrayList<>();
        if (client.player != null) {
            BagInventory bags = BagHandler.getBagHolder(client.player).getBagInventory();
            for (int i = 0; i < 10; i++) {
                slots.add(new Slot(bags, i, bX, bY));
                bY += 18;
            }
            for (int i = 10; i < bags.size(); ) {
                slots.add(new Slot(bags, i, contX, contY));
                contX += 18;
                i++;
                if (i % 10 == 0) {
                    contX = bX + 23;
                    contY += 18;
                }
            }
        }
    }

    @Override
    public void removed() {
        super.removed();
        widget = null;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        drawBackground(matrices, mouseX, mouseY);
        focusedSlot = null;
        drawForeground(matrices, mouseX, mouseY);
        scroll.renderScrollBar();
        scroll.updatePosition(delta);
        if (this.focusedSlot != null && focusedSlot.hasStack()) {
            this.client.currentScreen.renderTooltip(matrices, this.client.currentScreen.getTooltipFromItem(focusedSlot.getStack()), mouseX, mouseY);
        }
    }

    public void drawBackground(MatrixStack matrices, int mouseX, int mouseY) {
        this.client.getTextureManager().bindTexture(GUI);
        this.drawTexture(matrices, bagX, bagY, 0, 0, 256, 256);
    }

    public void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
        for (Slot slot : slots) {
            renderSlot(matrices, slot, mouseX, mouseY);
        }
    }

    public void renderSlot(MatrixStack matrices, Slot slot, int mouseX, int mouseY) {
        matrices.push();
        RenderSystem.disableLighting();
        RenderSystem.disableDepthTest();
        this.drawSlot(matrices, slot);
        if (this.isPointOverSlot(slot, 16, 16, mouseX, mouseY) && slot.doDrawHoveringEffect()) {
            this.focusedSlot = slot;
            RenderSystem.disableDepthTest();
            RenderSystem.colorMask(true, true, true, false);
            this.fillGradient(matrices, slot.x, slot.y, slot.x + 16, slot.y + 16, -2130706433, -2130706433);
            RenderSystem.colorMask(true, true, true, true);
        }
        matrices.pop();
    }

    private void drawSlot(MatrixStack matrices, Slot slot) {
        int i = slot.x;
        int j = slot.y;
        ItemStack itemStack = slot.getStack();

        this.setZOffset(100);
        this.itemRenderer.zOffset = 100.0F;
        if (itemStack.isEmpty() && slot.doDrawHoveringEffect()) {
            Pair<Identifier, Identifier> pair = slot.getBackgroundSprite();
            if (pair != null) {
                Sprite sprite = this.client.getSpriteAtlas(pair.getFirst()).apply(pair.getSecond());
                this.client.getTextureManager().bindTexture(sprite.getAtlas().getId());
                drawSprite(matrices, i, j, this.getZOffset(), 16, 16, sprite);
            }
        }

        RenderSystem.enableDepthTest();
        this.itemRenderer.renderInGuiWithOverrides(this.client.player, itemStack, i, j);
        this.itemRenderer.renderGuiItemOverlay(this.textRenderer, itemStack, i, j, null);

        this.itemRenderer.zOffset = 0.0F;
        this.setZOffset(0);
    }

    protected void drawMouseoverTooltip(MatrixStack matrices, int x, int y) {
        if (this.client.player.inventory.getCursorStack().isEmpty() && this.focusedSlot != null && this.focusedSlot.hasStack()) {
            this.renderTooltip(matrices, this.focusedSlot.getStack(), x, y);
        }

    }

    protected boolean isPointOverSlot(Slot slot, int width, int height, double pointX, double pointY) {
        return pointX >= slot.x - 1 && pointX < slot.x + width + 1 && pointY >= slot.y - 1 && pointY < slot.y + height + 1;
    }

    @Override
    public boolean isMouseOver(double pointX, double pointY) {
        return pointX >= bagX && pointX <= bagX + bagW && pointY >= bagY && pointY <= bagY + bagH;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOver(mouseX, mouseY)) {
            if (scroll.updateDraggingState(mouseX, mouseY, button))
                return true;
            else if (focusedSlot != null) {
                int index = ((SlotAccessor) focusedSlot).getIndex();
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
}
