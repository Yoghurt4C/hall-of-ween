package mods.hallofween;

import com.google.common.collect.ImmutableSet;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static mods.hallofween.util.HallOfWeenUtil.L;
import static mods.hallofween.util.HallOfWeenUtil.getModId;

public class Config {
    private static boolean isInitialized = false;

    public static int maxBagInventorySize, startingBagInventorySize;
    public static float testificateChance;
    public static boolean
            annoyingTestificates, injectTestificatesIntoLootTables,
            disableDefaultLootContainers, injectLootContainers,
            enableDiscoveryRecipes, faithfulRecipeSheets, recipeSheetXP,
            enableBagInventory,
            enableOfficialTitles, enableCustomTitles,
            generateDataWarning,
            enableREICompat;

    public static String gw2ApiKey;

    public static void tryInit() {
        if (!isInitialized) init();
    }

    private static void init() {
        String filename = "hallofween.properties";
        ImmutableSet<? extends Entry<? extends Serializable>> entries = ImmutableSet.of(
                Entry.of("testificateChance", 0.15f,
                        "How likely it is for a Captive Testificate to appear in a structure chest.\n" +
                                "You may want to reduce this if you have a lot of structure mods. [Side: SERVER | Default: 0.15f]"),
                Entry.of("annoyingTestificates", true,
                        "Captive Testificates periodically annoy you. [Side: CLIENT | Default: true]"),
                Entry.of("injectTestificatesIntoLootTables", true,
                        "Automatically adds Captive Testificates into every non-village structure chest.\n" +
                                "Disable this if you want precise control using something like KubeJS. [Side: SERVER | Default: true]"),
                Entry.of("disableDefaultLootContainers", false,
                        "Disables the default Loot Containers.\n" +
                                "Their Loot Tables are still loaded by the game, so you'll have to clean that up yourself. [Side: SERVER | Default: false]"),
                Entry.of("injectLootContainers", true,
                        "If false, the mod ceases attempts to modify loot tables based on predicates in Loot Container JSONs. [Side: SERVER | Default: true]"),
                Entry.of("enableDiscoveryRecipes", true,
                        "Setting this to false disables the functionality of testing for advancements in certain recipes. [Side: SERVER | Default: true]"),
                Entry.of("faithfulRecipeSheets", true,
                        "Makes items drawn on Recipe Sheets pitch black. Setting to false gives them back their colours. [Side: CLIENT | Default: true]"),
                Entry.of("recipeSheetXP", true,
                        "Recipe Sheets grant experience when consumed, giving them a use when they're random drops. [Side: SERVER | Default: true]"),
                Entry.of("enableBagInventory", true,
                        "Controls loading of the entire Bag Inventory system. Disabling this mid-playthrough may cause items inside the inventory to vanish forever. [Side: BOTH | Default: true]"),
                Entry.of("maxBagInventorySize", 12,
                        "The maximum amount of Bag slots a player can unlock. This used to be 10 before 23/11/2022. [Side: SERVER | Default: 12]"),
                Entry.of("startingBagInventorySize", 3,
                        "The amount of Bag slots a player starts with. If the player authenticates a valid GW2 API key for an account that bought the game, it won't be lower than 5. [Side: SERVER | Default: 3]"),
                Entry.of("generateDataWarning", true,
                        "Creates a file called \"warning.txt\" inside the world/gw2/ folder. [Side: SERVER | Default: true]"),
                Entry.of("enableOfficialTitles", true,
                        "Retrieves title data from the GW2 API. You don't need an API key for this. [Side: SERVER | Default: true]"),
                Entry.of("enableCustomTitles", true,
                        "Lets you define custom titles. [Side: SERVER | Default: true]"),
                Entry.of("gw2ApiKey", "",
                        "You can paste a Guild Wars 2 API key in here to receive various benefits in this mod. Check the Github Wiki for required permissions.\n" +
                                "To validate the key in-game, run the /gw2_auth command. Although other people won't be able to do much with your API key, be careful when sharing your modpack or config folder. [Side: CLIENT]"),
                Entry.of("enableREICompat", true,
                        "Adds various bits and bobs to RoughlyEnoughItems to enhance your Recipe viewing experience. [Side: CLIENT | Default: true]")
        );
        if (Files.notExists(getConfigDir()) && !getConfigDir().toFile().mkdir()) {
            L.error("[" + getModId() + "] Can't reach the config directory. This is probably really bad.");
        }
        Path configPath = getConfigDir().resolve(filename);
        Map<String, String> cfg = new HashMap<>();
        try {
            boolean changed = false;
            File configurationFile = configPath.toFile();
            StringBuilder content = new StringBuilder().append("#Audino Configuration.\n");
            content.append("#Last generated at: ").append(new Date()).append("\n\n");
            if (Files.notExists(configPath) && !configurationFile.createNewFile())
                L.error("[" + getModId() + "] Can't create config file \"" + configurationFile + "\". This is probably bad.");
            BufferedReader r = Files.newBufferedReader(configPath, StandardCharsets.UTF_8);

            String line;
            while ((line = r.readLine()) != null) {
                if (line.startsWith("#") || line.isEmpty()) continue;
                String[] kv = line.split("=");
                if (kv.length == 2) cfg.put(kv[0], kv[1]);
            }
            r.close();

            for (Entry<?> entry : entries) {
                String key = entry.key;
                Object value = entry.value;
                Class<?> cls = entry.cls;
                if (cfg.containsKey(key)) {
                    String s = cfg.get(key);
                    if (s.equals("")) {
                        logEntryError(configurationFile, key, value, "nothing", "present");
                    } else if (cls.equals(Integer.class)) {
                        try {
                            setCfgValue(key, Integer.parseInt(s));
                        } catch (NumberFormatException e) {
                            logEntryError(configurationFile, key, value, s, "an integer");
                        }
                    } else if (cls.equals(Float.class)) {
                        try {
                            setCfgValue(key, Float.parseFloat(s));
                        } catch (NumberFormatException e) {
                            logEntryError(configurationFile, key, value, s, "a float");
                        }
                    } else if (cls.equals(Boolean.class)) {
                        if (!"true".equalsIgnoreCase(s) && !"false".equalsIgnoreCase(s)) {
                            logEntryError(configurationFile, key, value, s, "a boolean");
                        } else setCfgValue(key, Boolean.parseBoolean(s));
                    } else if (cls.equals(String.class)) {
                        setCfgValue(key, s);
                    }
                } else {
                    changed = true;
                    cfg.put(key, value.toString());
                    setCfgValue(key, value);
                }
                content.append("#").append(key).append(": ").append(entry.comment.replace("\n", "\n#")).append("\n");
                content.append(key).append("=").append(cfg.get(key)).append("\n");
            }
            if (changed) {
                Files.write(configPath, Collections.singleton(content.toString()), StandardCharsets.UTF_8);
            }
            isInitialized = true;
        } catch (IOException e) {
            L.fatal("[" + getModId() + "] Could not read/write config!");
            L.fatal(e);
        }
    }

    private static void logEntryError(File configurationFile, String key, Object value, String found, String expected) {
        L.error("[" + getModId() + "] Error processing configuration file \"" + configurationFile + "\".");
        L.error("[" + getModId() + "] Expected configuration value for " + key + " to be " + expected + ", found \"" + found + "\". Using default value \"" + value + "\" instead.");
        setCfgValue(key, value);
    }

    private static void setCfgValue(String k, Object v) {
        try {
            Config.class.getDeclaredField(k).set(Config.class, v);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            L.error("[" + getModId() + "] Could not set the runtime config state!");
            L.error(e);
        }
    }

    private static Path getConfigDir() {
        return Paths.get(".").resolve("config");
    }

    private static class Entry<T> {
        private final String key;
        private final T value;
        private final String comment;
        private final Class<T> cls;

        private Entry(String key, T value, String comment, Class<T> cls) {
            this.key = key;
            this.value = value;
            this.comment = comment;
            this.cls = cls;
        }

        public static Entry<Integer> of(String key, int value, String comment) {
            return new Entry<>(key, value, comment, Integer.class);
        }

        public static Entry<Float> of(String key, float value, String comment) {
            return new Entry<>(key, value, comment, Float.class);
        }

        public static Entry<Boolean> of(String key, boolean value, String comment) {
            return new Entry<>(key, value, comment, Boolean.class);
        }

        public static Entry<String> of(String key, String value, String comment) {
            return new Entry<>(key, value, comment, String.class);
        }
    }
}
