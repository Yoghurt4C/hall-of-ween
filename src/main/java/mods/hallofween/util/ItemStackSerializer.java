package mods.hallofween.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public interface ItemStackSerializer {
    static JsonElement toJson(ItemStack stack) {
        JsonObject obj = new JsonObject();
        if (stack.isEmpty()) {
            return new JsonPrimitive("empty");
        } else {
            obj.addProperty("item", Registry.ITEM.getId(stack.getItem()).toString());
            if (stack.getCount() > 1) obj.addProperty("count", stack.getCount());
            if (stack.hasTag()) obj.addProperty("nbt", stack.getTag().asString());
        }
        return obj;
    }

    static ItemStack fromJson(JsonElement je) {
        ItemStack stack = ItemStack.EMPTY;
        try {
            if (je.isJsonObject()) {
                JsonObject obj = je.getAsJsonObject();
                Item item = Registry.ITEM.get(new Identifier(obj.get("item").getAsString()));
                int count = obj.has("count") ? obj.get("count").getAsInt() : 1;
                stack = new ItemStack(item, count);
                if (obj.has("nbt")) {
                    CompoundTag tag = StringNbtReader.parse(obj.get("nbt").getAsString());
                    stack.setTag(tag);
                }
            }
        } catch (UnsupportedOperationException e) {
            HallOfWeenUtil.L.warn("[Hall of Ween] Couldn't read ItemStack from {}.", je);
            HallOfWeenUtil.L.error(e.getMessage());
        } catch (CommandSyntaxException e) {
            HallOfWeenUtil.L.warn("[Hall of Ween] Couldn't read ItemStack NBT from {}", je);
            HallOfWeenUtil.L.error(e.getMessage());
        }
        return stack;
    }
}
