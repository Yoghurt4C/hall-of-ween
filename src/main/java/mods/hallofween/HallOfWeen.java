package mods.hallofween;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mods.hallofween.item.ContainerItem;
import mods.hallofween.item.RecipeSheetItem;
import mods.hallofween.registry.ContainerRegistry;
import mods.hallofween.registry.ContainerRegistry.ContainerLootProperties;
import mods.hallofween.registry.ContainerRegistry.ContainerProperties;
import mods.hallofween.registry.HallOfWeenBlocks;
import mods.hallofween.registry.HallOfWeenItems;
import mods.hallofween.registry.HallOfWeenNetworking;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.loot.v1.FabricLootPoolBuilder;
import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.ConstantLootTableRange;
import net.minecraft.loot.UniformLootTableRange;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.function.SetNbtLootFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.function.Predicate;

public class HallOfWeen implements ModInitializer {
    public static final Identifier DEFAULTID = new Identifier("hallofween", "hallofween");
    public static Map<Identifier, Identifier> DISCOVERY = new Object2ObjectOpenHashMap<>();
    public static ItemGroup ITEMGROUP = FabricItemGroupBuilder.create(DEFAULTID)
            .icon(() -> new ItemStack(getItem("testificate")))
            .build();
    public static ItemGroup CONTAINER_GROUP = FabricItemGroupBuilder.create(getId("containers"))
            .icon(ContainerItem::getDefaultContainer)
            .appendItems(ContainerRegistry::appendItems)
            .build();
    public static ItemGroup DISCOVERY_GROUP = FabricItemGroupBuilder.create(getId("discovery"))
            .icon(() -> new ItemStack(getItem("recipe_sheet")))
            .appendItems(RecipeSheetItem::appendStacks)
            .build();
    public static final Logger L = LogManager.getLogger("Hall of Ween");

    @Override
    public void onInitialize() {
        Config.tryInit();

        HallOfWeenBlocks.init();
        HallOfWeenItems.init();
        HallOfWeenNetworking.init();

        if (Config.injectTestificatesIntoLootTables) {
            LootTableLoadingCallback.EVENT.register((resourceManager, manager, id, supplier, setter) -> {
                if (id.getPath().startsWith("chests") && !id.getPath().contains("village")) {
                    FabricLootPoolBuilder b = FabricLootPoolBuilder.builder()
                            .rolls(ConstantLootTableRange.create(1))
                            .with(ItemEntry.builder(getItem("testificate")))
                            .withFunction(SetCountLootFunction.builder(ConstantLootTableRange.create(1)).build())
                            .withCondition(RandomChanceLootCondition.builder(Config.testificateChance).build());
                    supplier.withPool(b.build());
                }
            });
        }

        if (Config.injectLootContainers) {
            LootTableLoadingCallback.EVENT.register((resourceManager, manager, id, supplier, setter) -> {
                for (Map.Entry<String, ContainerLootProperties> e : ContainerRegistry.LOOT_PREDICATES.entrySet()) {
                    Predicate<Identifier> p = e.getValue().predicate;
                    if (p.test(id)) {
                        String name = e.getKey();
                        ContainerProperties props = ContainerRegistry.CONTAINERS.get(name);
                        ContainerLootProperties lootProps = e.getValue();
                        CompoundTag tag = new CompoundTag();
                        tag.putString("bagId", name);
                        tag.putInt("bagColor", props.bagColor);
                        tag.putInt("overlayColor", props.overlayColor);
                        FabricLootPoolBuilder b = FabricLootPoolBuilder.builder()
                                .rolls(ConstantLootTableRange.create(1))
                                .with(ItemEntry.builder(getItem("container")))
                                .withFunction(SetNbtLootFunction.builder(tag).build())
                                .withFunction(SetCountLootFunction.builder(UniformLootTableRange.between(lootProps.min, lootProps.max)).build())
                                .withCondition(RandomChanceLootCondition.builder(lootProps.chance).build());
                        supplier.withPool(b.build());
                    }
                }
            });
        }

    }

    public static String getModId() {
        return DEFAULTID.getNamespace();
    }

    public static Identifier getId(String id) {
        return new Identifier(getModId(), id);
    }

    public static Item getItem(String id) {
        return Registry.ITEM.get(getId(id));
    }
}
