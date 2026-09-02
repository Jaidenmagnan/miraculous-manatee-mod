package net.neetcoders.miraculousmanatee.worldgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BubbleColumnBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * A spring head: a round, bowl-shaped pool of clear water with a sandy floor, a mossy stone rim, and a soul
 * sand vent in the middle that sends up a column of bubbles.
 * <p>
 * Placement (see {@code placed_feature/spring_pool.json}) hands us a surface position. We refuse spots that
 * are already wet or not flat enough, then carve a parabolic bowl: the deepest point is at the centre and the
 * floor rises to meet the rim. Every carved cell is sealed underneath and the ring outside the bowl is walled
 * with stone below the rim, so caves cannot drain the pool. Bubble column blocks are placed explicitly because
 * chunks are still proto-chunks during feature placement and soul sand's normal "spawn a column" hook does
 * not run there.
 */
public class SpringPoolFeature extends Feature<NoneFeatureConfiguration> {
    private static final int MIN_RADIUS = 4;
    private static final int MAX_RADIUS = 6;
    private static final int MIN_DEPTH = 4;
    private static final int MAX_DEPTH = 6;
    /** Rim points may differ from the centre surface height by at most this much, or the spot is too steep. */
    private static final int MAX_RIM_HEIGHT_DIFFERENCE = 2;
    /** Blocks above the rim to clear of grass and other soft vegetation. */
    private static final int CLEAR_HEIGHT = 3;

    public SpringPoolFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();

        int radius = Mth.nextInt(random, MIN_RADIUS, MAX_RADIUS);
        int depth = Mth.nextInt(random, MIN_DEPTH, MAX_DEPTH);
        // The origin is the first air block above the surface; the rim sits on the surface block itself.
        int rimY = origin.getY() - 1;
        if (rimY - depth - 2 <= level.getMinBuildHeight()) {
            return false;
        }

        BlockState ground = level.getBlockState(origin.below());
        if (!ground.isSolid() || !ground.getFluidState().isEmpty()) {
            return false;
        }
        if (!isFlatEnough(level, origin, radius + 1, rimY)) {
            return false;
        }

        int radiusSqr = radius * radius;
        int outer = radius + 1;
        int outerSqr = outer * outer;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int dx = -outer; dx <= outer; dx++) {
            for (int dz = -outer; dz <= outer; dz++) {
                int distSqr = dx * dx + dz * dz;
                if (distSqr > outerSqr) {
                    continue;
                }
                int x = origin.getX() + dx;
                int z = origin.getZ() + dz;

                if (distSqr <= radiusSqr) {
                    carveBowlColumn(level, random, pos, x, z, rimY, depth, distSqr, radiusSqr);
                } else {
                    buildRimColumn(level, random, pos, x, z, rimY, depth);
                }
                clearAbove(level, pos, x, z, rimY);
            }
        }
        return true;
    }

    private static boolean isFlatEnough(WorldGenLevel level, BlockPos origin, int ringRadius, int rimY) {
        for (int i = 0; i < 8; i++) {
            double angle = i * Math.PI / 4.0;
            int x = origin.getX() + Mth.floor(Math.cos(angle) * ringRadius);
            int z = origin.getZ() + Mth.floor(Math.sin(angle) * ringRadius);
            int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
            if (Math.abs(surfaceY - rimY) > MAX_RIM_HEIGHT_DIFFERENCE) {
                return false;
            }
            if (!level.getFluidState(new BlockPos(x, surfaceY, z)).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static void carveBowlColumn(WorldGenLevel level, RandomSource random, BlockPos.MutableBlockPos pos,
            int x, int z, int rimY, int depth, int distSqr, int radiusSqr) {
        // Parabolic profile: full depth at the centre, one block at the edge.
        double fraction = distSqr / (double) radiusSqr;
        int localDepth = Math.max(1, Mth.ceil(depth * (1.0 - fraction)));
        int floorY = rimY - localDepth;
        boolean vent = distSqr <= 1;

        pos.set(x, floorY, z);
        level.setBlock(pos, vent ? Blocks.SOUL_SAND.defaultBlockState() : floorBlock(random), 2);

        // Seal beneath the floor so a cave underneath does not drain the pool.
        pos.set(x, floorY - 1, z);
        if (!level.getBlockState(pos).isSolid()) {
            level.setBlock(pos, Blocks.STONE.defaultBlockState(), 2);
        }

        BlockState water = Blocks.WATER.defaultBlockState();
        BlockState bubbles = Blocks.BUBBLE_COLUMN.defaultBlockState().setValue(BubbleColumnBlock.DRAG_DOWN, false);
        for (int y = floorY + 1; y <= rimY; y++) {
            pos.set(x, y, z);
            level.setBlock(pos, vent ? bubbles : water, 2);
        }
    }

    private static void buildRimColumn(WorldGenLevel level, RandomSource random, BlockPos.MutableBlockPos pos,
            int x, int z, int rimY, int depth) {
        pos.set(x, rimY, z);
        BlockState rim = random.nextFloat() < 0.6F ? Blocks.MOSSY_COBBLESTONE.defaultBlockState()
                : Blocks.MOSS_BLOCK.defaultBlockState();
        level.setBlock(pos, rim, 2);

        // Wall the outside of the bowl below the rim so water cannot leak sideways into caves or hollows.
        for (int y = rimY - depth - 1; y < rimY; y++) {
            pos.set(x, y, z);
            if (!level.getBlockState(pos).isSolid()) {
                level.setBlock(pos, Blocks.STONE.defaultBlockState(), 2);
            }
        }
    }

    private static BlockState floorBlock(RandomSource random) {
        float roll = random.nextFloat();
        if (roll < 0.15F) {
            return Blocks.GRAVEL.defaultBlockState();
        }
        if (roll < 0.25F) {
            return Blocks.CLAY.defaultBlockState();
        }
        return Blocks.SAND.defaultBlockState();
    }

    /** Removes grass, flowers and similar soft cover that would otherwise float above the water or rim. */
    private static void clearAbove(WorldGenLevel level, BlockPos.MutableBlockPos pos, int x, int z, int rimY) {
        for (int y = rimY + 1; y <= rimY + CLEAR_HEIGHT; y++) {
            pos.set(x, y, z);
            BlockState state = level.getBlockState(pos);
            if (!state.isAir() && (state.is(BlockTags.REPLACEABLE) || state.is(BlockTags.LEAVES))) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
            }
        }
    }
}
