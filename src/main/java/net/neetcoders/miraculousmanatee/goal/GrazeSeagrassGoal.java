package net.neetcoders.miraculousmanatee.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.ItemStack;
import net.neetcoders.miraculousmanatee.registry.ModItems;
import net.neetcoders.miraculousmanatee.entity.Manatee;

import net.minecraft.world.entity.ai.goal.Goal;
import java.util.EnumSet;

public class GrazeSeagrassGoal extends Goal {
    private final Manatee manatee;
    private final double speed;
    private BlockPos targetPos = null;
    private int searchCooldown = 0;

    private int stuckTimer = 0;

    private static final int MAX_STUCK_TIME = 400;

    public GrazeSeagrassGoal(Manatee manatee, double speed) {
        this.manatee = manatee;
        this.speed = speed;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {

        System.out.println("canUse called, target: " + targetPos + ", cooldown: " + manatee.grazeCooldown);
        if (manatee.grazeCooldown > 0)
            return false;
        if (isInventoryFull())
            return false;
        if (searchCooldown > 0) {
            searchCooldown--;
            return false;
        }
        targetPos = findNearbySeagrass();
        return targetPos != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (targetPos == null)
            return false;
        if (manatee.grazeCooldown > 0)
            return false;
        stuckTimer++;
        if (stuckTimer > MAX_STUCK_TIME)
            return false; // give up
        // stop if seagrass was removed by something else
        var block = manatee.level().getBlockState(targetPos).getBlock();
        return block == Blocks.SEAGRASS || block == Blocks.TALL_SEAGRASS;
    }

    @Override
    public void start() {

        System.out.println("Manatee heading to seagrass at " + targetPos);

        stuckTimer = 0; // reset on each new attempt
        manatee.getNavigation().moveTo(
                targetPos.getX() + 0.5,
                targetPos.getY() + 0.5,
                targetPos.getZ() + 0.5,
                speed);
    }

    @Override
    public void stop() {

        stuckTimer = 0;
        targetPos = null;
        manatee.getNavigation().stop();
        searchCooldown = 40;
    }

    @Override
    public void tick() {
        if (targetPos == null)
            return;

        manatee.getLookControl().setLookAt(
                targetPos.getX(),
                targetPos.getY(),
                targetPos.getZ());

        // check if close enough to eat
        double dist = manatee.blockPosition().distSqr(targetPos);
        if (dist < 4.0) {
            var block = manatee.level().getBlockState(targetPos).getBlock();
            if (block == Blocks.SEAGRASS || block == Blocks.TALL_SEAGRASS) {
                manatee.level().setBlockAndUpdate(targetPos, Blocks.WATER.defaultBlockState());
                manatee.level().playSound(null, targetPos, SoundEvents.GRASS_BREAK,
                        SoundSource.NEUTRAL, 1.0f, 1.0f);
                manatee.getInventory().addItem(new ItemStack(ModItems.BLUBBER.get()));
                manatee.grazeCooldown = 200;
                targetPos = null;
            }
        }
    }

    private BlockPos findNearbySeagrass() {
        BlockPos origin = manatee.blockPosition();
        int range = 16;
        for (BlockPos pos : BlockPos.betweenClosed(
                origin.offset(-range, -8, -range),
                origin.offset(range, 8, range))) {
            var block = manatee.level().getBlockState(pos).getBlock();
            if (block == Blocks.SEAGRASS || block == Blocks.TALL_SEAGRASS) {
                var belowBlock = manatee.level().getBlockState(pos.below()).getBlock();
                boolean isTrulyBottom = belowBlock != Blocks.SEAGRASS && belowBlock != Blocks.TALL_SEAGRASS
                        && manatee.level().getBlockState(pos.below()).isSolid();
                if (!isTrulyBottom) {
                    return pos.immutable();
                }
            }
        }
        return null;
    }

    private boolean isInventoryFull() {
        var inv = manatee.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (inv.getItem(i).isEmpty())
                return false;
        }
        return true;
    }
}
