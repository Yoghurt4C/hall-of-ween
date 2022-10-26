package mods.hallofween.mixin.containers;

import mods.hallofween.registry.ContainerRegistry;
import mods.hallofween.registry.ContainerRegistry.ContainerLootProperties;
import net.fabricmc.fabric.api.loot.v1.FabricLootPoolBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.loot.ConstantLootTableRange;
import net.minecraft.loot.UniformLootTableRange;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.function.SetNbtLootFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Map;
import java.util.function.Predicate;

import static mods.hallofween.HallOfWeen.getItem;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "dropLoot", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    public void appendToTBags(DamageSource source, boolean byPlayer, CallbackInfo ctx, Identifier loot) {
        if (world.isClient()) return;
        for (Map.Entry<String, ContainerLootProperties> e : ContainerRegistry.LOOT_PREDICATES.entrySet()) {
            Predicate<Identifier> p = e.getValue().predicate;
            if (p.test(loot)) {
                String name = e.getKey();
                ContainerRegistry.ContainerProperties props = ContainerRegistry.CONTAINERS.get(name);
                ContainerLootProperties lootProps = e.getValue();
                CompoundTag tag = new CompoundTag();
                tag.putString("totId", name);
                tag.putInt("bagColor", props.bagColor);
                tag.putInt("magicColor", props.overlayColor);
                FabricLootPoolBuilder b = FabricLootPoolBuilder.builder()
                        .rolls(ConstantLootTableRange.create(1))
                        .with(ItemEntry.builder(getItem("trick_or_treat_bag")))
                        .withFunction(SetNbtLootFunction.builder(tag).build())
                        .withFunction(SetCountLootFunction.builder(UniformLootTableRange.between(lootProps.min, lootProps.max)).build())
                        .withCondition(RandomChanceLootCondition.builder(lootProps.chance).build());
                b.build().addGeneratedLoot(this::dropStack, new LootContext.Builder((ServerWorld) this.world)
                        .parameter(LootContextParameters.THIS_ENTITY, this)
                        .parameter(LootContextParameters.ORIGIN, this.getPos())
                        .parameter(LootContextParameters.DAMAGE_SOURCE, source)
                        .random(world.random)
                        .build(LootContextTypes.ENTITY));
                break;
            }
        }
    }
}
