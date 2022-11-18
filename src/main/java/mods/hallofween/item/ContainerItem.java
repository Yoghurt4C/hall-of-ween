package mods.hallofween.item;

import mods.hallofween.HallOfWeen;
import mods.hallofween.registry.ContainerRegistry;
import mods.hallofween.util.ContainerUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

import static mods.hallofween.registry.ContainerRegistry.CONTAINERS;
import static mods.hallofween.registry.ContainerRegistry.ContainerProperties;
import static mods.hallofween.util.HallOfWeenUtil.getItem;

public class ContainerItem extends Item {
    public ContainerItem() {
        super(new FabricItemSettings().group(HallOfWeen.CONTAINER_GROUP));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (world.isClient())
            return TypedActionResult.fail(stack);
        else {
            dropLoot(stack, (ServerWorld) world, (ServerPlayerEntity) user);
            return TypedActionResult.success(stack, true);
        }
    }

    private void dropLoot(ItemStack stack, ServerWorld world, ServerPlayerEntity player) {
        LootTable table = ContainerUtils.getLootTableForContainer(stack, world);
        if (table != LootTable.EMPTY) {
            LootContext ctx = ContainerUtils.getLootContext(world, player);
            table.generateLoot(ctx, (s) -> player.inventory.offerOrDrop(world, s));
        }
        stack.decrement(1);
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains("bagId")) {
            String id = stack.getTag().getString("bagId");
            if (CONTAINERS.containsKey(id)) {
                return CONTAINERS.get(id).name;
            }
        }
        return this.getTranslationKey();
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        if (stack.hasTag()) {
            if (stack.getTag().contains("bagId")) {
                String id = stack.getTag().getString("bagId");
                if (CONTAINERS.containsKey(id)) {
                    ContainerProperties bp = CONTAINERS.get(id);
                    if (bp.tooltips != null)
                        bp.tooltips.forEach(s -> tooltip.add(new TranslatableText(s)));
                    return;
                }
            }
        }
        tooltip.add(new TranslatableText("text.container.empty").formatted(Formatting.DARK_PURPLE, Formatting.ITALIC));
    }

    @Override
    public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks) {
        if (group.equals(HallOfWeen.CONTAINER_GROUP)) {
            for (Map.Entry<String, ContainerProperties> e : ContainerRegistry.CONTAINERS.entrySet()) {
                ItemStack stack = new ItemStack(getItem("container"));
                ContainerProperties props = e.getValue();
                stack.getOrCreateTag().putString("bagId", e.getKey());
                if (props.bagColor != 0xFFFFFF) stack.getTag().putInt("bagColor", props.bagColor);
                if (props.overlayColor != 0xFFFFFF) stack.getTag().putInt("overlayColor", props.overlayColor);
                stacks.add(stack);
            }
        }
    }

    @Environment(EnvType.CLIENT)
    public static int getColor(ItemStack stack, int layer) {
        if (stack.hasTag()) {
            CompoundTag tag = stack.getTag();
            if (tag.contains("bagColor") && tag.contains("overlayColor")) {
                int bagColor = tag.getInt("bagColor");
                int magicColor = tag.getInt("overlayColor");
                return layer == 0 ? bagColor : magicColor;
            }
        }
        return 0xFFFFFF;
    }
}
