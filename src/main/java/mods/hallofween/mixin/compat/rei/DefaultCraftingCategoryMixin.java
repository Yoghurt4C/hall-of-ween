package mods.hallofween.mixin.compat.rei;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.gui.widget.Widget;
import me.shedaniel.rei.plugin.crafting.DefaultCraftingCategory;
import me.shedaniel.rei.plugin.crafting.DefaultCraftingDisplay;
import mods.hallofween.compat.rei.SheetWidget;
import mods.hallofween.discovery.DiscoveryRecipe;
import net.minecraft.advancement.Advancement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.recipe.Recipe;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(DefaultCraftingCategory.class)
public abstract class DefaultCraftingCategoryMixin {
    @Inject(method = "setupDisplay(Lme/shedaniel/rei/plugin/crafting/DefaultCraftingDisplay;Lme/shedaniel/math/Rectangle;)Ljava/util/List;", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILHARD, remap = false)
    public void addDiscoveryInfo(DefaultCraftingDisplay display, Rectangle bounds, CallbackInfoReturnable<List<Widget>> ctx, Point startPoint, List<Widget> widgets) {
        if (display.getOptionalRecipe().isPresent()) {
            Recipe<?> recipe = display.getOptionalRecipe().get();
            if (recipe instanceof DiscoveryRecipe) {
                Identifier advId = ((DiscoveryRecipe) recipe).getAdvancement();
                if (advId == null) return;
                MinecraftClient mc = MinecraftClient.getInstance();
                Advancement adv = mc.getNetworkHandler().getAdvancementHandler().getManager().get(advId);
                widgets.add(new SheetWidget(startPoint, display.getOutputEntries().get(0).getItemStack(), advId, adv == null));
            }
        }
    }
}
