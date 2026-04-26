package net.neetcoders.miraculousmanatee.entity.goal;

import java.util.EnumSet;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neetcoders.miraculousmanatee.entity.Manatee;

public class ManateeGrazeKelpGoal extends Goal {
    private static final int SEARCH_RADIUS = 8;
    private static final int VERTICAL_SEARCH_RADIUS = 4;
    private static final int GRAZE_COOLDOWN_TICKS = 120;
    private static final double GRAZE_DISTANCE_SQUARED = 4.0D;

    private final Manatee manatee;
    private final double speedModifier;

    private BlockPos targetPos;
    private int cooldownTicks;
    private int grazingTicks;

    public ManateeGrazeKelpGoal(Manatee manatee, double speedModifier) {
        this.manatee = manatee;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (cooldownTicks > 0) {
            cooldownTicks--;
            return false;
        }

        if (manatee.isInSittingPose() || !manatee.isInWaterOrBubble() || !manatee.canStoreBlubber()) {
            return false;
        }

        targetPos = findKelpToGraze();
        return targetPos != null;
    }

    @Override
    public boolean canContinueToUse() {
        return targetPos != null
                && !manatee.isInSittingPose()
                && manatee.canStoreBlubber()
                && grazingTicks < 200
                && isEdibleKelp(manatee.level(), targetPos);
    }

    @Override
    public void start() {
        grazingTicks = 0;
        moveToTarget();
    }

    @Override
    public void stop() {
        targetPos = null;
        cooldownTicks = GRAZE_COOLDOWN_TICKS;
        manatee.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (targetPos == null) {
            return;
        }

        grazingTicks++;
        manatee.getLookControl().setLookAt(targetPos.getX() + 0.5D, targetPos.getY() + 0.5D, targetPos.getZ() + 0.5D);

        if (manatee.distanceToSqr(targetPos.getCenter()) <= GRAZE_DISTANCE_SQUARED) {
            grazeTarget();
            return;
        }

        moveToTarget();
    }

    private void moveToTarget() {
        manatee.getNavigation().moveTo(targetPos.getX() + 0.5D, targetPos.getY(), targetPos.getZ() + 0.5D,
                speedModifier);
    }

    private void grazeTarget() {
        Level level = manatee.level();
        if (isEdibleKelp(level, targetPos) && manatee.addBlubberToBelly()) {
            level.destroyBlock(targetPos, false, manatee);
        }

        targetPos = null;
        cooldownTicks = GRAZE_COOLDOWN_TICKS;
    }

    private BlockPos findKelpToGraze() {
        BlockPos origin = manatee.blockPosition();
        BlockPos closest = null;
        double closestDistance = Double.MAX_VALUE;

        for (BlockPos pos : BlockPos.betweenClosed(
                origin.offset(-SEARCH_RADIUS, -VERTICAL_SEARCH_RADIUS, -SEARCH_RADIUS),
                origin.offset(SEARCH_RADIUS, VERTICAL_SEARCH_RADIUS, SEARCH_RADIUS))) {
            if (!isEdibleKelp(manatee.level(), pos)) {
                continue;
            }

            double distance = manatee.distanceToSqr(pos.getCenter());
            if (distance < closestDistance) {
                closest = pos.immutable();
                closestDistance = distance;
            }
        }

        return closest;
    }

    private static boolean isEdibleKelp(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        BlockState belowState = level.getBlockState(pos.below());
        return isKelp(state) && isKelp(belowState);
    }

    private static boolean isKelp(BlockState state) {
        return state.is(Blocks.KELP) || state.is(Blocks.KELP_PLANT);
    }
}
