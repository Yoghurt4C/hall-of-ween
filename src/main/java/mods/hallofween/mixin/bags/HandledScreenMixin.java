package mods.hallofween.mixin.bags;

import com.mojang.blaze3d.systems.RenderSystem;
import mods.hallofween.bags.BagHolder;
import mods.hallofween.bags.BagInventory;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;

import static mods.hallofween.client.bags.BagHandler.*;
import static mods.hallofween.util.HallOfWeenUtil.getId;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin<T extends ScreenHandler> extends Screen implements ScreenHandlerProvider<T> {
    @Shadow
    protected int x;
    @Shadow
    protected int y;
    @Shadow
    @Final
    protected PlayerInventory playerInventory;

    @Shadow
    protected abstract void drawSlot(MatrixStack matrices, Slot slot);

    @Shadow
    protected abstract boolean isPointOverSlot(Slot slot, double pointX, double pointY);

    @Shadow
    @Nullable
    protected Slot focusedSlot;
    @Unique
    private final Identifier GUI = getId("textures/gui/bags.png");
    @Unique
    private final int bagW = 218;
    @Unique
    private final int bagH = 97;
    @Unique
    private int bagX;
    @Unique
    private int bagY;

    protected HandledScreenMixin(Text t) {
        super(t);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;drawBackground(Lnet/minecraft/client/util/math/MatrixStack;FII)V", shift = At.Shift.AFTER))
    public void drawBackground(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ctx) {
        this.client.getTextureManager().bindTexture(GUI);
        this.drawTexture(matrices, bagX, bagY, 0, 0, 256, 256);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;drawForeground(Lnet/minecraft/client/util/math/MatrixStack;II)V", shift = At.Shift.AFTER))
    public void drawForeground(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ctx) {
        for (Slot slot : bagSlots) {
            renderSlot(matrices, slot, mouseX, mouseY);
        }

        for (Slot slot : contentSlots) {
            renderSlot(matrices, slot, mouseX, mouseY);
        }
    }

    @Inject(method = "init", at = @At("TAIL"))
    public void init(CallbackInfo ctx) {
        bagX = this.x - this.bagW;
        bagY = (this.client.getWindow().getScaledHeight() - this.bagH) / 2;
        int bX = bagX + 4 - this.x, bY = bagY + 23 - this.y, contX = bX + 23, contY = bY - 18;
        BagHolder holder = getBagHolder(this.playerInventory.player);
        BagInventory inv = holder.getBagInventory();
        bagSlots = new ArrayList<>();
        contentSlots = new ArrayList<>();
        for (int i = 0; i < inv.contents.size(); ) {
            if (i < inv.contentsOrdinal) {
                bagSlots.add(new Slot(inv, i, bX, bY));
                bY += 18;
                i++;
            } else {
                contentSlots.add(new Slot(inv, i, contX, contY));
                contX += 18;
                i++;
                if (i % 10 == 0) {
                    contX = bX + 23;
                    contY += 18;
                }
            }
        }
    }

    @Inject(method = "isClickOutsideBounds", at = @At("HEAD"), cancellable = true)
    public void addMoreBounds(double mouseX, double mouseY, int left, int top, int button, CallbackInfoReturnable<Boolean> ctx) {
        if (mouseX > bagX && mouseY > bagY && mouseX < bagX + bagW && mouseY < bagY + bagH) ctx.setReturnValue(false);
    }

    public void renderSlot(MatrixStack matrices, Slot slot, int mouseX, int mouseY) {
        matrices.push();
        RenderSystem.disableLighting();
        RenderSystem.disableDepthTest();
        this.drawSlot(matrices, slot);
        if (this.isPointOverSlot(slot, mouseX, mouseY) && slot.doDrawHoveringEffect()) {
            this.focusedSlot = slot;
            RenderSystem.disableDepthTest();
            RenderSystem.colorMask(true, true, true, false);
            this.fillGradient(matrices, slot.x, slot.y, slot.x + 16, slot.y + 16, -2130706433, -2130706433);
            RenderSystem.colorMask(true, true, true, true);
        }
        matrices.pop();
    }
}
