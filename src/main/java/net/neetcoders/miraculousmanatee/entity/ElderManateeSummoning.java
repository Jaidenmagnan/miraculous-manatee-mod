package net.neetcoders.miraculousmanatee.entity;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;
import net.neetcoders.miraculousmanatee.registry.ModBlocks;
import net.neetcoders.miraculousmanatee.registry.ModEntities;
import org.jetbrains.annotations.Nullable;

/**
 * Elder Manatee boss summoning ritual, modeled directly on vanilla's wither summon
 * ({@code WitherSkullBlock}/{@code CarvedPumpkinBlock}).
 *
 * <p>The structure is a T made of four {@link ModBlocks#BLUBBER_BLOCK} blocks: three of them form the
 * top row, and the fourth sits directly below the middle block of that row as the stem. A
 * {@link ModBlocks#MANATEE_HEAD} sits on top of each of the three top-row blocks. Placing the LAST of
 * the three heads is what triggers the summon - the check runs from
 * {@link net.neetcoders.miraculousmanatee.block.ManateeHeadBlock#setPlacedBy}, exactly like the vanilla
 * wither triggers off the last wither skeleton skull placed on a soul sand T.
 *
 * <p>The pattern is matched in all four horizontal orientations ({@link BlockPattern#find}), so the T can
 * be built facing any direction. Note that the aisle strings passed to {@link BlockPatternBuilder#aisle}
 * are written TOP ROW FIRST: the first string is the row of heads, the second is the row of blubber
 * blocks the heads rest on, and the third is the stem below.
 */
public final class ElderManateeSummoning {
    /** Y offset above the spawn block's floor the boss is placed at, matching the vanilla wither's summon. */
    private static final double SPAWN_Y_OFFSET = 0.55D;
    /** Radius (blocks) around the spawn point searched for players to award the summon advancement to. */
    private static final double SUMMON_ADVANCEMENT_RADIUS = 50.0D;
    private static final float SUMMON_SOUND_VOLUME = 3.0F;
    private static final float SUMMON_SOUND_PITCH = 0.6F;
    /** Number of splash particles spread around the boss on spawn. */
    private static final int SPLASH_PARTICLE_COUNT = 60;
    /** Horizontal/vertical spread (blocks) of the spawn splash particles. */
    private static final double PARTICLE_SPREAD = 3.0D;

    @Nullable
    private static BlockPattern fullPattern;

    private ElderManateeSummoning() {
    }

    /**
     * Checks whether a full ritual structure is present around {@code pos} and, if so, summons the Elder
     * Manatee. Intended to be called from {@code ManateeHeadBlock.setPlacedBy} for the block just placed.
     *
     * @param level the level the head was placed in
     * @param pos   the position of the manatee head that was just placed
     */
    public static void trySummon(Level level, BlockPos pos) {
        if (level.isClientSide || pos.getY() < level.getMinBuildHeight() || level.getDifficulty() == Difficulty.PEACEFUL) {
            return;
        }

        BlockPattern.BlockPatternMatch match = getOrCreatePattern().find(level, pos);
        if (match == null) {
            return;
        }

        ElderManatee elder = ModEntities.ELDER_MANATEE.get().create(level);
        if (elder == null) {
            return;
        }

        CarvedPumpkinBlock.clearPatternBlocks(level, match);

        BlockPos spawnPos = match.getBlock(1, 2, 0).getPos();
        float yaw = match.getForwards().getAxis() == Direction.Axis.X ? 0.0F : 90.0F;
        elder.moveTo(spawnPos.getX() + 0.5, spawnPos.getY() + SPAWN_Y_OFFSET, spawnPos.getZ() + 0.5, yaw, 0.0F);
        elder.yBodyRot = yaw;

        // Resolved once and reused below for both finalizeSpawn and the spawn particles - the level is a
        // ServerLevel in every real case (trySummon already bailed out on level.isClientSide above).
        ServerLevel serverLevel = level instanceof ServerLevel sl ? sl : null;
        if (serverLevel != null) {
            elder.finalizeSpawn(serverLevel, level.getCurrentDifficultyAt(spawnPos), MobSpawnType.TRIGGERED, null);
        }
        elder.setPersistenceRequired();

        for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class,
                elder.getBoundingBox().inflate(SUMMON_ADVANCEMENT_RADIUS))) {
            CriteriaTriggers.SUMMONED_ENTITY.trigger(player, elder);
        }

        level.addFreshEntity(elder);
        CarvedPumpkinBlock.updatePatternBlocks(level, match);

        level.playSound(null, spawnPos, SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.HOSTILE, SUMMON_SOUND_VOLUME,
                SUMMON_SOUND_PITCH);
        level.playSound(null, spawnPos, SoundEvents.RAVAGER_ROAR, SoundSource.HOSTILE, SUMMON_SOUND_VOLUME,
                SUMMON_SOUND_PITCH);

        if (serverLevel != null) {
            serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, spawnPos.getX() + 0.5, spawnPos.getY() + 0.5,
                    spawnPos.getZ() + 0.5, 1, 0.0, 0.0, 0.0, 0.0);
            serverLevel.sendParticles(ParticleTypes.SPLASH, spawnPos.getX() + 0.5, spawnPos.getY() + 0.5,
                    spawnPos.getZ() + 0.5, SPLASH_PARTICLE_COUNT, PARTICLE_SPREAD, PARTICLE_SPREAD, PARTICLE_SPREAD, 0.0);
        }
    }

    /** Lazily builds the ritual's block pattern; must be lazy since block registries aren't populated at class-load time. */
    private static BlockPattern getOrCreatePattern() {
        if (fullPattern == null) {
            fullPattern = BlockPatternBuilder.start()
                    .aisle("^^^", "###", "~#~")
                    .where('#', BlockInWorld.hasState(BlockStatePredicate.forBlock(ModBlocks.BLUBBER_BLOCK.get())))
                    .where('^', BlockInWorld.hasState(BlockStatePredicate.forBlock(ModBlocks.MANATEE_HEAD.get())))
                    .where('~', predicate -> predicate.getState().isAir())
                    .build();
        }

        return fullPattern;
    }
}
