package mods.hallofween.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import mods.hallofween.Config;
import mods.hallofween.bags.BagHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;

public class PlayerData {
    public int bagSlots;
    public final boolean hasExpansions;
    public final int[] titles;
    public final int[] customTitles;

    public PlayerData(boolean hasExpansions, int[] titles, int[] customTitles) {
        this.bagSlots = BagHandler.getInitialBagCount(hasExpansions);
        this.hasExpansions = hasExpansions;
        this.titles = titles;
        this.customTitles = customTitles;
    }

    public PlayerData(int bagSlots, boolean hasExpansions, int[] titles, int[] customTitles) {
        this.bagSlots = bagSlots;
        this.hasExpansions = hasExpansions;
        this.titles = titles;
        this.customTitles = customTitles;
    }

    public static void fromJson(JsonObject json, PlayerEntity player) {
        boolean access;
        if (json.has("hasExpansions")) {
            access = json.get("hasExpansions").getAsBoolean();
        } else {
            access = false;
        }

        int bagSlots;
        if (json.has("unlockedBagSlotCount")) {
            bagSlots = MathHelper.clamp(json.get("unlockedBagSlotCount").getAsInt(), 3, 10);
        } else {
            bagSlots = BagHandler.getInitialBagCount(access);
        }

        int[] titles, custom = titles = PlayerDataManager.EMPTY_TITLES;
        if (json.has("titles")) {
            JsonObject obj = json.getAsJsonObject("titles");
            if (Config.enableOfficialTitles && obj.has("official")) {
                JsonArray arr = obj.getAsJsonArray("official");
                titles = new int[arr.size()];
                for (int i = 0; i < arr.size(); i++) {
                    titles[i] = arr.get(i).getAsInt();
                }
            }
            if (Config.enableCustomTitles && obj.has("custom")) {
                JsonArray arr = obj.getAsJsonArray("custom");
                custom = new int[arr.size()];
                for (int i = 0; i < arr.size(); i++) {
                    custom[i] = arr.get(i).getAsInt();
                }
            }
        }

        PlayerDataManager.DATA.put(player.getUuid(), new PlayerData(bagSlots, access, titles, custom));
    }

    public static JsonObject toJson(PlayerEntity player) {
        JsonObject obj = new JsonObject();
        obj.addProperty("hasExpansions", PlayerDataManager.hasExpansions(player));
        obj.addProperty("unlockedBagSlotCount", PlayerDataManager.getUnlockedBagSlotCount(player));
        JsonObject titles = new JsonObject();
        JsonArray official = new JsonArray(), custom = new JsonArray();
        for (int i : PlayerDataManager.getTitles(player)) {
            official.add(i);
        }
        for (int i : PlayerDataManager.getCustomTitles(player)) {
            custom.add(i);
        }
        titles.add("official", official);
        titles.add("custom", custom);
        obj.add("titles", titles);
        return obj;
    }
}
