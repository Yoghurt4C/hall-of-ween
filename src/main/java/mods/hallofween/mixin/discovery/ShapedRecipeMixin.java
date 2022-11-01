package mods.hallofween.mixin.discovery;

import com.google.gson.JsonObject;
import mods.hallofween.HallOfWeen;
import mods.hallofween.discovery.DiscoveryRecipe;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShapedRecipe.class)
public abstract class ShapedRecipeMixin implements DiscoveryRecipe {
    @Unique
    private Identifier advancement = null;

    @Inject(method = "<init>(Lnet/minecraft/util/Identifier;Ljava/lang/String;IILnet/minecraft/util/collection/DefaultedList;Lnet/minecraft/item/ItemStack;)V", at = @At("RETURN"))
    public void setAdvancement(Identifier id, String group, int width, int height, DefaultedList<Ingredient> ingredients, ItemStack output, CallbackInfo ctx) {
        this.advancement = HallOfWeen.DISCOVERY.get(id);
    }

    @Inject(method = "matches(Lnet/minecraft/inventory/CraftingInventory;Lnet/minecraft/world/World;)Z", at = @At("HEAD"), cancellable = true)
    public void matchesAdvancement(CraftingInventory inv, World world, CallbackInfoReturnable<Boolean> ctx) {
        if (!DiscoveryRecipe.matchesAdvancement(this, inv, world)) ctx.setReturnValue(false);
    }

    @Unique
    @Nullable
    @Override
    public Identifier getAdvancement() {
        return this.advancement;
    }

    @Mixin(ShapedRecipe.Serializer.class)
    public abstract static class ShapedRecipeSerializerMixin {
        @Inject(method = "read(Lnet/minecraft/util/Identifier;Lcom/google/gson/JsonObject;)Lnet/minecraft/recipe/ShapedRecipe;", at = @At("HEAD"))
        public void readAdvancement(Identifier id, JsonObject json, CallbackInfoReturnable<ShapedRecipe> ctx) {
            if (json.has("advancement")) {
                HallOfWeen.DISCOVERY.putIfAbsent(id, new Identifier(json.getAsJsonPrimitive("advancement").getAsString()));
            }
        }
    }
}
