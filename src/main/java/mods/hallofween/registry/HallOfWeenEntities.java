package mods.hallofween.registry;

import mods.hallofween.entity.ThrownRottenEgg;
import mods.hallofween.entity.ThrownToiletPaper;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.registry.Registry;

import static mods.hallofween.util.HallOfWeenUtil.getId;

public class HallOfWeenEntities {
    public static EntityType<ThrownRottenEgg> ROTTEN_EGG = register("rotten_egg", ThrownRottenEgg::new);
    public static EntityType<ThrownToiletPaper> TOILET_PAPER = register("toilet_paper", ThrownToiletPaper::new);

    private static <T extends Entity> EntityType<T> register(String id, EntityType.EntityFactory<T> factory) {
        return Registry.register(Registry.ENTITY_TYPE, getId(id),
                FabricEntityTypeBuilder.create(SpawnGroup.MISC, factory)
                        .dimensions(EntityDimensions.fixed(0.25f, 0.25f))
                        .trackRangeBlocks(4).trackedUpdateRate(10).build());
    }
}
