package net.neetcoders.miraculousmanatee.entity.goal;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import net.neetcoders.miraculousmanatee.config.ModServerConfig;
import net.neetcoders.miraculousmanatee.entity.Manatee;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.Nullable;

/**
 * Swim to a nearby water plant, chew on it for a moment, then eat it.
 * <p>
 * Lifecycle (driven by the vanilla GoalSelector):
 * <ol>
 * <li>{@link #canUse()} runs while idle: waits out the cooldown, then scans a box around the manatee for food
 * and picks the best candidate <em>that the pathfinder can actually reach</em> (see {@link #findFood()}).</li>
 * <li>{@link #tick()} in the TRAVEL phase lets the navigation follow that path. If the navigation finishes
 * without the plant in reach (per-node timeout, or the world changed) it asks for one fresh path; if that one
 * cannot reach either, the plant is remembered as unreachable and the goal ends. Once the plant is inside the
 * manatee's reach it switches to EATING.</li>
 * <li>The EATING phase stops movement, flags the entity so the client plays the eat animation, and after
 * {@value #EAT_DURATION_TICKS} ticks removes the plant and feeds the manatee via {@link Manatee#onAte()}.</li>
 * </ol>
 * Food priority follows the issue discussion: kelp heads (the top {@code KELP} block) ordered by highest Y,
 * then kelp stalks, then lily pads. A lily pad sits on top of the water, so the manatee navigates to the water
 * block beneath it and eats from below.
 * <p>
 * Why the reachability check matters: the swim node evaluator paths the manatee as a solid
 * {@code floor(width+1) x floor(height+1)} block of water (3x2 at scale 1.5). Kelp hugging a river bank often has
 * no such column beside it, and A* then returns a <em>partial</em> path ending at the nearest reachable node.
 * Following partial paths forever is what made manatees circle "with purpose" without ever eating.
 */
public class ManateeGrazeGoal extends Goal {
    private static final int SEARCH_RADIUS = 8;
    private static final int VERTICAL_SEARCH_RADIUS = 4;
    /**
     * A* stops as soon as it closes a node within this Manhattan distance of the target. 1 lets the manatee stop
     * in the water block beside/below a plant, which is required near the surface where the top water layer is
     * never a valid node (the evaluator needs water through the mob's whole height).
     */
    private static final int PATH_ACCURACY = 1;
    /** How many of the best candidates to test with a real path search before giving up this round. */
    private static final int MAX_PATH_ATTEMPTS = 4;
    /** Ticks before the "unreachable plants" memory is cleared (kelp grows, players dig, etc.). */
    private static final int UNREACHABLE_MEMORY_TICKS = 1200;
    /** Abandon a plant we could not reach in this many ticks. */
    private static final int GIVE_UP_TICKS = 400;
    /** Fresh path requests allowed after the navigation finished without the plant in reach. */
    private static final int MAX_REPATHS = 2;
    /** Chewing time before the plant disappears (same as a sheep eating grass). */
    private static final int EAT_DURATION_TICKS = 40;
    /** The target counts as reached when its centre is within this many blocks of the manatee's hitbox. */
    private static final double REACH_DISTANCE = 1.25D;

    private enum FoodType {
        KELP_HEAD, KELP_STALK, LILY_PAD
    }

    private record Candidate(BlockPos pos, FoodType type, double distanceSqr) {
        BlockPos swimPos() {
            return type == FoodType.LILY_PAD ? pos.below() : pos;
        }
    }

    /** Best food first: kelp heads over stalks over lily pads, then highest, then nearest. */
    private static final Comparator<Candidate> FOOD_ORDER = Comparator
            .comparing(Candidate::type)
            .thenComparing(Comparator.comparingInt((Candidate c) -> c.pos().getY()).reversed())
            .thenComparingDouble(Candidate::distanceSqr);

    private final Manatee manatee;
    private final double speedModifier;
    private final Set<BlockPos> unreachable = new HashSet<>();

    @Nullable
    private BlockPos foodPos;
    /** Water block the manatee actually navigates to; equals {@link #foodPos} for kelp, the block below for lily pads. */
    @Nullable
    private BlockPos swimPos;
    @Nullable
    private Path path;
    private int cooldownTicks;
    private int unreachableMemoryTicks;
    private int travelTicks;
    private int repaths;
    private int eatTicks;
    private boolean eating;

    public ManateeGrazeGoal(Manatee manatee, double speedModifier) {
        this.manatee = manatee;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!unreachable.isEmpty() && --unreachableMemoryTicks <= 0) {
            unreachable.clear();
        }

        if (cooldownTicks > 0) {
            cooldownTicks--;
            return false;
        }

        if (manatee.isInSittingPose() || !manatee.isInWaterOrBubble() || manatee.isFull()) {
            return false;
        }

        return findFood();
    }

    @Override
    public boolean canContinueToUse() {
        if (foodPos == null || manatee.isInSittingPose()) {
            return false;
        }
        if (eating) {
            return eatTicks > 0;
        }
        return travelTicks < GIVE_UP_TICKS
                && !manatee.isFull()
                && classify(manatee.level(), foodPos) != null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        travelTicks = 0;
        repaths = 0;
        eatTicks = 0;
        eating = false;
        manatee.getNavigation().moveTo(path, speedModifier);
    }

    @Override
    public void stop() {
        if (!eating && foodPos != null) {
            // Travel phase ended without a meal (gave up, plant vanished, sat down): don't retry it immediately.
            rememberUnreachable(foodPos);
        }
        foodPos = null;
        swimPos = null;
        path = null;
        eating = false;
        manatee.setEating(false);
        manatee.getNavigation().stop();
        cooldownTicks = ModServerConfig.MANATEE_GRAZE_COOLDOWN_TICKS.get();
    }

    @Override
    public void tick() {
        if (foodPos == null) {
            return;
        }

        Vec3 foodCenter = Vec3.atCenterOf(foodPos);
        manatee.getLookControl().setLookAt(foodCenter.x, foodCenter.y, foodCenter.z);

        if (eating) {
            tickEating();
        } else {
            tickTravel();
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Travel phase
    // ---------------------------------------------------------------------------------------------

    private void tickTravel() {
        travelTicks++;

        // Reach is measured against the water block we swim to (for a lily pad that is the block under it), so
        // the manatee counts as arrived while sitting just below the surface rather than needing to breach.
        if (swimPos != null && manatee.getBoundingBox().inflate(REACH_DISTANCE).contains(Vec3.atCenterOf(swimPos))) {
            beginEating();
            return;
        }

        PathNavigation navigation = manatee.getNavigation();
        if (navigation.isDone()) {
            // The path ran out (per-node timeout, stuck detection, or the world changed) with the plant still out
            // of reach. Try a fresh search a couple of times; if it still cannot reach, give this plant up.
            if (repaths++ >= MAX_REPATHS || !requestPath()) {
                rememberUnreachable(foodPos);
                foodPos = null;
            }
        }
    }

    /**
     * Computes a new path to {@link #swimPos} and follows it only if A* actually reached the target
     * ({@link Path#canReach()}); a partial path to "the closest point I could get to" is rejected.
     */
    private boolean requestPath() {
        if (swimPos == null) {
            return false;
        }
        PathNavigation navigation = manatee.getNavigation();
        Path fresh = navigation.createPath(swimPos, PATH_ACCURACY);
        if (fresh == null || !fresh.canReach()) {
            return false;
        }
        path = fresh;
        return navigation.moveTo(fresh, speedModifier);
    }

    private void rememberUnreachable(@Nullable BlockPos pos) {
        if (pos == null) {
            return;
        }
        unreachable.add(pos.immutable());
        unreachableMemoryTicks = UNREACHABLE_MEMORY_TICKS;
    }

    // ---------------------------------------------------------------------------------------------
    // Eating phase
    // ---------------------------------------------------------------------------------------------

    private void beginEating() {
        eating = true;
        eatTicks = adjustedTickDelay(EAT_DURATION_TICKS);
        manatee.setEating(true);
        manatee.getNavigation().stop();
    }

    private void tickEating() {
        eatTicks--;
        if (eatTicks > 0) {
            return;
        }

        Level level = manatee.level();
        if (foodPos != null && classify(level, foodPos) != null) {
            // Respect the mobGriefing rule for the block itself (like sheep), but the manatee still gets its meal.
            if (EventHooks.canEntityGrief(level, manatee)) {
                // destroyBlock also fires the block-break particle/sound level event.
                level.destroyBlock(foodPos, false, manatee);
            }
            manatee.onAte();
        }
        // eatTicks is now 0, so canContinueToUse() ends the goal on the next selector pass.
    }

    // ---------------------------------------------------------------------------------------------
    // Food search
    // ---------------------------------------------------------------------------------------------

    /**
     * Scans the search box, sorts candidates by {@link #FOOD_ORDER}, then runs a real path search on the best
     * few until one is reachable. Candidates that fail are remembered so the next round skips them.
     */
    private boolean findFood() {
        Level level = manatee.level();
        BlockPos origin = manatee.blockPosition();
        List<Candidate> candidates = new ArrayList<>();

        for (BlockPos pos : BlockPos.betweenClosed(
                origin.offset(-SEARCH_RADIUS, -VERTICAL_SEARCH_RADIUS, -SEARCH_RADIUS),
                origin.offset(SEARCH_RADIUS, VERTICAL_SEARCH_RADIUS, SEARCH_RADIUS))) {
            if (unreachable.contains(pos)) {
                continue;
            }
            FoodType type = classify(level, pos);
            if (type != null) {
                candidates.add(new Candidate(pos.immutable(), type, manatee.distanceToSqr(Vec3.atCenterOf(pos))));
            }
        }

        if (candidates.isEmpty()) {
            return false;
        }
        candidates.sort(FOOD_ORDER);

        PathNavigation navigation = manatee.getNavigation();
        int attempts = Math.min(MAX_PATH_ATTEMPTS, candidates.size());
        for (int i = 0; i < attempts; i++) {
            Candidate candidate = candidates.get(i);
            Path candidatePath = navigation.createPath(candidate.swimPos(), PATH_ACCURACY);
            if (candidatePath != null && candidatePath.canReach()) {
                foodPos = candidate.pos();
                swimPos = candidate.swimPos();
                path = candidatePath;
                return true;
            }
            rememberUnreachable(candidate.pos());
        }
        return false;
    }

    /** Returns what kind of food is at {@code pos}, or null if it is not edible (or not reachable from water). */
    @Nullable
    private static FoodType classify(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.is(Blocks.KELP)) {
            return FoodType.KELP_HEAD;
        }
        if (state.is(Blocks.KELP_PLANT)) {
            return FoodType.KELP_STALK;
        }
        if (state.is(Blocks.LILY_PAD)
                && ModServerConfig.MANATEE_EAT_LILY_PADS.get()
                && level.getFluidState(pos.below()).is(FluidTags.WATER)) {
            return FoodType.LILY_PAD;
        }
        return null;
    }
}
