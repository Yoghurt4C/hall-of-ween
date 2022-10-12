package mods.hallofween.item;

import mods.hallofween.HallOfWeen;
import mods.hallofween.registry.ToTRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TrickOrTreatBagItem extends Item {
    public TrickOrTreatBagItem() {
        super(new FabricItemSettings());
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (world.isClient())
            return TypedActionResult.fail(stack);
        else {
            dropLoot(stack, (ServerWorld) world, user);
            return TypedActionResult.success(stack, true);
        }
    }

    private void dropLoot(ItemStack stack, ServerWorld world, PlayerEntity player) {
        if (stack.hasTag()) {
            if (stack.getTag().contains("totId")) {
                String id = stack.getTag().getString("totId");
                if (ToTRegistry.BAGS.containsKey(id)) {
                    ToTRegistry.ToTBagProperties bp = ToTRegistry.BAGS.get(id);
                    Identifier identifier = HallOfWeen.getId("tot_bags/" + id);
                    LootTable lootTable = world.getServer().getLootManager().getTable(identifier);
                    LootContext ctx = new LootContext.Builder(world)
                            .parameter(LootContextParameters.THIS_ENTITY, player)
                            .random(world.random)
                            .build(LootContextTypes.BARTER);
                    lootTable.generateLoot(ctx, (s) -> player.inventory.offerOrDrop(world, s));
                }
            }
        }
        stack.decrement(1);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        if (stack.hasTag()) {
            if (stack.getTag().contains("totId")) {
                String id = stack.getTag().getString("totId");
                if (ToTRegistry.BAGS.containsKey(id)) {
                    ToTRegistry.ToTBagProperties bp = ToTRegistry.BAGS.get(id);
                    if (bp.tooltips != null)
                        bp.tooltips.forEach(s -> tooltip.add(new TranslatableText(s)));
                    return;
                }
            }
        }
        tooltip.add(new TranslatableText("text.trick_or_treat_bag.empty").formatted(Formatting.DARK_PURPLE, Formatting.ITALIC));
    }

    @Environment(EnvType.CLIENT)
    public static int getColor(ItemStack stack, int layer) {
        if (stack.hasTag()) {
            CompoundTag tag = stack.getTag();
            if (tag.contains("bagColor") && tag.contains("magicColor")) {
                int bagColor = tag.getInt("bagColor");
                int magicColor = tag.getInt("magicColor");
                return layer == 0 ? bagColor : magicColor;
            }
        }
        return layer == 0 ? 0xE38A1D : 0x9F3C9F;
    }
}
