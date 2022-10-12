package mods.hallofween.item;

import mods.hallofween.Config;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TestificateItem extends Item {
    private short cd = 0;

    public TestificateItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(new TranslatableText("text.testificate.tooltip0").formatted(Formatting.RED));
        tooltip.add(new TranslatableText("text.testificate.tooltip1").formatted(Formatting.RED));
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext ctx) {
        World world = ctx.getWorld();
        BlockPos pos = ctx.getBlockPos();
        ItemStack stack = ctx.getStack();
        if (world.isClient() || ctx.getPlayer() == null || (world.getBlockState(pos.up()).shouldSuffocate(world, pos) && world.getBlockState(pos.up(2)).shouldSuffocate(world, pos)))
            return super.useOnBlock(ctx);
        else {
            int i = world.random.nextInt(10);
            VillagerEntity e = EntityType.VILLAGER.spawn((ServerWorld) world, null, null, null, pos, SpawnReason.SPAWN_EGG, true, false);
            if (Config.annoyingTestificates)
                ctx.getPlayer().sendMessage(new LiteralText("<").append(e.getName()).append("> ").append(new TranslatableText("text.testificate.freed" + i)), false);
            stack.decrement(1);
            return ActionResult.SUCCESS;
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);
        if (world.isClient() && Config.annoyingTestificates) {
            if (entity instanceof PlayerEntity && cd >= 800 && world.random.nextInt(250) == 0) {
                cd = 0;
                PlayerEntity player = (PlayerEntity) entity;
                int i = world.random.nextInt(9);
                player.sendMessage(new LiteralText("<Captive Testificate> ").append(new TranslatableText("text.testificate.plea" + i)), false);
            } else {
                cd++;
            }
        }
    }
}
