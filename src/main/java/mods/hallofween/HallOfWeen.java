package mods.hallofween;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.shedaniel.cloth.api.common.events.v1.WorldSaveCallback;
import me.shedaniel.cloth.api.utils.v1.GameInstanceUtils;
import mods.hallofween.data.PlayerDataManager;
import mods.hallofween.data.TitleData;
import mods.hallofween.mixin.MinecraftServerAccessor;
import mods.hallofween.mixin.WorldSavePathAccessor;
import mods.hallofween.registry.ContainerRegistry;
import mods.hallofween.registry.HallOfWeenBlocks;
import mods.hallofween.registry.HallOfWeenItems;
import mods.hallofween.registry.HallOfWeenNetworking;
import mods.hallofween.util.ContainerUtils;
import mods.hallofween.util.GW2APIRequests;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.loot.v1.FabricLootPoolBuilder;
import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.ConstantLootTableRange;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

import static mods.hallofween.util.HallOfWeenUtil.*;

public class HallOfWeen implements ModInitializer {
    public static Map<Identifier, Identifier> DISCOVERY;
    public static List<TitleData> DEFAULT_TITLES;
    public static List<TitleData> CUSTOM_TITLES;
    public static ItemGroup ITEMGROUP = FabricItemGroupBuilder.create(DEFAULTID)
            .icon(() -> new ItemStack(getItem("testificate")))
            .build();
    public static ItemGroup CONTAINER_GROUP = FabricItemGroupBuilder.create(getId("containers"))
            .icon(ContainerUtils::getDefaultContainer)
            .build();
    public static ItemGroup DISCOVERY_GROUP = FabricItemGroupBuilder.create(getId("discovery"))
            .icon(() -> new ItemStack(getItem("recipe_sheet")))
            .build();

    @Override
    public void onInitialize() {
        Config.tryInit();

        if (Config.enableDiscoveryRecipes) DISCOVERY = new Object2ObjectOpenHashMap<>();

        HallOfWeenBlocks.init();
        HallOfWeenItems.init();
        HallOfWeenNetworking.init();
        ContainerRegistry.init();

        if (Config.enableOfficialTitles) {
            DEFAULT_TITLES = new ArrayList<>();
            ServerLifecycleEvents.SERVER_STARTING.register((server -> {
                try {
                    JsonArray json = GW2APIRequests.getAllTitles();
                    for (JsonElement e : json) {
                        JsonObject obj = e.getAsJsonObject();
                        DEFAULT_TITLES.add(new TitleData(obj.get("id").getAsInt(), obj.get("name").getAsString()));
                    }
                    L.info("[Hall of Ween] Retrieved {} titles from the GW2 API.", json.size());
                } catch (ExecutionException | InterruptedException e) {
                    L.error("[Hall of Ween] Couldn't fetch title data from the API.");
                    L.error(e.getMessage());
                }
            }));
        }

        if (Config.enableCustomTitles) {
            CUSTOM_TITLES = new ArrayList<>();
            //todo
        }

        if (Config.generateDataWarning) {
            ServerLifecycleEvents.SERVER_STARTED.register((server -> {
                MinecraftServerAccessor acc = (MinecraftServerAccessor) GameInstanceUtils.getServer();
                Path path = acc.getSession().getDirectory(WorldSavePathAccessor.constructor("gw2"));
                try {
                    if (Files.notExists(path)) Files.createDirectory(path);
                    Path warning = path.resolve("warning.txt");
                    CompletableFuture.runAsync(() -> {
                        if (Files.notExists(warning)) {
                            try (BufferedWriter bw = Files.newBufferedWriter(warning, StandardCharsets.UTF_8)) {
                                bw.write("While these files are plaintext, please be very careful when changing anything inside.\n" +
                                        "Any invalid change may result in data loss, corruption, irritation and spontaneous baldness.\n" +
                                        "Make manual backups if you *really* need to interact with the files themselves.\n" +
                                        "Do not edit without adult supervision. Consider yourself warned.\n\n" +
                                        "P.S. This file is not deletable without config intervention.");
                            } catch (IOException e) {
                                L.warn("[Hall of Ween] Couldn't write warning file? Oops.");
                                L.error(e.getMessage());
                            }
                        }
                    }, acc.getWorkerExecutor());
                } catch (IOException e) {
                    L.warn("[Hall of Ween] Couldn't create a data folder within the world folder! Partial data loss imminent.");
                    L.error(e.getMessage());
                }
            }));
        }

        WorldSaveCallback.EVENT.register((world, progressListener, flush) -> {
            if (!world.isClient()) {
                MinecraftServer server = world.getServer();
                MinecraftServerAccessor acc = (MinecraftServerAccessor) server;
                Path path = acc.getSession().getDirectory(WorldSavePathAccessor.constructor("gw2"));
                try {
                    if (Files.notExists(path)) Files.createDirectory(path);
                    CompletableFuture
                            .supplyAsync(world::getPlayers, acc.getWorkerExecutor())
                            .thenAcceptAsync((players) -> PlayerDataManager.savePlayerData(path, world, players), acc.getWorkerExecutor())
                            .join();
                } catch (IOException e) {
                    L.warn("[Hall of Ween] Couldn't create the gw2 data folder within the world folder!");
                    L.error(e.getMessage());
                } catch (CompletionException | CancellationException e) {
                    L.warn("[Hall of Ween] Couldn't save player data.");
                    L.error(e.getStackTrace());
                }
            }
        });

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
    }
}
