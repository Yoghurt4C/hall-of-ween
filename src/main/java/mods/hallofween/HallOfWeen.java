package mods.hallofween;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mods.hallofween.item.RecipeSheetItem;
import mods.hallofween.network.S2CToTSyncMessage;
import mods.hallofween.registry.HallOfWeenBlocks;
import mods.hallofween.registry.HallOfWeenItems;
import mods.hallofween.registry.ToTRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.loot.v1.FabricLootPoolBuilder;
import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.ConstantLootTableRange;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class HallOfWeen implements ModInitializer {
    public static final Identifier DEFAULTID = new Identifier("hallofween", "hallofween");
    public static Map<Identifier, Identifier> DISCOVERY = new Object2ObjectOpenHashMap<>();
    public static ItemGroup ITEMGROUP = FabricItemGroupBuilder.create(DEFAULTID)
            .icon(() -> new ItemStack(getItem("testificate")))
            .build();
    public static ItemGroup TOT_GROUP = FabricItemGroupBuilder.create(getId("tot_bags"))
            .icon(() -> new ItemStack(getItem("trick_or_treat_bag")))
            .appendItems(ToTRegistry::appendItems)
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
        ToTRegistry.init();

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

        ServerPlayConnectionEvents.JOIN.register(S2CToTSyncMessage::send);
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register(S2CToTSyncMessage::send);
    }

    public static Identifier getId(String id) {
        return new Identifier(DEFAULTID.getNamespace(), id);
    }

    public static Item getItem(String id) {
        return Registry.ITEM.get(getId(id));
    }
}
