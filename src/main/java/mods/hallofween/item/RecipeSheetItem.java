package mods.hallofween.item;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static mods.hallofween.HallOfWeen.DISCOVERY;
import static mods.hallofween.HallOfWeen.getItem;

public class RecipeSheetItem extends Item {
    public RecipeSheetItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if (stack.hasTag() && stack.getTag().contains("targetItem")) {
            Identifier id = new Identifier(stack.getTag().getString("targetItem"));
            MutableText item = Registry.ITEM.get(id).getName().copy();
            tooltip.add(new TranslatableText("text.hallofween.recipe_sheet")
                    .append(" ")
                    .append(item.formatted(Formatting.BOLD))
                    .append("."));
        } else tooltip.add(new TranslatableText("text.hallofween.recipe_sheet_empty"));
        super.appendTooltip(stack, world, tooltip, context);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public Text getName(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains("targetItem")) {
            Item item = Registry.ITEM.get(new Identifier(stack.getTag().getString("targetItem")));
            return new TranslatableText(this.getTranslationKey())
                    .append(": ")
                    .append(item.getName());
        }
        return super.getName(stack);
    }

    public static void appendStacks(List<ItemStack> list) {
        for (Identifier id : DISCOVERY.keySet()) {
            ItemStack stack = new ItemStack(getItem("recipe_sheet"));
            stack.getOrCreateTag().putString("targetItem", id.toString());
            list.add(stack);
        }
    }
}
