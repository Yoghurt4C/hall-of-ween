package mods.hallofween.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.CakeBlock;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class SpookyCakeBlock extends CakeBlock {
    private final StatusEffect pot;
    private final int hunger;
    private final float sat;

    public SpookyCakeBlock(Settings settings, int hunger, float sat, StatusEffect effect) {
        super(settings);
        this.hunger = hunger;
        this.sat = sat;
        this.pot = effect;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack stack = player.getStackInHand(hand);
        if (!world.isClient() && Registry.ITEM.getId(stack.getItem()).getPath().equals("candy_corn_cake_slice") && state.get(BITES) > 0) {
            world.setBlockState(pos, state.with(BITES, state.get(BITES) - 1), 3);
            stack.decrement(1);
            return ActionResult.SUCCESS;
        }
        return this.tryEat(world, pos, state, player);
    }

    private ActionResult tryEat(WorldAccess world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!player.canConsume(true)) {
            return ActionResult.PASS;
        } else {
            player.incrementStat(Stats.EAT_CAKE_SLICE);
            player.getHungerManager().add(this.hunger, this.sat);
            player.addStatusEffect(new StatusEffectInstance(this.pot, 200));
            int i = state.get(BITES);
            if (i < 6) {
                world.setBlockState(pos, state.with(BITES, i + 1), 3);
            } else {
                world.removeBlock(pos, false);
            }

            return ActionResult.SUCCESS;
        }
    }

}
