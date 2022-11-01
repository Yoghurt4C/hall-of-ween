package mods.hallofween.registry;

import mods.hallofween.HallOfWeen;
import mods.hallofween.block.SpookyCakeBlock;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.MaterialColor;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.BlockItem;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import static mods.hallofween.util.HallOfWeenUtil.getId;

public class HallOfWeenBlocks {
    public static void init() {
        register("block_of_candy_corn", new Block(FabricBlockSettings.of(Material.STONE, MaterialColor.ORANGE).requiresTool().strength(1.25f, 5f).sounds(BlockSoundGroup.STONE)), 64);

        AbstractBlock.Settings cake = AbstractBlock.Settings.of(Material.CAKE).strength(0.5f).sounds(BlockSoundGroup.WOOL);
        register("candy_corn_cake", new SpookyCakeBlock(cake, 2, 0.12f, StatusEffects.LUCK), 16);
        register("soul_cake", new SpookyCakeBlock(cake, 3, 0.9f, StatusEffects.GLOWING), 16);
    }

    private static void register(String id, Block block, int stackSize) {
        Identifier Id = getId(id);
        Registry.register(Registry.ITEM, Id, new BlockItem(Registry.register(Registry.BLOCK, Id, block), new FabricItemSettings().group(HallOfWeen.ITEMGROUP).maxCount(stackSize)));
    }
}
