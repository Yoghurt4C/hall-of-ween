package mods.hallofween.registry;

import com.google.gson.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mods.hallofween.Config;
import mods.hallofween.HallOfWeen;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.profiler.Profiler;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Predicate;

import static mods.hallofween.HallOfWeen.getId;
import static mods.hallofween.HallOfWeen.getItem;

public class ToTRegistry implements SimpleResourceReloadListener<Collection<Identifier>> {
    private final Gson GSON = new GsonBuilder().create();
    public static Map<String, ToTBagProperties> BAGS = new Object2ObjectOpenHashMap<>();
    public static Map<String, ToTLootProperties> LOOT_PREDICATES = new Object2ObjectOpenHashMap<>();

    public static void init() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new ToTRegistry());
    }

    @Override
    public CompletableFuture<Collection<Identifier>> load(ResourceManager manager, Profiler profiler, Executor executor) {
        BAGS.clear();
        LOOT_PREDICATES.clear();
        return CompletableFuture.supplyAsync(() -> manager.findResources("tot_bags", (string) -> string.endsWith(".json")), executor);
    }

    @Override
    public CompletableFuture<Void> apply(Collection<Identifier> data, ResourceManager manager, Profiler profiler, Executor executor) {
        return CompletableFuture.runAsync(() -> {
            for (Identifier id : data) {
                String name = id.getPath().substring(9, id.getPath().length() - 5);
                if (Config.disableDefaultToTBag && name.equals("default")) continue;
                try {
                    InputStream is = manager.getResource(id).getInputStream();
                    JsonObject json = GSON.fromJson(new InputStreamReader(is), JsonObject.class);
                    List<String> tt = new ArrayList<>();
                    int bC = 0, mC = 0;
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
                    if (json.has("magicColor")) {
                        JsonElement e = json.get("magicColor");
                        String s = e.getAsString();
                        if (s.startsWith("0x"))
                            mC = Integer.parseInt(s.substring(2), 16);
                        else
                            mC = e.getAsInt();
                    }

                    Predicate<Identifier> p = null;
                    int min = 1;
                    int max = 1;
                    float chance = 1f;
                    if (json.has("injection_predicates")) {
                        JsonArray a = json.getAsJsonArray("injection_predicates");
                        for (JsonElement e : a) {
                            Predicate<Identifier> p2 = null;
                            JsonObject obj = e.getAsJsonObject();
                            for (Map.Entry<String, JsonElement> kv : obj.entrySet()) {
                                Predicate<Identifier> p3;
                                String v = kv.getValue().getAsString();
                                switch (kv.getKey()) {
                                    case "namespace":
                                        p3 = s -> s.getNamespace().equals(v);
                                        break;
                                    case "contains":
                                        p3 = s -> s.getPath().contains(v);
                                        break;
                                    case "negate_contains":
                                        p3 = s -> !s.getPath().contains(v);
                                        break;
                                    case "starts_with":
                                        p3 = s -> s.getPath().startsWith(v);
                                        break;
                                    case "negate_starts_with":
                                        p3 = s -> !s.getPath().startsWith(v);
                                        break;
                                    case "ends_with":
                                        p3 = s -> s.getPath().endsWith(v);
                                        break;
                                    case "negate_ends_with":
                                        p3 = s -> !s.getPath().endsWith(v);
                                        break;
                                    case "min_amount":
                                        min = Integer.parseInt(v);
                                    case "max_amount":
                                        max = Integer.parseInt(v);
                                    case "chance":
                                        chance = MathHelper.clamp(Float.parseFloat(v), 0, 1);
                                    default:
                                        continue;
                                }
                                p2 = p2 == null ? p3 : p2.and(p3);
                            }
                            if (p == null) p = p2;
                            else if (p2 != null) p = p.or(p2);
                        }
                    }

                    BAGS.put(name, new ToTBagProperties(bC, mC, tt));
                    LOOT_PREDICATES.put(name, new ToTLootProperties(p, min, max, chance));
                } catch (IOException e) {
                    HallOfWeen.L.error(e.getMessage());
                }
            }
        }, executor);
    }

    @Override
    public Identifier getFabricId() {
        return getId("tot_bag_reload");
    }

    public static void appendItems(List<ItemStack> list) {
        for (Map.Entry<String, ToTRegistry.ToTBagProperties> e : ToTRegistry.BAGS.entrySet()) {
            ItemStack stack = new ItemStack(getItem("trick_or_treat_bag"));
            stack.getOrCreateTag().putString("totId", e.getKey());
            stack.getTag().putInt("bagColor", e.getValue().bagColor);
            stack.getTag().putInt("magicColor", e.getValue().magicColor);
            list.add(stack);
        }
    }

    public static class ToTBagProperties {
        public List<String> tooltips;
        public int bagColor, magicColor;

        public ToTBagProperties(int bagColor, int magicColor, List<String> tooltips) {
            this.bagColor = bagColor;
            this.magicColor = magicColor;
            this.tooltips = tooltips.isEmpty() ? null : tooltips;
        }

        public ToTBagProperties(int bagColor, int magicColor) {
            this.bagColor = bagColor;
            this.magicColor = magicColor;
        }
    }

    public static class ToTLootProperties {
        public Predicate<Identifier> predicate;
        public int min, max;
        public float chance;

        public ToTLootProperties(Predicate<Identifier> p, int min, int max, float chance) {
            this.predicate = p;
            this.min = min;
            this.max = max;
            this.chance = chance;
        }
    }
}
