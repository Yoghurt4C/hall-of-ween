package mods.hallofween.discovery;

import mods.hallofween.mixin.discovery.CraftingInventoryAccessor;
import mods.hallofween.mixin.discovery.CraftingScreenHandlerAccessor;
import mods.hallofween.mixin.discovery.PlayerScreenHandlerAccessor;
import net.minecraft.advancement.Advancement;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public interface DiscoveryRecipe {
    Identifier getAdvancement();

    @SuppressWarnings("all")
    static boolean matchesAdvancement(DiscoveryRecipe recipe, CraftingInventory inv, World world) {
        if (recipe.getAdvancement() != null && !world.isClient()) {
            ScreenHandler handler = ((CraftingInventoryAccessor) inv).getHandler();
            PlayerEntity player;
            if (handler instanceof PlayerScreenHandler) {
                player = ((PlayerScreenHandlerAccessor) handler).getOwner();
            } else if (handler instanceof CraftingScreenHandler) {
                player = ((CraftingScreenHandlerAccessor) handler).getPlayer();
            } else /*give up*/ return false;
            Advancement adv = world.getServer().getAdvancementLoader().get(recipe.getAdvancement());
            return ((ServerPlayerEntity) player).getAdvancementTracker().getProgress(adv).isDone();
        }
        return true;
    }
}
