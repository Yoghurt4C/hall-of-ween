package mods.hallofween.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import me.shedaniel.cloth.api.utils.v1.GameInstanceUtils;
import mods.hallofween.mixin.MinecraftServerAccessor;
import mods.hallofween.mixin.WorldSavePathAccessor;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public interface HallOfWeenUtil {
    Identifier DEFAULTID = new Identifier("hallofween", "hallofween");
    Logger L = LogManager.getLogger("Hall of Ween");

    static String getModId() {
        return DEFAULTID.getNamespace();
    }

    static Identifier getId(String id) {
        return new Identifier(getModId(), id);
    }

    static Item getItem(String id) {
        return Registry.ITEM.get(getId(id));
    }

    static Path getDataPath(MinecraftServer server) {
        return ((MinecraftServerAccessor) GameInstanceUtils.getServer())
                .getSession().getDirectory(WorldSavePathAccessor.constructor("gw2"));
    }

    static List<String> toList(JsonArray arr) {
        List<String> list = new ArrayList<>();
        for (JsonElement e : arr) {
            list.add(e.getAsString());
        }
        return list;
    }
}
