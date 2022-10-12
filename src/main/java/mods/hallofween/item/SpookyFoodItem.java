package mods.hallofween.item;

import mods.hallofween.HallOfWeen;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SpookyFoodItem extends Item {
    public int xp;

    public SpookyFoodItem(int xp, FoodComponent.Builder foodComp) {
        super(new FabricItemSettings().group(HallOfWeen.ITEMGROUP).food(foodComp.build()));
        this.xp = xp;
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (user instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) user;
            player.addExperience(xp);
        }
        return user.eatFood(world, stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(new TranslatableText("text.hallofween.xp", this.xp).formatted(Formatting.GREEN));
        super.appendTooltip(stack, world, tooltip, context);
    }
}
