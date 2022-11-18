package mods.hallofween.data;

import com.google.gson.*;
import mods.hallofween.bags.BagHandler;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static mods.hallofween.util.HallOfWeenUtil.L;

public class PlayerDataManager {

    public static boolean loadPlayerData(Path path, ServerPlayerEntity player) throws IOException {
        Gson gson = new GsonBuilder().create();
        Path data = FabricLoader.getInstance().isDevelopmentEnvironment()
                ? path.resolve("devenv.json")
                : path.resolve(player.getUuidAsString() + ".json");
        if (Files.exists(data)) {
            try (BufferedReader br = Files.newBufferedReader(data, StandardCharsets.UTF_8)) {
                JsonObject obj = gson.fromJson(br, JsonObject.class);
                JsonArray bags = obj.get("bag_inventory").getAsJsonArray();
                BagHandler.loadPlayerData(bags, player);
                return true;
            } catch (IllegalStateException e) {
                L.warn("[Hall of Ween] Couldn't load player data file for {}!", player.getName().asString());
                L.error(e.getMessage());
            }
        }
        return false;
    }

    public static String savePlayerData(Path path, World world, List<ServerPlayerEntity> players) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        int i = 0;
        for (ServerPlayerEntity player : players) {
            if (world == player.world) {
                i++;
                Path playerPath = FabricLoader.getInstance().isDevelopmentEnvironment()
                        ? path.resolve("devenv.json")
                        : path.resolve(player.getUuidAsString() + ".json");
                try (BufferedWriter bw = Files.newBufferedWriter(playerPath, StandardCharsets.UTF_8)) {
                    JsonObject obj = new JsonObject();
                    JsonElement bags = BagHandler.getPlayerData(player);
                    if (bags.isJsonArray()) obj.add("bag_inventory", bags.getAsJsonArray());
                    gson.toJson(obj, bw);
                } catch (IOException e) {
                    L.warn("[Hall of Ween] Couldn't create a player data file for {}!", player.getName().asString());
                    L.error(e.getMessage());
                }
            }
        }
        return "[Hall of Ween] Saved Player Data for " + i + " players.";
    }
}
