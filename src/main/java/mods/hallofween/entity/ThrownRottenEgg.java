package mods.hallofween.entity;

import mods.hallofween.registry.HallOfWeenEntities;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import static mods.hallofween.util.HallOfWeenUtil.getId;

@SuppressWarnings("unchecked")
public class ThrownRottenEgg extends SpookyThrowable {
    public ThrownRottenEgg(EntityType type, World world) {
        super(type, world);
    }

    public ThrownRottenEgg(World world, LivingEntity owner) {
        super(HallOfWeenEntities.ROTTEN_EGG, world, owner);
    }

    public ThrownRottenEgg(World world, double x, double y, double z) {
        super(HallOfWeenEntities.ROTTEN_EGG, world, x, y, z);
    }

    @Override
    protected Item getDefaultItem() {
        return Registry.ITEM.get(getId("rotten_egg"));
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        if (entityHitResult.getEntity() instanceof LivingEntity) {
            LivingEntity victim = (LivingEntity) entityHitResult.getEntity();
            victim.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 60, 1));
            victim.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 100, 0));
            victim.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 200, 0));
        }
    }
}
