package mods.hallofween.util;

import mods.hallofween.mixin.containers.LootPoolAccessor;
import mods.hallofween.mixin.containers.LootTableAccessor;
import mods.hallofween.registry.ContainerRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.entry.LootPoolEntry;
import net.minecraft.loot.function.ApplyBonusLootFunction;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import static mods.hallofween.registry.ContainerRegistry.CONTAINERS;

public interface ContainerUtils {

    static ItemStack getDefaultContainer() {
        ItemStack stack = new ItemStack(HallOfWeenUtil.getItem("container"));
        stack.getOrCreateTag().putString("bagId", "trick_or_treat_bag");
        stack.getTag().putInt("bagColor", 0xE3901D);
        stack.getTag().putInt("overlayColor", 0x9F3C9F);
        return stack;
    }

    static LootTable getLootTableForContainer(ItemStack stack, ServerWorld world) {
        if (stack.hasTag()) {
            if (stack.getTag().contains("bagId")) {
                String id = stack.getTag().getString("bagId");
                if (CONTAINERS.containsKey(id)) {
                    ContainerRegistry.ContainerProperties bp = CONTAINERS.get(id);
                    Identifier identifier = HallOfWeenUtil.getId("containers/" + id);
                    return world.getServer().getLootManager().getTable(identifier);
                }
            }
        }
        return LootTable.EMPTY;
    }

    static LootContext getLootContext(ServerWorld world, ServerPlayerEntity player) {
        return new LootContext.Builder(world)
                .parameter(LootContextParameters.THIS_ENTITY, player)
                .random(world.random)
                .build(LootContextTypes.BARTER);
    }

    //todo setup screen
    static Map<Integer, ItemStack> getPreviewStacks(LootTable table, LootContext ctx) {
        Map<Integer, ItemStack> map = new TreeMap<>(Comparator.reverseOrder());
        for (LootPool lp : ((LootTableAccessor) table).getPools()) {
            LootPoolAccessor pool = (LootPoolAccessor) lp;
            for (LootPoolEntry entry : pool.getEntries()) {
                entry.expand(ctx, choice -> {
                    int i = choice.getWeight(0);
                    choice.generateLoot(stack -> {
                        for (LootFunction function : pool.getFunctions()) {
                            if (function instanceof SetCountLootFunction || function instanceof ApplyBonusLootFunction)
                                continue;
                            stack = function.apply(stack, ctx);
                        }
                        map.put(i, stack);
                    }, ctx);
                });
            }
        }

        return map;
    }
}
