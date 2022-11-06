package mods.hallofween.registry;

import mods.hallofween.entity.ThrownRottenEgg;
import mods.hallofween.entity.ThrownToiletPaper;
import mods.hallofween.item.*;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static mods.hallofween.HallOfWeen.ITEMGROUP;
import static mods.hallofween.util.HallOfWeenUtil.getId;
import static net.minecraft.entity.effect.StatusEffects.*;

public class HallOfWeenItems {
    public static void init() {
        register("testificate", new TestificateItem(generic().maxCount(1).equipmentSlot(stack -> EquipmentSlot.HEAD)));

        register("container", new ContainerItem());

        register("candy_corn", new SpookyFoodItem(1, spook(1, 0.1f).snack()) {
            @Override
            public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
                tooltip.add(new TranslatableText("text.hallofween.sugar_rush").formatted(Formatting.YELLOW));
                super.appendTooltip(stack, world, tooltip, context);
            }

            @SuppressWarnings("all")
            @Override
            public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
                int i = 60;
                int k = 0;
                if (user.hasStatusEffect(SPEED)) {
                    i += user.getStatusEffect(SPEED).getDuration();
                    k = user.getStatusEffect(SPEED).getAmplifier();
                    user.removeStatusEffect(SPEED);
                }
                user.addStatusEffect(new StatusEffectInstance(SPEED, i, k));
                return super.finishUsing(stack, world, user);
            }
        });

        register("chattering_skull", new Item(generic()));
        register("nougat_center", new Item(generic()));
        register("plastic_fangs", new Item(generic()));

        register("candied_apple", new SpookyFoodItem(3, spook(3, 0.3f, HASTE, 200, 0, 0.75f)));
        register("candy_corn_custard", new SpookyFoodItem(5, spook(5, 0.45f, REGENERATION, 200, 0, 1f)));
        register("candy_corn_cake_slice", new SpookyFoodItem(2, spook(2, 0.12f, LUCK, 200, 0, 1f)));
        register("strawberry_ghost", new SpookyFoodItem(3, spook(3, 0.3f, JUMP_BOOST, 600, 0, 0.75f)));
        register("candy_corn_cookie", new SpookyFoodItem(2, spook(2, 0.15f, RESISTANCE, 200, 0, 0.75f)));
        register("candy_corn_almond_brittle", new SpookyFoodItem(3, spook(3, 0.25f, LUCK, 200, 1, 0.25f)));
        register("glazed_pear_tart", new SpookyFoodItem(5, spook(6, 0.275f, STRENGTH, 600, 0, 1f)));
        register("glazed_pumpkin_pie", new SpookyFoodItem(5, spook(7, 0.29f, REGENERATION, 600, 0, 1f)));
        register("glazed_chocolate_raspberry_cookie", new SpookyFoodItem(3, spook(3, 0.25f, RESISTANCE, 200, 1, 0.75f)));
        register("glazed_peach_tart", new SpookyFoodItem(6, spook(5, 0.315f, STRENGTH, 200, 1, 1f)));
        register("homemade_campfire_treat", new SpookyFoodItem(4, spook(4, 0.26f, LUCK, 600, 1, 0.25f)));
        register("omnomberry_ghost", new SpookyFoodItem(3, spook(5, 0.375f, ABSORPTION, 200, 1, 0.75f)));
        register("saint_bones", new SpookyFoodItem(8, spook(2, 0.18f, LEVITATION, 100, 0, 0.25f)));
        register("soul_pastry", new SpookyFoodItem(5, spook(4, 0.23f, GLOWING, 200, 0, 0.75f)));
        register("spicy_pumpkin_cookie", new SpookyFoodItem(6, spook(3, 0.21f, FIRE_RESISTANCE, 600, 0, 1f)));

        register("candy_corn_glaze", new Item(generic().recipeRemainder(Items.BOWL)));

        register("rotten_egg", new ThrownSpookyItem(ThrownRottenEgg::new));
        register("toilet_paper", new ThrownSpookyItem(ThrownToiletPaper::new));

        register("recipe_sheet", new RecipeSheetItem());

        register("testbag", new BagItem(generic()));
    }

    private static FabricItemSettings generic() {
        return new FabricItemSettings().group(ITEMGROUP);
    }

    private static void register(String id, Item item) {
        Registry.register(Registry.ITEM, getId(id), item);
    }


    private static FoodComponent.Builder spook(int hunger, float saturation) {
        return new FoodComponent.Builder()
                .alwaysEdible()
                .hunger(hunger)
                .saturationModifier(saturation);
    }

    private static FoodComponent.Builder spook(int hunger, float saturation, StatusEffect pot, int duration, int amp, float chance) {
        return spook(hunger, saturation)
                .statusEffect(new StatusEffectInstance(pot, duration, amp), chance);
    }
}
