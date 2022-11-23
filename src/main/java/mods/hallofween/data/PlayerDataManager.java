package mods.hallofween.data;

import com.google.gson.*;
import com.mojang.brigadier.context.CommandContext;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mods.hallofween.Config;
import mods.hallofween.bags.BagHandler;
import mods.hallofween.network.PlayerDataSyncMessage;
import mods.hallofween.util.GW2APIRequests;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.World;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static mods.hallofween.util.HallOfWeenUtil.L;

public interface PlayerDataManager {
    Map<UUID, PlayerData> DATA = new Object2ObjectOpenHashMap<>();
    int[] EMPTY_TITLES = new int[0];

    static boolean loadPlayerData(Path path, ServerPlayerEntity player) throws IOException {
        Gson gson = new GsonBuilder().create();
        Path data = FabricLoader.getInstance().isDevelopmentEnvironment()
                ? path.resolve("devenv.json")
                : path.resolve(player.getUuidAsString() + ".json");
        if (Files.exists(data)) {
            try (BufferedReader br = Files.newBufferedReader(data, StandardCharsets.UTF_8)) {
                JsonObject obj = gson.fromJson(br, JsonObject.class);
                if (obj.has("player_data")) {
                    JsonObject pData = obj.get("player_data").getAsJsonObject();
                    PlayerData.fromJson(pData, player);
                }
                if (obj.has("bag_inventory")) {
                    JsonArray bags = obj.get("bag_inventory").getAsJsonArray();
                    BagHandler.loadPlayerData(bags, player);
                }
            } catch (IllegalStateException e) {
                L.warn("[Hall of Ween] Couldn't load player data file for {}!", player.getName().asString());
                L.error(e.getMessage());
            }
        } else {
            //no data file implies defaults across the board
            DATA.put(player.getUuid(), getDefaultPlayerData());
        }
        return true;
    }

    static void savePlayerData(Path path, World world, List<ServerPlayerEntity> players) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        for (ServerPlayerEntity player : players) {
            if (world == player.world) {
                Path playerPath = FabricLoader.getInstance().isDevelopmentEnvironment()
                        ? path.resolve("devenv.json")
                        : path.resolve(player.getUuidAsString() + ".json");
                try (BufferedWriter bw = Files.newBufferedWriter(playerPath, StandardCharsets.UTF_8)) {
                    JsonObject obj = new JsonObject();
                    JsonElement pData = PlayerData.toJson(player);
                    if (pData.isJsonObject()) obj.add("player_data", pData.getAsJsonObject());
                    JsonElement bags = BagHandler.getPlayerData(player);
                    if (bags.isJsonArray()) obj.add("bag_inventory", bags.getAsJsonArray());
                    gson.toJson(obj, bw);
                } catch (IOException e) {
                    L.warn("[Hall of Ween] Couldn't create a player data file for {}!", player.getName().asString());
                    L.error(e.getMessage());
                }
            }
        }
    }

    @Environment(EnvType.CLIENT)
    static int authCommand(CommandContext<FabricClientCommandSource> ctx) {
        try {
            JsonObject account = GW2APIRequests.getAccountInfo();
            JsonArray jsonAccess = account.getAsJsonArray("access");
            boolean access = false;
            for (JsonElement e : jsonAccess) {
                if (access) break;
                switch (e.getAsString()) {
                    case "GuildWars2":
                    case "HeartOfThorns":
                    case "PathOfFire":
                    case "EndOfDragons":
                        access = true;
                        break;
                }
            }

            int[] official = EMPTY_TITLES;
            if (Config.enableOfficialTitles) {
                JsonArray jsonTitles = GW2APIRequests.getPlayerTitles();
                official = new int[jsonTitles.size()];
                for (int i = 0; i < jsonTitles.size(); i++) {
                    official[i] = jsonTitles.get(i).getAsInt();
                }
            }

            new PlayerDataSyncMessage(new PlayerData(access, official, EMPTY_TITLES)).send();
        } catch (ExecutionException | InterruptedException e) {
            ctx.getSource().sendFeedback(new TranslatableText("text.gw2_api.sync_failure"));
            L.error("[Hall of Ween] GW2 API Authentication failed.");
            L.error(e.getMessage());
        }
        ctx.getSource().sendFeedback(new TranslatableText("text.gw2_api.sync_finish"));
        return 0;
    }

    static PlayerData getDefaultPlayerData() {
        return new PlayerData(false, EMPTY_TITLES, EMPTY_TITLES);
    }

    static boolean hasExpansions(PlayerEntity player) {
        if (DATA.containsKey(player.getUuid())) {
            return DATA.get(player.getUuid()).hasExpansions;
        }
        return false;
    }

    static int getUnlockedBagSlotCount(PlayerEntity player) {
        if (DATA.containsKey(player.getUuid())) {
            return DATA.get(player.getUuid()).bagSlots;
        }
        return BagHandler.getInitialBagCount(hasExpansions(player));
    }

    static int[] getTitles(PlayerEntity player) {
        if (DATA.containsKey(player.getUuid())) {
            return DATA.get(player.getUuid()).titles;
        }
        return EMPTY_TITLES;
    }

    static int[] getCustomTitles(PlayerEntity player) {
        if (DATA.containsKey(player.getUuid())) {
            return DATA.get(player.getUuid()).customTitles;
        }
        return EMPTY_TITLES;
    }
}
