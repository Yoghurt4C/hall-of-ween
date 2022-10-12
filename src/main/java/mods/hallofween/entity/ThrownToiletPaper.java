package mods.hallofween.entity;

import mods.hallofween.HallOfWeen;
import mods.hallofween.registry.HallOfWeenEntities;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

@SuppressWarnings("unchecked")
public class ThrownToiletPaper extends SpookyThrowable {
    public ThrownToiletPaper(EntityType type, World world) {
        super(type, world);
    }

    public ThrownToiletPaper(World world, LivingEntity owner) {
        super(HallOfWeenEntities.TOILET_PAPER, world, owner);
    }

    public ThrownToiletPaper(World world, double x, double y, double z) {
        super(HallOfWeenEntities.TOILET_PAPER, world, x, y, z);
    }

    @Override
    protected Item getDefaultItem() {
        return Registry.ITEM.get(HallOfWeen.getId("toilet_paper"));
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        if (entityHitResult.getEntity() instanceof LivingEntity) {
            LivingEntity victim = (LivingEntity) entityHitResult.getEntity();
            victim.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 180, 0));
            victim.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 160, 1));
            victim.addStatusEffect(new StatusEffectInstance(StatusEffects.UNLUCK, 200));
        }
    }
}
