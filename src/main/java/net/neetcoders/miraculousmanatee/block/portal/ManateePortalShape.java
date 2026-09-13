package net.neetcoders.miraculousmanatee.block.portal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neetcoders.miraculousmanatee.block.ManateePortalBlock;
import net.neetcoders.miraculousmanatee.registry.ModBlocks;

/**
 * The one place that knows the layout of a manatee portal frame: a 4-wide by 5-tall rectangle of prismarine
 * (or the altar keystone) enclosing a 2-wide by 3-tall interior of portal blocks. The anchor position -
 * where the altar keystone sits - is the bottom row, column index 1 (columns run 0..3 along the portal's
 * horizontal axis, rows run 0..4 upward from the bottom).
 *
 * <pre>
 * row 4: F F F F
 * row 3: F . . F
 * row 2: F . . F
 * row 1: F . . F
 * row 0: F A F F
 *        0 1 2 3   (column, F = frame, . = interior, A = anchor / altar)
 * </pre>
 */
public final class ManateePortalShape {
    public static final int WIDTH = 4;
    public static final int HEIGHT = 5;

    private ManateePortalShape() {
    }

    /** Column 1 is the anchor's own column; column/row are offsets within the frame as diagrammed above. */
    private static BlockPos at(BlockPos anchor, Direction.Axis axis, int column, int row) {
        return anchor.relative(axis, column - 1).above(row);
    }

    private static boolean isFramePosition(int column, int row) {
        return row == 0 || row == HEIGHT - 1 || column == 0 || column == WIDTH - 1;
    }

    /** True for the blocks allowed to form the portal frame: prismarine, or the altar keystone itself. */
    public static boolean isFrame(BlockState state) {
        return state.is(Blocks.PRISMARINE) || state.is(ModBlocks.MANATEE_PORTAL_ALTAR.get());
    }

    /**
     * Checks whether a portal could be built around {@code anchor} without disturbing existing blocks: every
     * position in the 4x5 rectangle other than the anchor itself must already be either replaceable or a
     * valid frame block.
     */
    public static boolean canBuild(LevelReader level, BlockPos anchor, Direction.Axis axis) {
        for (int column = 0; column < WIDTH; column++) {
            for (int row = 0; row < HEIGHT; row++) {
                if (column == 1 && row == 0) {
                    continue;
                }
                BlockState state = level.getBlockState(at(anchor, axis, column, row));
                if (!state.canBeReplaced() && !isFrame(state)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Places the portal frame and fills its interior with manatee portal blocks. When {@code keepAnchor} is
     * true the anchor position is left untouched so the altar block remains the keystone; otherwise it is
     * overwritten with frame prismarine like the rest of the border.
     *
     * <p>Blocks are placed with update flag {@link Block#UPDATE_CLIENTS} (2)
     * rather than the usual default (3, which also triggers neighbour shape updates). Portal blocks validate
     * their own shape reactively via {@link ManateePortalBlock#updateShape}, so applying neighbour updates
     * while the frame is still half-built would make the first placed portal blocks immediately see an
     * incomplete frame and pop themselves back to air.
     */
    public static void build(Level level, BlockPos anchor, Direction.Axis axis, boolean keepAnchor) {
        for (int column = 0; column < WIDTH; column++) {
            for (int row = 0; row < HEIGHT; row++) {
                if (keepAnchor && column == 1 && row == 0) {
                    continue;
                }
                BlockPos pos = at(anchor, axis, column, row);
                BlockState state = isFramePosition(column, row)
                        ? Blocks.PRISMARINE.defaultBlockState()
                        : ModBlocks.MANATEE_PORTAL.get().defaultBlockState().setValue(ManateePortalBlock.AXIS, axis);
                level.setBlock(pos, state, 2);
            }
        }
    }

    /** The lowest, first interior column (column 1, row 1) - the bottom-left corner of the 2x3 interior. */
    public static BlockPos interiorMin(BlockPos anchor, Direction.Axis axis) {
        return anchor.above();
    }

    /** The bottom centre of the 2-wide interior, shifted half a block along {@code axis} to sit between the two interior columns. */
    public static Vec3 entryPosition(BlockPos interiorMin, Direction.Axis axis) {
        Vec3 base = Vec3.atBottomCenterOf(interiorMin);
        return axis == Direction.Axis.X ? base.add(0.5, 0.0, 0.0) : base.add(0.0, 0.0, 0.5);
    }
}
