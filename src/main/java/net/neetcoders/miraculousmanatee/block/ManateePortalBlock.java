package net.neetcoders.miraculousmanatee.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neetcoders.miraculousmanatee.block.portal.ManateePortalShape;
import net.neetcoders.miraculousmanatee.block.portal.ManateePortalTravel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The interior block of a manatee portal, modelled closely on vanilla {@code NetherPortalBlock}. It carries
 * entities between dimensions via {@link ManateePortalTravel} and destroys itself if its frame (checked
 * through {@link ManateePortalShape#isFrame}) is broken.
 */
public class ManateePortalBlock extends Block implements Portal {
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
    protected static final VoxelShape X_AXIS_AABB = Block.box(0.0, 0.0, 6.0, 16.0, 16.0, 10.0);
    protected static final VoxelShape Z_AXIS_AABB = Block.box(6.0, 0.0, 0.0, 10.0, 16.0, 16.0);

    /** Ambient sparks spawned per client tick per portal block; deliberately sparse so a tall portal isn't noisy. */
    private static final int PARTICLES_PER_TICK = 2;
    /** Distance (blocks) from the block centre out to the portal face the sparks converge on. */
    private static final double PARTICLE_FACE_INSET = 0.25D;
    /** How far (blocks) in front of that face a spark starts before drifting back onto the portal. */
    private static final double PARTICLE_DRIFT_DISTANCE = 1.25D;
    /** Random scatter (blocks) applied within the portal plane so sparks don't all converge on one line. */
    private static final double PARTICLE_PLANE_SPREAD = 0.5D;

    public ManateePortalBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AXIS, Direction.Axis.X));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos,
            @NotNull CollisionContext context) {
        return switch (state.getValue(AXIS)) {
            case Z -> Z_AXIS_AABB;
            case X -> X_AXIS_AABB;
            default -> X_AXIS_AABB;
        };
    }

    @Override
    protected void entityInside(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Entity entity) {
        if (entity.canUsePortal(false)) {
            entity.setAsInsidePortal(this, pos);
        }
    }

    /** Players get the same NETHER_PORTAL-gamerule delay (shorter in creative) as a vanilla nether portal; other entities teleport instantly. */
    @Override
    public int getPortalTransitionTime(ServerLevel level, Entity entity) {
        return entity instanceof Player player
                ? Math.max(1, level.getGameRules().getInt(player.getAbilities().invulnerable
                        ? GameRules.RULE_PLAYERS_NETHER_PORTAL_CREATIVE_DELAY
                        : GameRules.RULE_PLAYERS_NETHER_PORTAL_DEFAULT_DELAY))
                : 0;
    }

    @Nullable
    @Override
    public DimensionTransition getPortalDestination(ServerLevel level, Entity entity, BlockPos pos) {
        return ManateePortalTravel.getDestination(level, entity, pos);
    }

    @Override
    public Portal.Transition getLocalTransition() {
        return Portal.Transition.CONFUSION;
    }

    /**
     * Converging nautilus sparks instead of vanilla's purple portal particles, plus the occasional portal
     * ambience sound.
     *
     * <p>{@link ParticleTypes#NAUTILUS} is drawn by vanilla's {@code FlyTowardsPositionParticle}: it spawns at
     * <em>position + velocity</em> and drifts back toward <em>position</em>, like an enchantment-table glyph.
     * So the arguments are used inside out compared to an ordinary particle - the position passed in is the
     * convergence point on the portal face, and the "velocity" is the offset the spark starts out at. The
     * nether-portal axis test below is kept: whichever horizontal axis the portal plane does <em>not</em> span
     * is the face normal, and sparks are launched out along it so they visibly fall back onto the portal.
     *
     * <p>Bubble particles were used here previously; they delete themselves as soon as they are not in water,
     * which is why a dry portal looked broken.
     */
    @Override
    public void animateTick(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        if (random.nextInt(100) == 0) {
            level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    SoundEvents.PORTAL_AMBIENT, SoundSource.BLOCKS, 0.5F, random.nextFloat() * 0.4F + 0.8F, false);
        }

        for (int i = 0; i < PARTICLES_PER_TICK; i++) {
            double x = pos.getX() + 0.5;
            double y = pos.getY() + random.nextDouble();
            double z = pos.getZ() + 0.5;
            double offsetX = (random.nextDouble() - 0.5) * PARTICLE_PLANE_SPREAD;
            double offsetY = (random.nextDouble() - 0.5) * PARTICLE_PLANE_SPREAD;
            double offsetZ = (random.nextDouble() - 0.5) * PARTICLE_PLANE_SPREAD;
            int side = random.nextInt(2) * 2 - 1;
            if (!level.getBlockState(pos.west()).is(this) && !level.getBlockState(pos.east()).is(this)) {
                x = pos.getX() + 0.5 + PARTICLE_FACE_INSET * side;
                offsetX = random.nextDouble() * PARTICLE_DRIFT_DISTANCE * side;
            } else {
                z = pos.getZ() + 0.5 + PARTICLE_FACE_INSET * side;
                offsetZ = random.nextDouble() * PARTICLE_DRIFT_DISTANCE * side;
            }
            level.addParticle(ParticleTypes.NAUTILUS, x, y, z, offsetX, offsetY, offsetZ);
        }
    }

    /**
     * Mirrors {@code NetherPortalBlock}: a neighbour update along a horizontal axis other than this portal's
     * own axis is ignored (the frame's side walls legitimately touch other blocks on that axis). Otherwise,
     * if the neighbour isn't another portal block or a valid frame block ({@link ManateePortalShape#isFrame}),
     * the frame has been broken and this block reverts to air.
     */
    @Override
    protected @NotNull BlockState updateShape(@NotNull BlockState state, @NotNull Direction direction,
            @NotNull BlockState neighborState, @NotNull LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockPos neighborPos) {
        Direction.Axis axis = direction.getAxis();
        Direction.Axis portalAxis = state.getValue(AXIS);
        boolean flag = axis != portalAxis && axis.isHorizontal();
        return !flag && !neighborState.is(this) && !ManateePortalShape.isFrame(neighborState)
                ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public @NotNull ItemStack getCloneItemStack(@NotNull LevelReader level, @NotNull BlockPos pos, @NotNull BlockState state) {
        return ItemStack.EMPTY;
    }
}
