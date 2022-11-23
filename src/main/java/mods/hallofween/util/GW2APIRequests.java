package mods.hallofween.util;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import mods.hallofween.Config;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public interface GW2APIRequests {
    static HttpURLConnection sendRequest(String api) throws IOException {
        URL url = new URL("https://api.guildwars2.com/v2/" + api);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestProperty("Accept", "application/json");
        con.setRequestProperty("Authorization", "Bearer " + Config.gw2ApiKey);
        con.setRequestMethod("GET");
        con.setConnectTimeout(2500);
        con.setReadTimeout(2500);

        con.connect();
        return con;
    }

    static JsonObject getAccountInfo() throws ExecutionException, InterruptedException {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Gson gson = new GsonBuilder().create();
                HttpURLConnection con = sendRequest("account");
                JsonObject obj = gson.fromJson(new JsonReader(new InputStreamReader(con.getInputStream())), JsonObject.class);
                con.disconnect();
                return obj;
            } catch (IOException e) {
                HallOfWeenUtil.L.error("[Hall of Ween] Couldn't get valid response from the API.");
                HallOfWeenUtil.L.error(e.getMessage());
                return new JsonObject();
            }
        }).join();
    }

    /**
     * @return List of title IDs ([1, 2, 3])
     */
    static JsonArray getPlayerTitles() throws ExecutionException, InterruptedException {
        Gson gson = new GsonBuilder().create();
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpURLConnection con = sendRequest("account/titles");
                JsonArray arr = gson.fromJson(new JsonReader(new InputStreamReader(con.getInputStream())), JsonArray.class);
                con.disconnect();
                return arr;
            } catch (IOException e) {
                HallOfWeenUtil.L.error("[Hall of Ween] Couldn't get valid response from the API.");
                HallOfWeenUtil.L.error(e.getStackTrace());
                return new JsonArray();
            }
        }).join();
    }

    /**
     * @return List of title data objects ([{}, {}])
     */
    static JsonArray getAllTitles() throws ExecutionException, InterruptedException {
        Gson gson = new GsonBuilder().create();
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpURLConnection con = sendRequest("titles?ids=all");
                JsonArray arr = gson.fromJson(new JsonReader(new InputStreamReader(con.getInputStream())), JsonArray.class);
                con.disconnect();
                return arr;
            } catch (IOException e) {
                HallOfWeenUtil.L.error("[Hall of Ween] Couldn't get valid response from the API.");
                HallOfWeenUtil.L.error(e.getStackTrace());
                return new JsonArray();
            }
        }).join();
    }

    static JsonArray getTitleData(Gson gson, JsonArray json) {
        if (json.size() == 0) return json;
        StringBuilder b = new StringBuilder("titles?ids=");
        Iterator<JsonElement> it = json.iterator();
        while (it.hasNext()) {
            b.append(it.next());
            if (it.hasNext()) b.append(",");
        }
        try {
            HttpURLConnection con = sendRequest(b.toString());
            JsonArray arr = gson.fromJson(new JsonReader(new InputStreamReader(con.getInputStream())), JsonArray.class);
            con.disconnect();
            return arr;
        } catch (IOException e) {
            HallOfWeenUtil.L.error("[Hall of Ween] Couldn't get valid response from the API.");
            HallOfWeenUtil.L.error(e.getStackTrace());
            return new JsonArray();
        }
    }
}
