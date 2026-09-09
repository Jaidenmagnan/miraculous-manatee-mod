package net.neetcoders.miraculousmanatee.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neetcoders.miraculousmanatee.registry.ModBlocks;
import net.neetcoders.miraculousmanatee.registry.ModItems;
import org.jetbrains.annotations.Nullable;

/**
 * A snow-layer style pile of blubber. Placing more blubber on a pile adds a layer; adding to a full pile of
 * {@value #MAX_LAYERS} layers turns it into a solid {@link BlubberBlock}.
 */
public class BlubberPileBlock extends Block {
    public static final int MAX_LAYERS = 8;
    public static final IntegerProperty LAYERS = IntegerProperty.create("layers", 1, MAX_LAYERS);

    /** Indexed by layer count; each layer is two pixels tall. Index 0 is never used but keeps the lookup direct. */
    private static final VoxelShape[] SHAPES = buildShapes();

    public BlubberPileBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(LAYERS, 1));
    }

    private static VoxelShape[] buildShapes() {
        VoxelShape[] shapes = new VoxelShape[MAX_LAYERS + 1];
        for (int layers = 0; layers <= MAX_LAYERS; layers++) {
            shapes[layers] = Block.box(0, 0, 0, 16, layers * 2, 16);
        }
        return shapes;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LAYERS);
    }

    /** Blubber can always be placed "into" a pile: it either adds a layer or upgrades a full pile to a block. */
    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return context.getItemInHand().is(ModItems.BLUBBER.get()) || super.canBeReplaced(state, context);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState clicked = context.getLevel().getBlockState(context.getClickedPos());
        if (clicked.is(this)) {
            int layers = clicked.getValue(LAYERS);
            if (layers >= MAX_LAYERS) {
                return ModBlocks.BLUBBER_BLOCK.get().defaultBlockState();
            }
            return clicked.setValue(LAYERS, layers + 1);
        }
        return this.defaultBlockState();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(LAYERS)];
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return Block.canSupportRigidBlock(level, pos.below());
    }
}
