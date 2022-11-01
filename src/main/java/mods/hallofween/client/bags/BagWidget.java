package mods.hallofween.client.bags;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import mods.hallofween.bags.BagHolder;
import mods.hallofween.bags.BagInventory;
import mods.hallofween.mixin.bags.client.HandledScreenAccessor;
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

import static mods.hallofween.client.bags.BagHandler.*;
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

    private final PlayerInventory playerInventory;
    private final BagInventory bags;
    @Nullable
    protected Slot focusedSlot;
    @Nullable
    private Slot touchDragSlotStart;
    @Nullable
    private Slot touchDropOriginSlot;
    @Nullable
    private Slot touchHoveredSlot;
    @Nullable
    private Slot lastClickedSlot;
    protected int x;
    protected int y;
    private boolean touchIsRightClickDrag;
    private ItemStack touchDragStack;
    private int touchDropX;
    private int touchDropY;
    private long touchDropTime;
    private ItemStack touchDropReturningStack;
    private long touchDropTimer;
    protected final Set<Slot> cursorDragSlots;
    protected boolean cursorDragging;
    private int heldButtonType;
    private int heldButtonCode;
    private boolean cancelNextRelease;
    private int draggedStackRemainder;
    private long lastButtonClickTime;
    private int lastClickedButton;
    private boolean doubleClicking;
    private ItemStack quickMovingStack;

    public BagWidget(Text title, MinecraftClient client, HandledScreen<?> parent) {
        super(title);
        HandledScreenAccessor acc = (HandledScreenAccessor) parent;
        this.x = acc.getX();
        this.y = acc.getY();
        this.playerInventory = client.player.inventory;
        this.bags = ((BagHolder) this.playerInventory).getBagInventory();
        this.cursorDragSlots = Sets.newHashSet();
    }

    @Override
    protected void init() {
        bagX = this.x - this.bagW;
        bagY = (this.client.getWindow().getScaledHeight() - this.bagH) / 2;
        int bX = bagX + 4, bY = bagY + 23, contX = bX + 23, contY = bY - 18;
        bagSlots = new ArrayList<>();
        contentSlots = new ArrayList<>();
        for (int i = 0; i < bags.contents.size(); ) {
            if (i < bags.contentsOrdinal) {
                bagSlots.add(new Slot(bags, i, bX, bY));
                bY += 18;
                i++;
            } else {
                contentSlots.add(new Slot(bags, i, contX, contY));
                contX += 18;
                i++;
                if (i % 10 == 0) {
                    contX = bX + 23;
                    contY += 18;
                }
            }
        }
        super.init();
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
        if (this.focusedSlot != null && focusedSlot.hasStack()) {
            this.client.currentScreen.renderTooltip(matrices, this.client.currentScreen.getTooltipFromItem(focusedSlot.getStack()), mouseX, mouseY);
        }
    }

    public void drawBackground(MatrixStack matrices, int mouseX, int mouseY) {
        this.client.getTextureManager().bindTexture(GUI);
        this.drawTexture(matrices, bagX, bagY, 0, 0, 256, 256);
    }

    public void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
        for (Slot slot : bagSlots) {
            renderSlot(matrices, slot, mouseX, mouseY);
        }

        for (Slot slot : contentSlots.subList(0, 40)) {
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
        boolean bl = false;
        boolean bl2 = slot == this.touchDragSlotStart && !this.touchDragStack.isEmpty() && !this.touchIsRightClickDrag;
        ItemStack itemStack2 = this.client.player.inventory.getCursorStack();
        String string = null;
        if (slot == this.touchDragSlotStart && !this.touchDragStack.isEmpty() && this.touchIsRightClickDrag && !itemStack.isEmpty()) {
            itemStack = itemStack.copy();
            itemStack.setCount(itemStack.getCount() / 2);
        } else if (this.cursorDragging && this.cursorDragSlots.contains(slot) && !itemStack2.isEmpty()) {
            if (this.cursorDragSlots.size() == 1) {
                return;
            }

            if (ScreenHandler.canInsertItemIntoSlot(slot, itemStack2, true)/*todo && this.handler.canInsertIntoSlot(slot)*/) {
                itemStack = itemStack2.copy();
                bl = true;
                ScreenHandler.calculateStackSize(this.cursorDragSlots, this.heldButtonType, itemStack, slot.getStack().isEmpty() ? 0 : slot.getStack().getCount());
                int k = Math.min(itemStack.getMaxCount(), slot.getMaxItemCount(itemStack));
                if (itemStack.getCount() > k) {
                    string = Formatting.YELLOW.toString() + k;
                    itemStack.setCount(k);
                }
            } else {
                this.cursorDragSlots.remove(slot);
                this.calculateOffset();
            }
        }

        this.setZOffset(100);
        this.itemRenderer.zOffset = 100.0F;
        if (itemStack.isEmpty() && slot.doDrawHoveringEffect()) {
            Pair<Identifier, Identifier> pair = slot.getBackgroundSprite();
            if (pair != null) {
                Sprite sprite = this.client.getSpriteAtlas(pair.getFirst()).apply(pair.getSecond());
                this.client.getTextureManager().bindTexture(sprite.getAtlas().getId());
                drawSprite(matrices, i, j, this.getZOffset(), 16, 16, sprite);
                bl2 = true;
            }
        }

        if (!bl2) {
            if (bl) {
                fill(matrices, i, j, i + 16, j + 16, -2130706433);
            }

            RenderSystem.enableDepthTest();
            this.itemRenderer.renderInGuiWithOverrides(this.client.player, itemStack, i, j);
            this.itemRenderer.renderGuiItemOverlay(this.textRenderer, itemStack, i, j, string);
        }

        this.itemRenderer.zOffset = 0.0F;
        this.setZOffset(0);
    }

    protected void drawMouseoverTooltip(MatrixStack matrices, int x, int y) {
        if (this.client.player.inventory.getCursorStack().isEmpty() && this.focusedSlot != null && this.focusedSlot.hasStack()) {
            this.renderTooltip(matrices, this.focusedSlot.getStack(), x, y);
        }

    }

    private void calculateOffset() {
        ItemStack itemStack = this.client.player.inventory.getCursorStack();
        if (!itemStack.isEmpty() && this.cursorDragging) {
            if (this.heldButtonType == 2) {
                this.draggedStackRemainder = itemStack.getMaxCount();
            } else {
                this.draggedStackRemainder = itemStack.getCount();

                ItemStack itemStack2;
                int i;
                for (Iterator var2 = this.cursorDragSlots.iterator(); var2.hasNext(); this.draggedStackRemainder -= itemStack2.getCount() - i) {
                    Slot slot = (Slot) var2.next();
                    itemStack2 = itemStack.copy();
                    ItemStack itemStack3 = slot.getStack();
                    i = itemStack3.isEmpty() ? 0 : itemStack3.getCount();
                    ScreenHandler.calculateStackSize(this.cursorDragSlots, this.heldButtonType, itemStack2, i);
                    int j = Math.min(itemStack2.getMaxCount(), slot.getMaxItemCount(itemStack2));
                    if (itemStack2.getCount() > j) {
                        itemStack2.setCount(j);
                    }
                }

            }
        }
    }

    protected boolean isPointOverSlot(Slot slot, int width, int height, double pointX, double pointY) {
        int i = this.x;
        int j = this.y;
        //pointX -= i;
        //pointY -= j;
        return pointX >= slot.x - 1 && pointX < slot.x + width + 1 && pointY >= slot.y - 1 && pointY < slot.y + height + 1;
    }
}
