package net.neetcoders.miraculousmanatee.block.portal;

import java.util.Comparator;
import java.util.Optional;

import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.DimensionTransition;
import net.neetcoders.miraculousmanatee.block.ManateePortalBlock;
import net.neetcoders.miraculousmanatee.registry.ModPoiTypes;
import net.neetcoders.miraculousmanatee.worldgen.ModDimensions;
import org.jetbrains.annotations.Nullable;

/**
 * Works out where a manatee portal sends an entity, mirroring vanilla {@code NetherPortalBlock}'s
 * search-or-create flow but using a {@link PoiManager} lookup (via {@link ModPoiTypes#MANATEE_PORTAL}) in
 * place of vanilla's {@code PortalForcer}.
 *
 * <p>The two dimensions this portal links are fixed: the overworld and {@link ModDimensions#MANATEE_DIMENSION}.
 * Stepping into a portal in either one sends the entity to the other. The destination position is worked out
 * in three stages:
 *
 * <ol>
 *     <li><b>Scale the coordinates.</b> The entity's X/Z are scaled by {@link DimensionType#getTeleportationScale}
 *     between the two dimensions' coordinate systems (1:1 here, since neither dimension is the nether - but the
 *     same formula vanilla uses) and clamped into the target dimension's world border to get an {@code exitPos}.</li>
 *     <li><b>Search.</b> The target's {@link PoiManager} is asked for the nearest registered
 *     {@code manatee_portal} POI within {@value #SEARCH_RADIUS} blocks of {@code exitPos}. If one exists, its
 *     frame is measured with {@link BlockUtil#getLargestRectangleAround} and the entity is sent to its
 *     interior - no new portal is built.</li>
 *     <li><b>Create.</b> If no portal is found, one is force-built at {@code exitPos}: the chunk is loaded, the
 *     terrain height is sampled to pick a Y that fits the whole frame within the build height limits, and
 *     {@link ManateePortalShape#build} carves a fresh frame with the same axis the entity entered on.</li>
 * </ol>
 *
 * Either way the returned {@link DimensionTransition} rotates the entity 90 degrees if the exit portal's axis
 * differs from the entry portal's axis, exactly like vanilla does for nether portals, so the entity always
 * steps out facing away from the frame.
 */
public final class ManateePortalTravel {
    /** Search radius, in blocks, used both for the POI lookup and (implicitly) the rectangle measurement. */
    private static final int SEARCH_RADIUS = 64;

    private ManateePortalTravel() {
    }

    /**
     * Computes the {@link DimensionTransition} for an entity stepping into the manatee portal block at
     * {@code pos} in {@code level}. Returns {@code null} only if the target dimension isn't loaded on this
     * server (e.g. the manatee dimension was disabled), matching the contract of
     * {@link net.minecraft.world.level.block.Portal#getPortalDestination}.
     */
    public static @Nullable DimensionTransition getDestination(ServerLevel level, Entity entity, BlockPos pos) {
        ResourceKey<Level> targetKey = level.dimension() == ModDimensions.MANATEE_DIMENSION
                ? Level.OVERWORLD
                : ModDimensions.MANATEE_DIMENSION;
        ServerLevel target = level.getServer().getLevel(targetKey);
        if (target == null) {
            return null;
        }

        double scale = DimensionType.getTeleportationScale(level.dimensionType(), target.dimensionType());
        WorldBorder worldBorder = target.getWorldBorder();
        BlockPos unclampedExitPos = BlockPos.containing(entity.getX() * scale, entity.getY(), entity.getZ() * scale);
        BlockPos exitPos = worldBorder.clampToBounds(unclampedExitPos);

        BlockState enteringState = level.getBlockState(pos);
        Direction.Axis enteringAxis = enteringState.hasProperty(ManateePortalBlock.AXIS)
                ? enteringState.getValue(ManateePortalBlock.AXIS)
                : Direction.Axis.X;

        BlockPos interiorMin;
        Direction.Axis exitAxis;
        DimensionTransition.PostDimensionTransition post;

        Optional<BlockPos> existingPortal = findExistingPortal(target, exitPos);
        if (existingPortal.isPresent()) {
            BlockPos found = existingPortal.get();
            BlockState state = target.getBlockState(found);
            exitAxis = state.getValue(ManateePortalBlock.AXIS);

            BlockUtil.FoundRectangle rect = BlockUtil.getLargestRectangleAround(found, exitAxis, 21, Direction.Axis.Y, 21,
                    candidate -> target.getBlockState(candidate) == state);
            interiorMin = rect.minCorner;
            post = DimensionTransition.PLAY_PORTAL_SOUND.then(transitioned -> transitioned.placePortalTicket(found));
        } else {
            BlockPos anchor = pickAnchor(target, exitPos);
            ManateePortalShape.build(target, anchor, enteringAxis, false);
            interiorMin = ManateePortalShape.interiorMin(anchor, enteringAxis);
            exitAxis = enteringAxis;
            post = DimensionTransition.PLAY_PORTAL_SOUND.then(DimensionTransition.PLACE_PORTAL_TICKET);
        }

        float yRotOffset = exitAxis == enteringAxis ? 0.0F : 90.0F;
        return new DimensionTransition(target, ManateePortalShape.entryPosition(interiorMin, exitAxis), entity.getDeltaMovement(),
                entity.getYRot() + yRotOffset, entity.getXRot(), post);
    }

    /**
     * Looks up the nearest existing manatee portal POI within {@value #SEARCH_RADIUS} blocks of
     * {@code exitPos}, breaking ties on {@link BlockPos}'s natural (Y, then Z, then X) order so the search is
     * deterministic when two portals are equidistant.
     */
    private static Optional<BlockPos> findExistingPortal(ServerLevel target, BlockPos exitPos) {
        PoiManager poiManager = target.getPoiManager();
        poiManager.ensureLoadedAndValid(target, exitPos, SEARCH_RADIUS);

        return poiManager.getInSquare(holder -> holder.is(ModPoiTypes.MANATEE_PORTAL.getKey()), exitPos, SEARCH_RADIUS, PoiManager.Occupancy.ANY)
                .map(PoiRecord::getPos)
                .filter(candidate -> target.getBlockState(candidate).hasProperty(ManateePortalBlock.AXIS))
                .min(Comparator.<BlockPos>comparingDouble(candidate -> candidate.distSqr(exitPos))
                        .thenComparing(Comparator.<BlockPos>naturalOrder()));
    }

    /** Force-loads the target chunk and picks a Y that keeps the whole frame within the build height limits. */
    private static BlockPos pickAnchor(ServerLevel target, BlockPos exitPos) {
        target.getChunk(exitPos);

        int x = exitPos.getX();
        int z = exitPos.getZ();
        int surfaceY = target.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        int y = Mth.clamp(surfaceY, target.getMinBuildHeight() + 1, target.getMaxBuildHeight() - ManateePortalShape.HEIGHT - 1);
        return new BlockPos(x, y, z);
    }
}
