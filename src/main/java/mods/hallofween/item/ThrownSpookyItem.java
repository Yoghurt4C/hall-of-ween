package mods.hallofween.item;

import mods.hallofween.HallOfWeen;
import mods.hallofween.entity.SpookyThrowable;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class ThrownSpookyItem extends Item {
    private final SpookyThrowableFactory fc;

    public ThrownSpookyItem(SpookyThrowableFactory factory) {
        super(new FabricItemSettings().group(HallOfWeen.ITEMGROUP));
        fc = factory;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_EGG_THROW, SoundCategory.NEUTRAL, 0.5F, 0.3F / (RANDOM.nextFloat() * 0.4F + 0.8F));
        if (!world.isClient) {
            SpookyThrowable egge = fc.create(world, user);
            egge.setItem(itemStack);
            egge.setProperties(user, user.pitch, user.yaw, 0.0F, 1.5F, 1.0F);
            world.spawnEntity(egge);
        }

        user.incrementStat(Stats.USED.getOrCreateStat(this));
        if (!user.abilities.creativeMode) {
            itemStack.decrement(1);
        }

        return TypedActionResult.success(itemStack, world.isClient());
    }

    public interface SpookyThrowableFactory {
        SpookyThrowable create(World world, LivingEntity user);
    }
}
