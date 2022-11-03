package mods.hallofween.registry;

import com.google.gson.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mods.hallofween.Config;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static mods.hallofween.util.HallOfWeenUtil.L;

public class ContainerRegistry {
    public static Map<String, ContainerProperties> CONTAINERS = new Object2ObjectOpenHashMap<>();
    public static Map<String, Map<Predicate<Identifier>, ContainerLootProperties>> LOOT_PREDICATES = new Object2ObjectOpenHashMap<>();

    public static void load(ResourceManager manager) {
        CONTAINERS.clear();
        LOOT_PREDICATES.clear();
        Collection<Identifier> data = manager.findResources("containers", (string) -> string.endsWith(".json"));
        Gson GSON = new GsonBuilder().create();

        for (Identifier id : data) {
            String name = id.getPath().substring(11, id.getPath().length() - 5);
            if (Config.disableDefaultLootContainers && name.startsWith("tot/")) continue;
            else if (name.startsWith("tot/")) name = name.substring(4);
            try {
                InputStream is = manager.getResource(id).getInputStream();
                JsonObject json = GSON.fromJson(new InputStreamReader(is), JsonObject.class);

                String display = "";
                Identifier modelId = null;
                List<String> tt = new ArrayList<>();
                int bC = 0xFFFFFF, mC = 0xFFFFFF;

                if (json.has("name")) {
                    display = json.get("name").getAsString();
                }
                if (json.has("model")) {
                    String s = json.get("model").getAsString();
                    if (s.contains(":")) {
                        String[] ss = s.split(":");
                        modelId = new Identifier(ss[0], "container/" + ss[1]);
                    } else {
                        modelId = new Identifier("container/" + s);
                    }
                }
                if (json.has("tooltips")) {
                    for (JsonElement e : json.getAsJsonArray("tooltips")) {
                        tt.add(e.getAsString());
                    }
                }
                if (json.has("bagColor")) {
                    JsonElement e = json.get("bagColor");
                    String s = e.getAsString();
                    if (s.startsWith("0x"))
                        bC = Integer.parseInt(s.substring(2), 16);
                    else
                        bC = e.getAsInt();
                }
                if (json.has("overlayColor")) {
                    JsonElement e = json.get("overlayColor");
                    String s = e.getAsString();
                    if (s.startsWith("0x"))
                        mC = Integer.parseInt(s.substring(2), 16);
                    else
                        mC = e.getAsInt();
                }

                Map<Predicate<Identifier>, ContainerLootProperties> map = new Object2ObjectLinkedOpenHashMap<>();
                if (json.has("injection_predicates")) {
                    JsonArray a = json.getAsJsonArray("injection_predicates");
                    for (JsonElement e : a) {
                        JsonObject obj = e.getAsJsonObject();
                        Predicate<Identifier> p = null;
                        int min = 1;
                        int max = 1;
                        float chance = 1f;
                        for (Map.Entry<String, JsonElement> kv : obj.entrySet()) {
                            String v = kv.getValue().getAsString();
                            switch (kv.getKey()) {
                                case "namespace":
                                    p = s -> s.getNamespace().equals(v);
                                    break;
                                case "contains":
                                    p = s -> s.getPath().contains(v);
                                    break;
                                case "negate_contains":
                                    p = s -> !s.getPath().contains(v);
                                    break;
                                case "starts_with":
                                    p = s -> s.getPath().startsWith(v);
                                    break;
                                case "negate_starts_with":
                                    p = s -> !s.getPath().startsWith(v);
                                    break;
                                case "ends_with":
                                    p = s -> s.getPath().endsWith(v);
                                    break;
                                case "negate_ends_with":
                                    p = s -> !s.getPath().endsWith(v);
                                    break;
                                case "min_amount":
                                    min = Integer.parseInt(v);
                                    break;
                                case "max_amount":
                                    max = Integer.parseInt(v);
                                    break;
                                case "chance":
                                    chance = MathHelper.clamp(Float.parseFloat(v), 0, 1);
                                    break;
                                default:
                                    break;
                            }
                        }
                        map.put(p, new ContainerLootProperties(min, Math.max(min, max), chance));
                    }
                }
                CONTAINERS.put(name, new ContainerProperties(display, modelId, bC, mC, tt));
                LOOT_PREDICATES.put(name, map);
            } catch (IOException e) {
                L.error(e.getMessage());
            }
        }
    }

    public static class ContainerProperties {
        public String name;
        @Nullable
        public Identifier modelId;
        public List<String> tooltips;
        public int bagColor, overlayColor;

        public ContainerProperties(String name, @Nullable Identifier modelId, int bagColor, int overlayColor, List<String> tooltips) {
            this.name = name;
            this.modelId = modelId;
            this.bagColor = bagColor;
            this.overlayColor = overlayColor;
            this.tooltips = tooltips.isEmpty() ? null : tooltips;
        }
    }

    public static class ContainerLootProperties {
        public int min, max;
        public float chance;

        public ContainerLootProperties(int min, int max, float chance) {
            this.min = min;
            this.max = max;
            this.chance = chance;
        }
    }
}
