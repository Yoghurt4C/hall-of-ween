package mods.hallofween.util;

import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
}
